package com.example.androidstarter.screens.settings

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.example.androidstarter.R
import com.example.androidstarter.application.appearance.AppearanceStore
import com.example.androidstarter.application.privacy.AnalyticsConsent
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/** The screen's Android boundary; tests substitute in-memory behavior, not Android mocks. */
interface SettingsEnvironment {
    fun themeMode(): AppearanceStore.ThemeMode
    fun languageTag(): String
    fun analyticsEnabled(): Boolean
    fun notificationsEnabled(): Boolean
    fun setThemeMode(mode: AppearanceStore.ThemeMode)
    /** Returns the normalized, persisted tag before the screen requests a process restart. */
    fun setLanguage(tag: String): String
    fun setAnalyticsEnabled(enabled: Boolean)
    suspend fun shareApk(): ApkShareOutcome
}

class AndroidSettingsEnvironment @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsEnvironment {
    override fun themeMode() = AppearanceStore.themeModeSync(context)
    override fun languageTag() = AppearanceStore.languageTag(context)
    override fun analyticsEnabled() = AnalyticsConsent.isEnabled(context)
    override fun notificationsEnabled() = NotificationManagerCompat.from(context).areNotificationsEnabled()
    override fun setThemeMode(mode: AppearanceStore.ThemeMode) = AppearanceStore.setThemeMode(context, mode)
    override fun setLanguage(tag: String): String {
        AppearanceStore.setLanguage(context, tag)
        return AppearanceStore.languageTag(context)
    }
    override fun setAnalyticsEnabled(enabled: Boolean) = AnalyticsConsent.setEnabled(context, enabled)
    override suspend fun shareApk() = AppApkSharer.share(context, context.getString(R.string.sys_share_app_title))
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    @Binds
    abstract fun bindSettingsEnvironment(implementation: AndroidSettingsEnvironment): SettingsEnvironment
}
