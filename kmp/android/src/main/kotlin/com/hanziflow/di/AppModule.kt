package com.hanziflow.di

import android.content.Context
import com.hanziflow.data.CourseDao
import com.hanziflow.data.CourseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

/** Room DB / DAO and the shared OkHttp client: the only places this app talks to disk/network directly. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun provideCourseDatabase(@ApplicationContext context: Context): CourseDatabase = CourseDatabase.open(context)

    @Provides
    fun provideCourseDao(database: CourseDatabase): CourseDao = database.dao()
}
