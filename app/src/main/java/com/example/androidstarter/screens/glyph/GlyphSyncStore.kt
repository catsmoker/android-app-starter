package com.example.androidstarter.screens.glyph

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class GlyphSyncStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, true)


    fun setEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_ENABLED, enabled) }
    }

    private companion object {
        const val PREFS_NAME = "glyph_prefs"
        const val KEY_ENABLED = "enabled"
    }
}
