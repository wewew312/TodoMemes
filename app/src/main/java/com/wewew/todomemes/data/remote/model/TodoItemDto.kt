package com.wewew.todomemes.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodoItemDto(
    @SerialName("id") val id: String,
    @SerialName("text") val text: String,
    @SerialName("importance") val importance: String,
    @SerialName("deadline") val deadline: Long? = null,
    @SerialName("done") val isDone: Boolean,
    @SerialName("color") val color: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("changed_at") val changedAt: Long,
    @SerialName("last_updated_by") val lastUpdatedBy: String,
)

@Serializable
data class FetchListResponse(
    val status: String,
    val list: List<TodoItemDto>,
    val revision: Int
)

@Serializable
data class FetchItemResponse(
    val status: String,
    val element: TodoItemDto,
    val revision: Int
)

@Serializable
data class ItemResponse(
    val status: String,
    val element: TodoItemDto,
    val revision: Int
)

@Serializable
data class ElementRequest(
    val element: TodoItemDto
)

@Serializable
data class PatchListRequest(
    val list: List<TodoItemDto>
)
