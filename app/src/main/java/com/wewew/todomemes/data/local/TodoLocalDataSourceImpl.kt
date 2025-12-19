package com.wewew.todomemes.data.local

import android.content.Context
import com.wewew.todomemes.TodoItem
import com.wewew.todomemes.json
import com.wewew.todomemes.parse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.File

class TodoLocalDataSourceImpl(
    context: Context,
    fileName: String = "todo_memes.json"
) : TodoLocalDataSource {

    private val logger = LoggerFactory.getLogger("TodoLocalDataSource")
    private val file: File = File(context.filesDir, fileName)

    private val _todosFlow = MutableStateFlow<List<TodoItem>>(emptyList())
    override val todosFlow: Flow<List<TodoItem>> = _todosFlow.asStateFlow()

    init {
        logger.info("TodoLocalDataSource initialized with file: ${file.absolutePath}")
    }

    override suspend fun loadAll(): List<TodoItem> = withContext(Dispatchers.IO) {
        logger.debug("loadAll() called")

        if (!file.exists()) {
            logger.info("Cache file does not exist")
            _todosFlow.value = emptyList()
            return@withContext emptyList()
        }

        try {
            val text = file.readText().trim()
            if (text.isBlank()) {
                logger.info("Cache file is empty")
                _todosFlow.value = emptyList()
                return@withContext emptyList()
            }

            val arr = runCatching { JSONArray(text) }.getOrNull()
                ?: runCatching { JSONObject(text).optJSONArray("items") }.getOrNull()
                ?: run {
                    logger.warn("Could not parse JSON from cache")
                    _todosFlow.value = emptyList()
                    return@withContext emptyList()
                }

            val items = mutableListOf<TodoItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val item = TodoItem.parse(obj) ?: continue
                items.add(item)
            }

            logger.info("Loaded ${items.size} items from cache")
            _todosFlow.value = items
            items
        } catch (e: Exception) {
            logger.error("Failed to load from cache: ${e.message}", e)
            _todosFlow.value = emptyList()
            emptyList()
        }
    }

    override suspend fun getById(uid: String): TodoItem? = withContext(Dispatchers.IO) {
        logger.debug("getById() called for uid=$uid")
        val items = _todosFlow.value.ifEmpty { loadAll() }
        items.find { it.uid == uid }
    }

    override suspend fun save(item: TodoItem) = withContext(Dispatchers.IO) {
        logger.debug("save() called for uid=${item.uid}")

        val currentItems = _todosFlow.value.toMutableList()
        val index = currentItems.indexOfFirst { it.uid == item.uid }

        if (index != -1) {
            currentItems[index] = item
            logger.info("Updated item in cache: uid=${item.uid}")
        } else {
            currentItems.add(item)
            logger.info("Added new item to cache: uid=${item.uid}")
        }

        _todosFlow.value = currentItems
        persistToFile(currentItems)
    }

    override suspend fun saveAll(items: List<TodoItem>) = withContext(Dispatchers.IO) {
        logger.debug("saveAll() called with ${items.size} items")
        _todosFlow.value = items
        persistToFile(items)
        logger.info("Saved ${items.size} items to cache")
    }

    override suspend fun delete(uid: String): Boolean = withContext(Dispatchers.IO) {
        logger.debug("delete() called for uid=$uid")

        val currentItems = _todosFlow.value.toMutableList()
        val removed = currentItems.removeAll { it.uid == uid }

        if (removed) {
            _todosFlow.value = currentItems
            persistToFile(currentItems)
            logger.info("Deleted item from cache: uid=$uid")
        } else {
            logger.warn("Item not found in cache: uid=$uid")
        }

        removed
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        logger.debug("clear() called")
        _todosFlow.value = emptyList()
        if (file.exists()) {
            file.delete()
        }
        logger.info("Cache cleared")
    }

    private fun persistToFile(items: List<TodoItem>) {
        try {
            val arr = JSONArray()
            items.forEach { arr.put(it.json) }
            file.parentFile?.mkdirs()
            file.writeText(arr.toString())
            logger.debug("Persisted ${items.size} items to file")
        } catch (e: Exception) {
            logger.error("Failed to persist to file: ${e.message}", e)
        }
    }
}