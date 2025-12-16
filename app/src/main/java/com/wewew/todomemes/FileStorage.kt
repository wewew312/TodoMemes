package com.wewew.todomemes

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FileStorage(
    context: Context,
    fileName: String = "todo_memes.json"
) {
    private val file: File = File(context.filesDir, fileName)

    private val items = mutableListOf<TodoItem>()

    fun getItems(): List<TodoItem> = items.toList()

    fun add(item: TodoItem) {
        val index = items.indexOfFirst { it.uid == item.uid }
        if (index != -1) {
            items[index] = item
        } else {
            items.add(item)
        }
    }

    fun remove(uid: String): Boolean {
        return items.removeIf { it.uid == uid }
    }

    fun save() {
        val arr = JSONArray()
        items.forEach { arr.put(it.json) }
        file.parentFile?.mkdirs()
        file.writeText(arr.toString())
    }

    fun load() {
        items.clear()
        if (!file.exists()) return

        val text = file.readText().trim()
        if (text.isBlank()) return

        val arr = runCatching { JSONArray(text) }.getOrNull()
            ?: runCatching { JSONObject(text).optJSONArray("items") }.getOrNull()
            ?: return

        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val item = TodoItem.parse(obj) ?: continue
            items.add(item)
        }
    }
}
