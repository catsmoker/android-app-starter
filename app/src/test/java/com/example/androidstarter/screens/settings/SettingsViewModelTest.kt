package com.example.androidstarter.screens.settings

import com.example.androidstarter.application.appearance.AppearanceStore.ThemeMode
import com.example.androidstarter.testing.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val environment = FakeSettingsEnvironment()

    @Test fun `initial state reflects saved preferences and OS notifications`() {
        environment.theme = ThemeMode.DARK
        environment.language = "ar"
        environment.analytics = true
        environment.notifications = false
        val state = SettingsViewModel(environment).uiState.value
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals("ar", state.languageTag)
        assertTrue(state.analyticsEnabled)
        assertFalse(state.notificationsEnabled)
        assertFalse(state.isSharingApk)
    }

    @Test fun `theme updates persist and are read by a new ViewModel`() {
        val vm = SettingsViewModel(environment)
        vm.onThemeModeChanged(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, vm.uiState.value.themeMode)
        assertEquals(ThemeMode.LIGHT, SettingsViewModel(environment).uiState.value.themeMode)
    }

    @Test fun `analytics choice persists both opt in and opt out`() {
        val vm = SettingsViewModel(environment)
        vm.onAnalyticsToggled(true)
        assertTrue(vm.uiState.value.analyticsEnabled)
        assertTrue(environment.analytics)
        vm.onAnalyticsToggled(false)
        assertFalse(vm.uiState.value.analyticsEnabled)
        assertFalse(SettingsViewModel(environment).uiState.value.analyticsEnabled)
    }

    @Test fun `notification resume refresh does not overwrite other preferences`() {
        val vm = SettingsViewModel(environment)
        vm.onThemeModeChanged(ThemeMode.DARK)
        environment.notifications = false
        vm.refreshNotifications()
        assertFalse(vm.uiState.value.notificationsEnabled)
        assertEquals(ThemeMode.DARK, vm.uiState.value.themeMode)
    }

    @Test fun `language is persisted and normalized before a late collector gets restart`() = runTest {
        val vm = SettingsViewModel(environment)
        vm.onLanguageChanged("ar-SA")
        assertEquals("ar", environment.language)
        assertEquals("ar", vm.uiState.value.languageTag)
        assertEquals(SettingsViewModel.UiEvent.RestartApp, vm.events.first())
    }

    @Test fun `same language does not write or restart`() = runTest {
        val vm = SettingsViewModel(environment)
        var events = 0
        backgroundScope.launch { vm.events.collect { events++ } }
        vm.onLanguageChanged("")
        runCurrent()
        assertEquals(0, environment.languageWrites)
        assertEquals(0, events)
    }

    @Test fun `equivalent normalized language does not restart`() = runTest {
        environment.language = "ar"
        val vm = SettingsViewModel(environment)
        var events = 0
        backgroundScope.launch { vm.events.collect { events++ } }
        vm.onLanguageChanged("ar-SA")
        runCurrent()
        assertEquals("ar", vm.uiState.value.languageTag)
        assertEquals(0, events)
    }

    @Test fun `sharing exposes busy state and rejects double tap`() = runTest {
        val result = CompletableDeferred<ApkShareOutcome>()
        environment.share = { result.await() }
        val vm = SettingsViewModel(environment)
        vm.onShareApp()
        vm.onShareApp()
        assertTrue(vm.uiState.value.isSharingApk)
        runCurrent()
        assertEquals(1, environment.shareCalls)
        result.complete(ApkShareOutcome.Shared(splitApks = false))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSharingApk)
    }

    @Test fun `ordinary successful share does not show an error`() = runTest {
        val vm = SettingsViewModel(environment)
        val messages = mutableListOf<SettingsViewModel.UiMessage>()
        backgroundScope.launch { vm.toasts.collect { messages.add(it) } }
        vm.onShareApp()
        advanceUntilIdle()
        runCurrent()
        assertFalse(vm.uiState.value.isSharingApk)
        assertTrue(messages.isEmpty())
    }

    @Test fun `split APK share explains limitation`() = runTest {
        environment.share = { ApkShareOutcome.Shared(splitApks = true) }
        val vm = SettingsViewModel(environment)
        vm.onShareApp()
        advanceUntilIdle()
        assertEquals(SettingsViewModel.UiMessage.SPLIT_APK_NOTE, vm.toasts.first())
    }

    @Test fun `failed sharing shows failure and clears busy state`() = runTest {
        environment.share = { ApkShareOutcome.Failed }
        val vm = SettingsViewModel(environment)
        vm.onShareApp()
        advanceUntilIdle()
        assertEquals(SettingsViewModel.UiMessage.SHARE_FAILED, vm.toasts.first())
        assertFalse(vm.uiState.value.isSharingApk)
    }

    @Test fun `unexpected sharing exception allows retry`() = runTest {
        environment.share = { throw IllegalStateException("No share handler") }
        val vm = SettingsViewModel(environment)
        vm.onShareApp()
        advanceUntilIdle()
        assertEquals(SettingsViewModel.UiMessage.SHARE_FAILED, vm.toasts.first())
        assertFalse(vm.uiState.value.isSharingApk)
        environment.share = { ApkShareOutcome.Shared(splitApks = false) }
        vm.onShareApp()
        advanceUntilIdle()
        assertEquals(2, environment.shareCalls)
        assertFalse(vm.uiState.value.isSharingApk)
    }

    @Test fun `cancellation clears busy state without an error toast`() = runTest {
        environment.share = { throw CancellationException("Screen closed") }
        val vm = SettingsViewModel(environment)
        val messages = mutableListOf<SettingsViewModel.UiMessage>()
        backgroundScope.launch { vm.toasts.collect { messages.add(it) } }
        vm.onShareApp()
        advanceUntilIdle()
        runCurrent()
        assertFalse(vm.uiState.value.isSharingApk)
        assertTrue(messages.isEmpty())
    }
}

private class FakeSettingsEnvironment : SettingsEnvironment {
    var theme = ThemeMode.SYSTEM
    var language = ""
    var analytics = false
    var notifications = true
    var languageWrites = 0
    var shareCalls = 0
    var share: suspend () -> ApkShareOutcome = { ApkShareOutcome.Shared(splitApks = false) }
    override fun themeMode() = theme
    override fun languageTag() = language
    override fun analyticsEnabled() = analytics
    override fun notificationsEnabled() = notifications
    override fun setThemeMode(mode: ThemeMode) { theme = mode }
    override fun setLanguage(tag: String): String {
        languageWrites++
        language = tag.substringBefore('-')
        return language
    }
    override fun setAnalyticsEnabled(enabled: Boolean) { analytics = enabled }
    override suspend fun shareApk(): ApkShareOutcome {
        shareCalls++
        return share()
    }
}
