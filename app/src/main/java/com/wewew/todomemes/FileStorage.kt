package com.wewew.todomemes

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.File

class FileStorage(
    context: Context,
    fileName: String = "todo_memes.json"
) {
    private val logger = LoggerFactory.getLogger("FileStorage")
    private val file: File = File(context.filesDir, fileName)

    private val items = mutableListOf<TodoItem>()

    init {
        logger.info("FileStorage initialized with file: ${file.absolutePath}")
    }

    fun getItems(): List<TodoItem> {
        logger.debug("getItems() called, returning ${items.size} items")
        return items.toList()
    }

    fun add(item: TodoItem) {
        val index = items.indexOfFirst { it.uid == item.uid }
        if (index != -1) {
            items[index] = item
            logger.info("Updated existing item: uid=${item.uid}, text='${item.text}'")
        } else {
            items.add(item)
            logger.info("Added new item: uid=${item.uid}, text='${item.text}'")
        }
    }

    fun remove(uid: String): Boolean {
        val removed = items.removeIf { it.uid == uid }
        if (removed) {
            logger.info("Removed item with uid=$uid")
        } else {
            logger.warn("Attempted to remove non-existent item with uid=$uid")
        }
        return removed
    }

    fun save() {
        logger.debug("save() called, saving ${items.size} items")
        try {
            val arr = JSONArray()
            items.forEach { arr.put(it.json) }
            file.parentFile?.mkdirs()
            file.writeText(arr.toString())
            logger.info("Successfully saved ${items.size} items to ${file.name}")
        } catch (e: Exception) {
            logger.error("Failed to save items to file: ${e.message}", e)
        }
    }

    fun load() {
        logger.debug("load() called, loading from ${file.name}")
        items.clear()

        if (!file.exists()) {
            logger.info("File does not exist: ${file.absolutePath}")
            return
        }

        try {
            val text = file.readText().trim()
            if (text.isBlank()) {
                logger.info("File is empty: ${file.name}")
                return
            }

            val arr = runCatching { JSONArray(text) }.getOrNull()
                ?: runCatching { JSONObject(text).optJSONArray("items") }.getOrNull()
                ?: run {
                    logger.warn("Could not parse JSON from file: ${file.name}")
                    return
                }

            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val item = TodoItem.parse(obj) ?: continue
                items.add(item)
            }
            logger.info("Successfully loaded ${items.size} items from ${file.name}")
        } catch (e: Exception) {
            logger.error("Failed to load items from file: ${e.message}", e)
        }
    }
}
