package com.example.androidstarter.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidstarter.application.appearance.AppearanceStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Owns screen state; Android persistence and sharing live behind [SettingsEnvironment]. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val environment: SettingsEnvironment
) : ViewModel() {
    data class UiState(
        val themeMode: AppearanceStore.ThemeMode = AppearanceStore.ThemeMode.SYSTEM,
        val languageTag: String = AppearanceStore.LANGUAGE_SYSTEM,
        val analyticsEnabled: Boolean = false,
        val notificationsEnabled: Boolean = true,
        val isSharingApk: Boolean = false
    )

    sealed interface UiEvent {
        data object RestartApp : UiEvent
    }

    enum class UiMessage { SPLIT_APK_NOTE, SHARE_FAILED }

    private val _uiState = MutableStateFlow(UiState(
        themeMode = environment.themeMode(),
        languageTag = environment.languageTag(),
        analyticsEnabled = environment.analyticsEnabled(),
        notificationsEnabled = environment.notificationsEnabled()
    ))
    val uiState = _uiState.asStateFlow()
    private val messages = Channel<UiMessage>(Channel.BUFFERED)
    val toasts = messages.receiveAsFlow()
    // A temporarily absent collector must not lose a restart after the language is saved.
    private val pendingEvents = Channel<UiEvent>(Channel.BUFFERED)
    val events = pendingEvents.receiveAsFlow()

    fun refreshNotifications() {
        _uiState.update { it.copy(
            notificationsEnabled = environment.notificationsEnabled()
        ) }
    }

    fun onThemeModeChanged(mode: AppearanceStore.ThemeMode) {
        environment.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun onLanguageChanged(tag: String) {
        if (tag == _uiState.value.languageTag) return
        val persistedTag = environment.setLanguage(tag)
        if (persistedTag == _uiState.value.languageTag) return
        _uiState.update { it.copy(languageTag = persistedTag) }
        // Engine/application-context strings also need to resolve the new locale.
        pendingEvents.trySend(UiEvent.RestartApp)
    }

    fun onAnalyticsToggled(enabled: Boolean) {
        environment.setAnalyticsEnabled(enabled)
        _uiState.update { it.copy(analyticsEnabled = enabled) }
    }

    /** Shares the installed APK, not an update downloaded from another distribution. */
    fun onShareApp() {
        if (_uiState.value.isSharingApk) return
        _uiState.update { it.copy(isSharingApk = true) }
        viewModelScope.launch {
            try {
                when (val outcome = environment.shareApk()) {
                    is ApkShareOutcome.Shared -> if (outcome.splitApks) {
                        messages.trySend(UiMessage.SPLIT_APK_NOTE)
                    }
                    ApkShareOutcome.Failed -> messages.trySend(UiMessage.SHARE_FAILED)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                messages.trySend(UiMessage.SHARE_FAILED)
            } finally {
                _uiState.update { it.copy(isSharingApk = false) }
            }
        }
    }
}
