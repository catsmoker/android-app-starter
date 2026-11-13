package com.example.androidstarter.application.configuration

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.example.androidstarter.R

/** Reserved example domains must never look like a working developer contact. */
object StarterLinks {
    fun isConfigured(url: String): Boolean = url.isNotBlank() && !url.contains("example.com") && !url.contains("example.invalid")
    fun open(context: Context, url: String): Boolean {
        if (isConfigured(url)) {
            if (runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true }.getOrDefault(false)) return true
        }
        Toast.makeText(context, R.string.starter_link_setup, Toast.LENGTH_LONG).show()
        return false
    }
}
