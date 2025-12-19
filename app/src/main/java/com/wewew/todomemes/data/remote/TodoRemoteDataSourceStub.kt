package com.wewew.todomemes.data.remote

import com.wewew.todomemes.TodoItem
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

class TodoRemoteDataSourceStub : TodoRemoteDataSource {

    private val logger = LoggerFactory.getLogger("TodoRemoteDataSource")

    private val fakeNetworkDelay = 500L

    override suspend fun fetchAll(): Result<List<TodoItem>> {
        logger.info("[NETWORK STUB] fetchAll() - Fetching all todos from backend")
        delay(fakeNetworkDelay)
        logger.info("[NETWORK STUB] fetchAll() - Backend returned empty list (stub)")
        return Result.success(emptyList())
    }

    override suspend fun fetchById(uid: String): Result<TodoItem?> {
        logger.info("[NETWORK STUB] fetchById() - Fetching todo uid=$uid from backend")
        delay(fakeNetworkDelay)
        logger.info("[NETWORK STUB] fetchById() - Backend returned null (stub)")
        return Result.success(null)
    }

    override suspend fun create(item: TodoItem): Result<TodoItem> {
        logger.info("[NETWORK STUB] create() - Sending new todo to backend: uid=${item.uid}, text='${item.text}'")
        delay(fakeNetworkDelay)
        logger.info("[NETWORK STUB] create() - Backend confirmed creation (stub)")
        return Result.success(item)
    }

    override suspend fun update(item: TodoItem): Result<TodoItem> {
        logger.info("[NETWORK STUB] update() - Updating todo on backend: uid=${item.uid}, text='${item.text}', isDone=${item.isDone}")
        delay(fakeNetworkDelay)
        logger.info("[NETWORK STUB] update() - Backend confirmed update (stub)")
        return Result.success(item)
    }

    override suspend fun delete(uid: String): Result<Boolean> {
        logger.info("[NETWORK STUB] delete() - Deleting todo from backend: uid=$uid")
        delay(fakeNetworkDelay)
        logger.info("[NETWORK STUB] delete() - Backend confirmed deletion (stub)")
        return Result.success(true)
    }

    override suspend fun syncAll(items: List<TodoItem>): Result<List<TodoItem>> {
        logger.info("[NETWORK STUB] syncAll() - Syncing ${items.size} todos with backend")
        delay(fakeNetworkDelay)
        items.forEach { item ->
            logger.debug("[NETWORK STUB] syncAll() - Syncing item: uid=${item.uid}, text='${item.text}'")
        }
        logger.info("[NETWORK STUB] syncAll() - Backend sync completed (stub)")
        return Result.success(items)
    }
}