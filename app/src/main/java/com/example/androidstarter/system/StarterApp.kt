package com.example.androidstarter.system

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.androidstarter.application.appearance.AppearanceStore
import com.example.androidstarter.application.appearance.LocaleHelper
import com.example.androidstarter.application.privacy.AnalyticsConsent
import com.example.androidstarter.screens.logs.AppLogCollector
import com.example.androidstarter.screens.logs.CrashLogStore
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.*

@HiltAndroidApp
class StarterApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var appLogCollector: AppLogCollector
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.wrap(base))
        AppearanceStore.init(this)
    }
    override fun onCreate() {
        super.onCreate()
        AnalyticsConsent.sync(this)
        scope.launch { runCatching { appLogCollector.ensureStarted() } }
        runCatching { CrashLogStore.install(this) { appLogCollector.currentLogs } }
    }
}
