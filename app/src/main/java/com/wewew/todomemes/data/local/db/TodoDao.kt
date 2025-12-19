package com.wewew.todomemes.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos")
    fun getTodosFlow(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE uid = :uid")
    suspend fun getTodoByUid(uid: String): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTodo(note: TodoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTodos(notes: List<TodoEntity>)

    @Query("DELETE FROM todos")
    suspend fun deleteAll()

    @Query("DELETE FROM todos WHERE uid = :uid")
    suspend fun deleteTodoByUid(uid: String): Int
}