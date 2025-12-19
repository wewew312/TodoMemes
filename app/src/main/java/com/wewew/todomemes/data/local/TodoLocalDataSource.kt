package com.wewew.todomemes.data.local

import com.wewew.todomemes.data.local.model.TodoItem
import kotlinx.coroutines.flow.Flow

interface TodoLocalDataSource {
    val todosFlow: Flow<List<TodoItem>>
    suspend fun loadAll(): List<TodoItem>
    suspend fun getById(uid: String): TodoItem?
    suspend fun save(item: TodoItem)
    suspend fun saveAll(items: List<TodoItem>)
    suspend fun delete(uid: String): Boolean
    suspend fun clear()
}

