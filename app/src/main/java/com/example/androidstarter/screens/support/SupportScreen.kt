package com.example.androidstarter.screens.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidstarter.R
import com.example.androidstarter.application.distribution.DistributionSupportAction
import com.example.androidstarter.application.configuration.StarterLinks
import com.example.androidstarter.screens.settings.DistributionAdsSettings
import com.example.androidstarter.ui.components.*

@Composable
fun SupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    ScreenScaffold(stringResource(R.string.donate_title), onBack = onBack) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionCard {
                Text(stringResource(R.string.starter_support_thanks), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.starter_support_summary))
            }
            SectionCard {
                Text(stringResource(R.string.sys_rate_app_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.starter_honest_rating))
                TextButton(onClick = { DistributionSupportAction.open(context) }) { Text(stringResource(R.string.sys_rate_app_title)) }
            }
            DistributionAdsSettings({})
            SectionCard {
                Text(stringResource(R.string.starter_contact_title), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { StarterLinks.open(context, context.getString(R.string.url_contact)) }) { Text(stringResource(R.string.starter_contact_title)) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
