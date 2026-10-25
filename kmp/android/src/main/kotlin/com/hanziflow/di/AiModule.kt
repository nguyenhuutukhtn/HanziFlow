package com.hanziflow.di

import android.content.Context
import com.hanziflow.ai.AiProviders
import com.hanziflow.ai.ChatClient
import com.hanziflow.ai.ExplainPrompt
import com.hanziflow.ai.FallbackChatClient
import com.hanziflow.ai.OpenAiCompatibleClient
import com.hanziflow.ai.ProviderCooldowns
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
    fun provideAiProviders(): AiProviders = AiProviders.fromBuildConfig()

    @Provides
    @Singleton
    fun provideChatClient(@ApplicationContext context: Context, http: OkHttpClient, providers: AiProviders): ChatClient =
        FallbackChatClient(
            providers.list.map { OpenAiCompatibleClient(http, it) },
            ProviderCooldowns(context.getSharedPreferences("ai_cooldowns", Context.MODE_PRIVATE)),
            providers.list.map { it.model + it.baseUrl },
        )

    @Provides
    @Singleton
    fun provideExplainPrompt(@ApplicationContext context: Context): ExplainPrompt = ExplainPrompt.fromAssets(context)
}
