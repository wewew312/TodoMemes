package com.wewew.todomemes

import android.graphics.Color
import org.json.JSONObject
import java.time.Instant

private const val KEY_UID = "uid"
private const val KEY_TEXT = "text"
private const val KEY_IMPORTANCE = "importance"
private const val KEY_COLOR = "color"
private const val KEY_DEADLINE = "deadline"
private const val KEY_IS_DONE = "isDone"

fun TodoItem.Companion.parse(json: JSONObject): TodoItem? {
    val text = json.optString(KEY_TEXT, "").trim()
    if (text.isBlank()) return null

    val uid = json.optString(KEY_UID, "").ifBlank { java.util.UUID.randomUUID().toString() }
    val importance = Importance.fromRuName(json.optString(KEY_IMPORTANCE, null))

    val color = if (json.has(KEY_COLOR)) json.optInt(KEY_COLOR, Color.WHITE) else Color.WHITE

    val deadline = if (json.has(KEY_DEADLINE)) {
        val millis = json.optLong(KEY_DEADLINE, Long.MIN_VALUE)
        if (millis == Long.MIN_VALUE) null else Instant.ofEpochMilli(millis)
    } else {
        null
    }

    val isDone = json.optBoolean(KEY_IS_DONE, false)

    return TodoItem(
        uid = uid,
        text = text,
        importance = importance,
        color = color,
        deadline = deadline,
        isDone = isDone
    )
}

val TodoItem.json: JSONObject
    get() {
        val obj = JSONObject()
        obj.put(KEY_UID, uid)
        obj.put(KEY_TEXT, text)
        obj.put(KEY_IS_DONE, isDone)

        if (importance != Importance.NORMAL) obj.put(KEY_IMPORTANCE, importance.ruName)
        if (color != Color.WHITE) obj.put(KEY_COLOR, color)
        if (deadline != null) obj.put(KEY_DEADLINE, deadline.toEpochMilli())

        return obj
    }
