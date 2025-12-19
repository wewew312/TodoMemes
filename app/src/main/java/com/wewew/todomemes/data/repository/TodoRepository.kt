package com.wewew.todomemes.data.repository

import com.wewew.todomemes.data.local.model.TodoItem
import com.wewew.todomemes.data.local.TodoLocalDataSource
import com.wewew.todomemes.data.remote.TodoRemoteDataSource
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory

class TodoRepository(
    private val localDataSource: TodoLocalDataSource,
    private val remoteDataSource: TodoRemoteDataSource
) {
    private val logger = LoggerFactory.getLogger("TodoRepository")

    val todosFlow: Flow<List<TodoItem>> = localDataSource.todosFlow

    suspend fun loadTodos(): List<TodoItem> {
        logger.info("loadTodos() - Loading todos from cache")
        val cachedItems = localDataSource.loadAll()

        logger.info("loadTodos() - Attempting to fetch from backend")
        val remoteResult = remoteDataSource.fetchAll()

        remoteResult.onSuccess { remoteItems ->
            if (remoteItems.isNotEmpty()) {
                logger.info("loadTodos() - Received ${remoteItems.size} items from backend, updating cache")
                localDataSource.saveAll(remoteItems)
                return remoteItems
            }
        }.onFailure { error ->
            logger.warn("loadTodos() - Backend fetch failed: ${error.message}, using cache")
        }

        return cachedItems
    }

    suspend fun getTodoById(uid: String): TodoItem? {
        logger.info("getTodoById() - Getting todo uid=$uid")
        return localDataSource.getById(uid)
    }

    suspend fun saveTodo(item: TodoItem) {
        logger.info("saveTodo() - Saving todo uid=${item.uid}")

        localDataSource.save(item)
        logger.info("saveTodo() - Saved to cache")

        val isNew = localDataSource.getById(item.uid) == null
        if (isNew) {
            remoteDataSource.create(item).onSuccess {
                logger.info("saveTodo() - Created on backend")
            }.onFailure { error ->
                logger.warn("saveTodo() - Backend create failed: ${error.message}")
            }
        } else {
            remoteDataSource.update(item).onSuccess {
                logger.info("saveTodo() - Updated on backend")
            }.onFailure { error ->
                logger.warn("saveTodo() - Backend update failed: ${error.message}")
            }
        }
    }

    suspend fun deleteTodo(uid: String): Boolean {
        logger.info("deleteTodo() - Deleting todo uid=$uid")

        val deleted = localDataSource.delete(uid)
        if (deleted) {
            logger.info("deleteTodo() - Deleted from cache")

            remoteDataSource.delete(uid).onSuccess {
                logger.info("deleteTodo() - Deleted from backend")
            }.onFailure { error ->
                logger.warn("deleteTodo() - Backend delete failed: ${error.message}")
            }
        }

        return deleted
    }

    suspend fun toggleDone(item: TodoItem): TodoItem {
        logger.info("toggleDone() - Toggling done status for uid=${item.uid}")
        val updatedItem = item.copy(isDone = !item.isDone)
        saveTodo(updatedItem)
        return updatedItem
    }

    suspend fun syncWithBackend() {
        logger.info("syncWithBackend() - Starting sync")
        val localItems = localDataSource.loadAll()

        remoteDataSource.syncAll(localItems).onSuccess { syncedItems ->
            logger.info("syncWithBackend() - Sync completed with ${syncedItems.size} items")
            localDataSource.saveAll(syncedItems)
        }.onFailure { error ->
            logger.error("syncWithBackend() - Sync failed: ${error.message}")
        }
    }

    suspend fun clearCache() {
        logger.info("clearCache() - Clearing local cache")
        localDataSource.clear()
    }
}