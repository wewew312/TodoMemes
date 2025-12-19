package com.wewew.todomemes.data.remote

import com.wewew.todomemes.data.local.model.TodoItem
import com.wewew.todomemes.data.remote.api.TodosApi
import com.wewew.todomemes.data.remote.model.ElementRequest
import com.wewew.todomemes.data.remote.model.PatchListRequest
import com.wewew.todomemes.data.remote.model.toDomain
import com.wewew.todomemes.data.remote.model.toDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.math.min

class TodoRemoteDataSourceImpl(
    private val api: TodosApi,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : TodoRemoteDataSource {

    private val logger = LoggerFactory.getLogger("TodoRemoteDataSource")

    private var revision: Int = 0

    private val operationQueue = Channel<suspend () -> Unit>(capacity = Channel.UNLIMITED)

    init {
        scope.launch {
            for (operation in operationQueue) {
                runWithRetry(operation)
            }
        }
    }

    override suspend fun fetchAll(): Result<List<TodoItem>> = runCatching {
        logger.info("fetchAll() - Fetching all todos from backend")
        val response = retryOnFailure { api.fetchList() }
        revision = response.revision
        val items = response.list.map { it.toDomain() }
        logger.info("fetchAll() - Received ${items.size} items, revision=$revision")
        items
    }

    override suspend fun fetchById(uid: String): Result<TodoItem?> = runCatching {
        logger.info("fetchById() - Fetching todo uid=$uid")
        val response = api.fetchItem(uid)
        revision = response.revision
        response.element.toDomain()
    }

    override suspend fun create(item: TodoItem): Result<TodoItem> = runCatching {
        logger.info("create() - Creating todo uid=${item.uid}")
        operationQueue.send { createNetwork(item) }
        item
    }

    override suspend fun update(item: TodoItem): Result<TodoItem> = runCatching {
        logger.info("update() - Updating todo uid=${item.uid}")
        operationQueue.send { updateNetwork(item) }
        item
    }

    override suspend fun delete(uid: String): Result<Boolean> = runCatching {
        logger.info("delete() - Deleting todo uid=$uid")
        operationQueue.send { deleteNetwork(uid) }
        true
    }

    override suspend fun syncAll(items: List<TodoItem>): Result<List<TodoItem>> = runCatching {
        logger.info("syncAll() - Syncing ${items.size} todos with backend")
        patchAllWithRetry(items)
    }

    private suspend fun patchAllWithRetry(items: List<TodoItem>): List<TodoItem> {
        var delayMs = INITIAL_DELAY_MS
        while (true) {
            try {
                val response = api.patchList(
                    revision = revision,
                    body = PatchListRequest(items.map { it.toDto() })
                )
                revision = response.revision
                logger.info("syncAll() - Sync completed, revision=$revision")
                return response.list.map { it.toDomain() }
            } catch (e: HttpException) {
                if (e.code() == 400) {
                    logger.warn("syncAll() - Revision mismatch, refreshing...")
                    val fresh = api.fetchList()
                    revision = fresh.revision
                    continue
                }
                if (!shouldRetry(e)) throw e
            } catch (e: Throwable) {
                if (!shouldRetry(e)) throw e
            }
            logger.info("syncAll() - Retrying in ${delayMs}ms...")
            delay(delayMs)
            delayMs = min(delayMs * 2, MAX_DELAY_MS)
        }
    }

    private suspend fun createNetwork(item: TodoItem) {
        try {
            val response = api.addItem(
                revision = revision,
                body = ElementRequest(item.toDto())
            )
            revision = response.revision
            logger.info("create() - Created on backend, revision=$revision")
        } catch (e: HttpException) {
            if (e.code() == 400) {
                refreshRevision()
                throw e
            }
            throw e
        }
    }

    private suspend fun updateNetwork(item: TodoItem) {
        try {
            val response = api.updateItem(
                revision = revision,
                id = item.uid,
                body = ElementRequest(item.toDto())
            )
            revision = response.revision
            logger.info("update() - Updated on backend, revision=$revision")
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> {
                    logger.info("update() - Item not found, creating...")
                    createNetwork(item)
                }
                400 -> {
                    refreshRevision()
                    throw e
                }
                else -> throw e
            }
        }
    }

    private suspend fun deleteNetwork(uid: String) {
        try {
            val response = api.deleteItem(
                revision = revision,
                id = uid
            )
            revision = response.revision
            logger.info("delete() - Deleted from backend, revision=$revision")
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> logger.warn("delete() - Item not found on server, ignoring")
                400 -> {
                    refreshRevision()
                    throw e
                }
                else -> throw e
            }
        }
    }

    private suspend fun refreshRevision() {
        val fresh = api.fetchList()
        revision = fresh.revision
        logger.info("Revision refreshed to $revision")
    }

    private suspend fun runWithRetry(
        operation: suspend () -> Unit,
        initialDelayMs: Long = INITIAL_DELAY_MS,
        maxDelayMs: Long = MAX_DELAY_MS
    ) {
        var delayMs = initialDelayMs
        while (true) {
            try {
                operation()
                return
            } catch (e: Throwable) {
                if (!shouldRetryModification(e)) {
                    logger.error("Operation failed permanently: ${e.message}")
                    return
                }
                logger.warn("Operation failed, retrying in ${delayMs}ms: ${e.message}")
                delay(delayMs)
                delayMs = min(delayMs * 2, maxDelayMs)
            }
        }
    }

    private suspend fun <T> retryOnFailure(
        initialDelayMs: Long = INITIAL_DELAY_MS,
        maxDelayMs: Long = MAX_DELAY_MS,
        block: suspend () -> T
    ): T {
        var delayMs = initialDelayMs
        while (true) {
            try {
                return block()
            } catch (e: Throwable) {
                if (!shouldRetry(e)) throw e
                logger.warn("Request failed, retrying in ${delayMs}ms: ${e.message}")
                delay(delayMs)
                delayMs = min(delayMs * 2, maxDelayMs)
            }
        }
    }

    private fun shouldRetry(e: Throwable): Boolean = when (e) {
        is SocketTimeoutException -> true
        is IOException -> true
        is HttpException -> e.code() in 500..599 || e.code() == 429
        else -> false
    }

    private fun shouldRetryModification(e: Throwable): Boolean = when (e) {
        is HttpException -> shouldRetry(e) || e.code() == 400
        else -> shouldRetry(e)
    }

    companion object {
        private const val INITIAL_DELAY_MS = 1_000L
        private const val MAX_DELAY_MS = 30_000L
    }
}
