package com.hanziflow.di

import com.hanziflow.data.CourseRepository
import com.hanziflow.data.CourseRepositoryImpl
import com.hanziflow.data.ProgressRepository
import com.hanziflow.data.ProgressRepositoryImpl
import com.hanziflow.data.SettingsRepository
import com.hanziflow.data.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCourseRepository(impl: CourseRepositoryImpl): CourseRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository
}
