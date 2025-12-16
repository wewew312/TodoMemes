package com.wewew.todomemes

import android.graphics.Color
import java.time.Instant
import java.util.UUID

data class TodoItem(
    val uid: String = UUID.randomUUID().toString(),
    val text: String,
    val importance: Importance = Importance.NORMAL,
    val color: Int = Color.WHITE,
    val deadline: Instant? = null,
    val isDone: Boolean = false
){
    companion object
}
