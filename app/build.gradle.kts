import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
}
val identity = Properties().apply { rootProject.file("starter.properties").inputStream().use { load(it) } }
val services = Properties().apply {
    val file = rootProject.file("services.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val firebaseConfigured = file("google-services.json").exists()
if (firebaseConfigured) apply(plugin = "com.google.gms.google-services")
fun quoted(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.example.androidstarter"
    compileSdk = 37
    defaultConfig {
        applicationId = identity.getProperty("applicationId")
        minSdk = 27
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
        resValue("string", "app_name", identity.getProperty("appName"))
        manifestPlaceholders["NOTHING_API_KEY"] = services.getProperty("NOTHING_API_KEY", "test")
        buildConfigField("boolean", "FIREBASE_CONFIGURED", firebaseConfigured.toString())
    }
    buildTypes {
        debug {
            manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_BANNER_ID", quoted("ca-app-pub-3940256099942544/9214589741"))
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            manifestPlaceholders["ADMOB_APP_ID"] = services.getProperty("ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")
            buildConfigField("String", "ADMOB_BANNER_ID", quoted(services.getProperty("ADMOB_BANNER_ID", "ca-app-pub-3940256099942544/9214589741")))
            // No signing material is supplied or silently replaced with a debug key.
        }
    }
    buildFeatures { compose = true; buildConfig = true; resValues = true }
    sourceSets.getByName("main").assets.srcDir(rootProject.file("notices"))
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    androidResources { localeFilters += setOf("en", "en-rGB", "ar", "es", "ja", "zh-rCN", "pt-rBR", "fr") }
}
dependencies {
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.material)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.work.runtime.ktx)
    implementation(libs.play.services.ads)
    implementation(libs.ump)
    implementation(libs.billing.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(files("libs/glyph-matrix-sdk-2.0.aar"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.compose.ui.tooling)
}
