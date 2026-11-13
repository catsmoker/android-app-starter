package com.example.androidstarter.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidstarter.R
import com.example.androidstarter.application.navigation.Routes
import com.example.androidstarter.integrations.ads.AdBanner
import com.example.androidstarter.ui.components.*

@Composable
fun HomeScreen(open: (String) -> Unit) {
    ScreenScaffold(stringResource(R.string.app_name), stringResource(R.string.starter_home_summary)) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionCard {
                    Text(stringResource(R.string.starter_welcome), style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.starter_home_intro), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HomeCard(Icons.Default.Apps, stringResource(R.string.starter_glyph_title), stringResource(R.string.starter_glyph_summary)) { open(Routes.GLYPH) }
                HomeCard(Icons.Default.Settings, stringResource(R.string.sys_settings_title), stringResource(R.string.starter_settings_summary)) { open(Routes.SETTINGS) }
                HomeCard(Icons.Default.Favorite, stringResource(R.string.donate_title), stringResource(R.string.starter_support_summary)) { open(Routes.DONATE) }
                HomeCard(Icons.Default.Info, stringResource(R.string.about_header_title, stringResource(R.string.app_name)), stringResource(R.string.starter_about_summary)) { open(Routes.ABOUT) }
                SetupDiagnostics()
                Spacer(Modifier.height(8.dp))
            }
        }
        // Separate from navigation controls: no deceptive placement or requests to click ads.
        Spacer(Modifier.height(16.dp))
        AdBanner()
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HomeCard(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    SectionCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(icon, null)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }
    }
}
