package com.example.androidstarter.application.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.androidstarter.screens.about.AboutRoute
import com.example.androidstarter.screens.home.HomeScreen
import com.example.androidstarter.screens.glyph.GlyphRoute
import com.example.androidstarter.screens.legal.*
import com.example.androidstarter.screens.logs.LogsRoute
import com.example.androidstarter.screens.permissions.PermissionRoute
import com.example.androidstarter.screens.settings.SettingsRoute
import com.example.androidstarter.screens.support.SupportScreen

object Routes {
    const val MAIN = "home"
    const val PERMISSION = "onboarding"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val DONATE = "support"
    const val GLYPH = "glyph"
    const val LOGS = "logs"
    const val PRIVACY = "privacy"
    const val TERMS = "terms"
    const val LICENSES = "licenses"
}

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String) {
    val back: () -> Unit = { navController.popBackStack() }
    val open: (String) -> Unit = { navController.navigate(it) { launchSingleTop = true } }
    NavHost(navController, startDestination) {
        composable(Routes.MAIN) { HomeScreen(open) }
        composable(Routes.PERMISSION) {
            PermissionRoute(onFinished = {
                // The activity may still have its original onboarding start destination
                // after first-run completion. Inspect the current stack, not that stale value.
                if (navController.previousBackStackEntry == null) navController.navigate(Routes.MAIN) {
                    popUpTo(Routes.PERMISSION) { inclusive = true }
                } else back()
            }, onBack = back)
        }
        composable(Routes.SETTINGS) { SettingsRoute(back, { open(Routes.PERMISSION) }, { open(Routes.LOGS) }, { open(Routes.DONATE) }, { open(Routes.ABOUT) }) }
        composable(Routes.ABOUT) { AboutRoute(back, { open(Routes.DONATE) }, { open(Routes.PRIVACY) }, { open(Routes.TERMS) }, { open(Routes.LICENSES) }) }
        composable(Routes.DONATE) { SupportScreen(back) }
        composable(Routes.GLYPH) { GlyphRoute(back) }
        composable(Routes.LOGS) { LogsRoute(back) }
        composable(Routes.PRIVACY) { PrivacyPolicyRoute(back) }
        composable(Routes.TERMS) { TermsRoute(back) }
        composable(Routes.LICENSES) { LicensesRoute(back) }
    }
}
