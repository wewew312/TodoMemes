package com.wewew.todomemes.data.remote.api

import com.wewew.todomemes.data.remote.model.ElementRequest
import com.wewew.todomemes.data.remote.model.FetchItemResponse
import com.wewew.todomemes.data.remote.model.FetchListResponse
import com.wewew.todomemes.data.remote.model.ItemResponse
import com.wewew.todomemes.data.remote.model.PatchListRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TodosApi {

    @GET("list")
    suspend fun fetchList(
        @Header("X-Generate-Fails") fails: Int? = null
    ): FetchListResponse

    @PATCH("list")
    suspend fun patchList(
        @Header("X-Last-Known-Revision") revision: Int,
        @Body body: PatchListRequest,
        @Header("X-Generate-Fails") fails: Int? = null
    ): FetchListResponse

    @GET("list/{id}")
    suspend fun fetchItem(
        @Path("id") id: String
    ): FetchItemResponse

    @POST("list")
    suspend fun addItem(
        @Header("X-Last-Known-Revision") revision: Int,
        @Body body: ElementRequest
    ): ItemResponse

    @PUT("list/{id}")
    suspend fun updateItem(
        @Header("X-Last-Known-Revision") revision: Int,
        @Path("id") id: String,
        @Body body: ElementRequest
    ): ItemResponse

    @DELETE("list/{id}")
    suspend fun deleteItem(
        @Header("X-Last-Known-Revision") revision: Int,
        @Path("id") id: String
    ): ItemResponse
}
