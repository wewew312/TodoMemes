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

interface TodoLocalDataSource {
    val todosFlow: Flow<List<TodoItem>>
    suspend fun loadAll(): List<TodoItem>
    suspend fun getById(uid: String): TodoItem?
    suspend fun save(item: TodoItem)
    suspend fun saveAll(items: List<TodoItem>)
    suspend fun delete(uid: String): Boolean
    suspend fun clear()
}

