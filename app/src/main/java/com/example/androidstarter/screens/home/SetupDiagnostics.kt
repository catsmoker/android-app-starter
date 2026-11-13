package com.example.androidstarter.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidstarter.BuildConfig
import com.example.androidstarter.R
import com.example.androidstarter.integrations.ads.AdConsentManager
import com.example.androidstarter.ui.components.SectionCard

@Composable
fun SetupDiagnostics() {
    val adStatus by AdConsentManager.diagnostic.collectAsStateWithLifecycle()
    SectionCard {
        Text(stringResource(R.string.starter_setup_title), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(if (BuildConfig.DEBUG) R.string.starter_debug_services else if (!BuildConfig.FIREBASE_CONFIGURED) R.string.starter_missing_firebase else R.string.starter_configured_firebase), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(adStatus), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.starter_setup_instructions), style = MaterialTheme.typography.bodySmall)
    }
}
