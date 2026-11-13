package com.example.androidstarter.integrations.ads

import com.example.androidstarter.BuildConfig

import android.view.View
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.androidstarter.integrations.ads.AdConsentManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Consent-gated, anchored adaptive AdMob banner.
 * The slot is reserved up front so a successful ad never shifts content, but it
 * collapses when no ad unit is configured or no ad arrives (offline, no fill),
 * or the gap would stay forever. The consent manager also enforces purchase entitlement.
 *
 * A throwing SDK call degrades to the same collapse: `getCurrentOrientation...`
 * and the [AdView] factory are guarded so an SDK-side failure (bad WebView,
 * R8-shrunk SDK types, missing application ID) hides the slot instead of
 * crashing the host activity — the banner must never take the app down.
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) return
    val consentReady by AdConsentManager.canLoad.collectAsStateWithLifecycle()
    if (!consentReady) return

    val adUnitId = BuildConfig.ADMOB_BANNER_ID
    if (adUnitId.isEmpty()) return

    var loadFailed by rememberSaveable { mutableStateOf(false) }
    if (loadFailed) return

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var currentAd by remember { mutableStateOf<AdView?>(null) }
    DisposableEffect(lifecycle, currentAd) {
        val ad = currentAd
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> ad?.resume()
                Lifecycle.Event.ON_PAUSE -> ad?.pause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) ad?.pause()
        onDispose { lifecycle.removeObserver(observer) }
    }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt()
        // Anchored adaptive size for the current width — the Play-recommended banner sizing.
        // Null when the SDK itself throws: the slot below collapses like a no-fill.
        val adSize = try {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        } catch (_: Exception) {
            null
        }
        if (adSize == null) return@BoxWithConstraints

        key(widthDp, adUnitId) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(adSize.height.dp),
                factory = { ctx ->
                    try {
                        AdView(ctx).apply {
                            currentAd = this
                            setAdSize(adSize)
                            setAdUnitId(adUnitId)
                            adListener = object : AdListener() {
                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    loadFailed = true
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    } catch (_: Exception) {
                        // SDK blew up while building the view — collapse the slot on recompose.
                        loadFailed = true
                        View(ctx)
                    }
                },
                onRelease = { (it as? AdView)?.destroy() }
            )
        }
    }
}
