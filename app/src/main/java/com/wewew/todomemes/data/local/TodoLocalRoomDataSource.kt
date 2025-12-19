package com.wewew.todomemes.data.local

import com.wewew.todomemes.data.local.db.TodoDao
import com.wewew.todomemes.data.local.db.toDomain
import com.wewew.todomemes.data.local.db.toEntity
import com.wewew.todomemes.data.local.model.TodoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory

class TodoLocalRoomDataSource(
    private val todoDao: TodoDao
) : TodoLocalDataSource {

    private val logger = LoggerFactory.getLogger(TodoLocalRoomDataSource::class.java)

    override val todosFlow: Flow<List<TodoItem>>
        get() = todoDao.getTodosFlow()
            .onEach { entities ->
                logger.debug("todosFlow emit: ${entities.size} entities")
            }
            .map { entities ->
                entities.map { it.toDomain() }
            }

    override suspend fun getById(uid: String): TodoItem? {
        logger.debug("getById(uid={})", uid)
        return try {
            val item = todoDao.getTodoByUid(uid)?.toDomain()
            logger.debug("getById(uid={}) -> {}", uid, item?.uid)
            item
        } catch (e: Exception) {
            logger.error("getById(uid=$uid) failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun save(item: TodoItem) {
        logger.info("save(uid={}, textLen={}, done={})", item.uid, item.text.length, item.isDone)
        try {
            todoDao.addTodo(item.toEntity())
            logger.debug("save(uid={}) ok", item.uid)
        } catch (e: Exception) {
            logger.error("save(uid=${item.uid}) failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun saveAll(items: List<TodoItem>) {
        logger.info("saveAll(count={})", items.size)
        try {
            todoDao.addTodos(items.map { it.toEntity() })
            logger.debug("saveAll ok")
        } catch (e: Exception) {
            logger.error("saveAll failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun delete(uid: String): Boolean {
        logger.info("delete(uid={})", uid)
        return try {
            val rows = todoDao.deleteTodoByUid(uid)
            val ok = rows > 0
            logger.debug("delete(uid={}) -> rows={}, ok={}", uid, rows, ok)
            ok
        } catch (e: Exception) {
            logger.error("delete(uid=$uid) failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun clear() {
        logger.info("clear()")
        try {
            todoDao.deleteAll()
            logger.debug("clear() ok")
        } catch (e: Exception) {
            logger.error("clear() failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun loadAll(): List<TodoItem> {
        logger.info("loadAll()")
        return try {
            val list = todoDao.getTodosFlow().first().map { it.toDomain() }
            logger.info("loadAll() -> {} items", list.size)
            list
        } catch (e: Exception) {
            logger.error("loadAll() failed: ${e.message}", e)
            throw e
        }
    }
}
