package com.wewew.todomemes.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.wewew.todomemes.data.local.TodoLocalDataSource
import com.wewew.todomemes.data.local.TodoLocalDataSourceImpl
import com.wewew.todomemes.data.local.TodoLocalRoomDataSource
import com.wewew.todomemes.data.local.db.TodoDatabase
import com.wewew.todomemes.data.remote.TodoRemoteDataSource
import com.wewew.todomemes.data.remote.TodoRemoteDataSourceImpl
import com.wewew.todomemes.data.remote.api.AuthInterceptor
import com.wewew.todomemes.data.remote.api.TodosApi
import com.wewew.todomemes.data.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://hive.mrdekk.ru/todo/"
    private const val TOKEN = "2271375c-19eb-44a6-b3e2-6c03a659efb4"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(TOKEN))
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideTodosApi(retrofit: Retrofit): TodosApi =
        retrofit.create(TodosApi::class.java)

//    @Provides
//    @Singleton
//    fun provideTodoLocalDataSource(
//        @ApplicationContext context: Context
//    ): TodoLocalDataSource {
//        return TodoLocalDataSourceImpl(context)
//    }

    @Provides
    @Singleton
    fun provideLocalRoomDataSource(
        @ApplicationContext context: Context
    ): TodoLocalDataSource {
        return TodoLocalRoomDataSource(
            todoDao = TodoDatabase.getInstance(context).noteDao()
        )
    }

    @Provides
    @Singleton
    fun provideTodoRemoteDataSource(
        api: TodosApi
    ): TodoRemoteDataSource {
        return TodoRemoteDataSourceImpl(api)
    }

    @Provides
    @Singleton
    fun provideTodoRepository(
        localDataSource: TodoLocalDataSource,
        remoteDataSource: TodoRemoteDataSource
    ): TodoRepository {
        return TodoRepository(localDataSource, remoteDataSource)
    }
}
