package com.wewew.todomemes.di

import android.content.Context
import com.wewew.todomemes.data.local.TodoLocalDataSource
import com.wewew.todomemes.data.local.TodoLocalDataSourceImpl
import com.wewew.todomemes.data.remote.TodoRemoteDataSource
import com.wewew.todomemes.data.remote.TodoRemoteDataSourceStub
import com.wewew.todomemes.data.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoLocalDataSource(
        @ApplicationContext context: Context
    ): TodoLocalDataSource {
        return TodoLocalDataSourceImpl(context)
    }

    @Provides
    @Singleton
    fun provideTodoRemoteDataSource(): TodoRemoteDataSource {
        return TodoRemoteDataSourceStub()
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
