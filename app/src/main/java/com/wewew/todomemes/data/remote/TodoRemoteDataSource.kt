package com.wewew.todomemes.data.remote

import com.wewew.todomemes.TodoItem
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

interface TodoRemoteDataSource {
    suspend fun fetchAll(): Result<List<TodoItem>>
    suspend fun fetchById(uid: String): Result<TodoItem?>
    suspend fun create(item: TodoItem): Result<TodoItem>
    suspend fun update(item: TodoItem): Result<TodoItem>
    suspend fun delete(uid: String): Result<Boolean>
    suspend fun syncAll(items: List<TodoItem>): Result<List<TodoItem>>
}