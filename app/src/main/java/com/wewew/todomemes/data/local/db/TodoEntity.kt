package com.wewew.todomemes.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wewew.todomemes.data.local.model.Importance
import com.wewew.todomemes.data.local.model.TodoItem
import java.time.Instant

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val uid: String,
    val text: String,
    val color: Int,
    val importance: String,
    val createdAt: Long,
    val isDone: Boolean = false,
    val selfDestructAt: Long? = null
)

fun TodoEntity.toDomain(): TodoItem = TodoItem(
    uid = uid,
    text = text,
    color = color,
    importance = when (importance.uppercase()) {
        "HIGH" -> Importance.HIGH
        "LOW" -> Importance.LOW
        else -> Importance.NORMAL
    },
    isDone = isDone,
    deadline = selfDestructAt.toInstantOrNull()
)

fun TodoItem.toEntity(): TodoEntity {
    val now = System.currentTimeMillis()

    return TodoEntity(
        uid = this.uid,
        text = this.text,
        color = this.color,
        importance = when (this.importance) {
            Importance.HIGH -> "HIGH"
            Importance.LOW -> "LOW"
            Importance.NORMAL -> "NORMAL"
            else -> "NORMAL"
        },
        createdAt = now,
        isDone = isDone,
        selfDestructAt = this.deadline?.toEpochMilliOrNull()
    )
}

fun Long?.toInstantOrNull(): Instant? = this?.let { Instant.ofEpochMilli(it) }
fun Instant?.toEpochMilliOrNull(): Long? = this?.toEpochMilli()