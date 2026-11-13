package com.example.androidstarter.ui.components

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/** A bounded design-review set, not screenshot assertions or a device compatibility claim. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Preview(name = "Small phone", widthDp = 320, heightDp = 640, showBackground = true)
@Preview(name = "Dark phone", widthDp = 393, heightDp = 851, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Tablet", widthDp = 840, heightDp = 900, showBackground = true)
@Preview(name = "Arabic RTL", widthDp = 393, heightDp = 851, locale = "ar", showBackground = true)
@Preview(name = "Large text", widthDp = 393, heightDp = 851, fontScale = 2f, showBackground = true)
annotation class StarterPreviews
