package com.example.androidstarter.screens.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidstarter.R
import com.example.androidstarter.application.appearance.AppearanceStore
import com.example.androidstarter.application.appearance.LocaleHelper
import com.example.androidstarter.application.privacy.AnalyticsConsent
import com.example.androidstarter.ui.components.SectionCard

@Composable
fun PermissionRoute(onFinished: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val firstRun = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getBoolean("is_first_run", true) }
    val draft = remember { OnboardingDraftStore(context) }
    var state by remember { mutableStateOf(draft.read(OnboardingState())) }
    val theme by AppearanceStore.themeMode.collectAsStateWithLifecycle()
    fun change(next: OnboardingState) {
        state = next
        draft.save(next, AppearanceStore.languageTag(context))
    }
    if (firstRun && state.step == OnboardingState.Step.WELCOME) {
        AppearanceStepScreen(theme, AppearanceStore.languageTag(context),
            onThemeChange = { AppearanceStore.setThemeMode(context, it) },
            onLanguageChange = {
                draft.save(state, it, commit = true)
                AppearanceStore.setLanguage(context, it)
                LocaleHelper.restartApp(context)
            }, onContinue = { change(state.continueWelcome()) })
    } else if (firstRun && state.step == OnboardingState.Step.TERMS) {
        AgreementStepScreen(state.agreed, { change(state.copy(agreed = it)) },
            onContinue = { change(state.continueTerms()) }, analytics = state.analytics,
            onAnalyticsChange = { change(state.copy(analytics = it)) }, onBack = { change(state.back()) })
    } else {
        NotificationsStep(onBack = { if (firstRun) change(state.back()) else onBack() }, onDone = {
            if (!firstRun || state.canFinish()) {
                if (firstRun) {
                    AnalyticsConsent.setEnabled(context, state.analytics)
                    AppearanceStore.setChosen(context)
                    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit(commit = true) {
                        putBoolean("is_first_run", false)
                    }
                    draft.clear()
                }
                onFinished()
            }
        })
    }
}

@Composable
private fun NotificationsStep(onBack: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        granted = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    OnboardingScaffold(step = 2, title = stringResource(R.string.starter_notifications),
        subtitle = stringResource(R.string.starter_optional), onBack = onBack,
        footer = { OnboardingFooterButton(stringResource(R.string.starter_done), onDone) }) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionCard {
                Text(stringResource(R.string.starter_notifications), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.starter_notification_explanation))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(if (granted) R.string.starter_allowed else R.string.starter_not_allowed))
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT >= 33 && !granted) request.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else runCatching { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }
                }) { Text(stringResource(R.string.starter_notification_settings)) }
            }
        }
    }
}
