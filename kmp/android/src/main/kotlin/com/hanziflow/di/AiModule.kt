package com.hanziflow.di

import android.content.Context
import com.hanziflow.ai.AiConfig
import com.hanziflow.ai.ChatClient
import com.hanziflow.ai.ExplainPrompt
import com.hanziflow.ai.OpenAiCompatibleClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    @Provides
    @Singleton
    fun provideAiConfig(): AiConfig = AiConfig.fromBuildConfig()

    @Provides
    @Singleton
    fun provideChatClient(http: OkHttpClient, config: AiConfig): ChatClient = OpenAiCompatibleClient(http, config)

    @Provides
    @Singleton
    fun provideExplainPrompt(@ApplicationContext context: Context): ExplainPrompt = ExplainPrompt.fromAssets(context)
}
