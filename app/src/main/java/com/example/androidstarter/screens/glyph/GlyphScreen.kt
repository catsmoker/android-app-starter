package com.example.androidstarter.screens.glyph

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidstarter.R
import com.example.androidstarter.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*

@HiltViewModel
class GlyphViewModel @Inject constructor(val lights: GlyphController, val preferences: GlyphSyncStore) : ViewModel() {
    override fun onCleared() { lights.release() }
}

@Composable
fun GlyphRoute(onBack: () -> Unit) {
    val model: GlyphViewModel = hiltViewModel()
    val lights = model.lights
    val status by lights.status.collectAsStateWithLifecycle()
    var enabled by remember { mutableStateOf(model.preferences.isEnabled()) }
    var progress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    var animation by remember { mutableStateOf<Job?>(null) }
    fun stop() { animation?.cancel(); animation = null; lights.cancel() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { stop() }
    DisposableEffect(lights) { onDispose { animation?.cancel(); lights.release() } }
    ScreenScaffold(stringResource(R.string.starter_glyph_title), onBack = onBack) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionCard {
                Text(stringResource(if (lights.isMatrix) R.string.starter_matrix_heading else R.string.starter_glyph_title), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(if (lights.isSupported) R.string.starter_glyph_supported else R.string.starter_glyph_unsupported))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(when (status) {
                    GlyphController.Status.IDLE -> R.string.starter_glyph_idle
                    GlyphController.Status.CONNECTING -> R.string.starter_glyph_connecting
                    GlyphController.Status.ACTIVE -> R.string.starter_glyph_active
                    GlyphController.Status.ERROR -> R.string.starter_glyph_error
                }), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (lights.isSupported) {
                SectionCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.starter_glyph_enabled), modifier = Modifier.weight(1f))
                        Switch(enabled, { enabled = it; model.preferences.setEnabled(it); if (!it) stop() })
                    }
                    Text(stringResource(R.string.starter_glyph_percent, progress.toInt()))
                    Slider(value = progress, onValueChange = {
                        animation?.cancel(); animation = null; progress = it
                        lights.start(); lights.setProgress(it.toInt())
                    }, valueRange = 0f..100f, enabled = enabled)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(enabled = enabled, onClick = {
                            stop()
                            animation = scope.launch {
                                lights.start()
                                for (p in 0..100 step 2) { progress = p.toFloat(); lights.setProgress(p); delay(60) }
                                lights.complete(); animation = null
                            }
                        }) { Text(stringResource(R.string.starter_glyph_fill)) }
                        OutlinedButton(enabled = enabled, onClick = { stop(); lights.startReverse() }) { Text(stringResource(R.string.starter_glyph_drain)) }
                        TextButton(onClick = { stop() }) { Text(stringResource(R.string.starter_glyph_stop)) }
                    }
                    Text(stringResource(R.string.starter_glyph_configuration), style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
