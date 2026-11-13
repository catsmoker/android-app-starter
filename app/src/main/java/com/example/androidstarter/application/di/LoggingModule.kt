package com.example.androidstarter.application.di

import com.example.androidstarter.screens.logs.LogcatStreamer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {
    @Provides
    @Singleton
    fun provideLogcatStreamer(): LogcatStreamer = LogcatStreamer()
}
