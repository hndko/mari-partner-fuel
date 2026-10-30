import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Loader konfigurasi lingkungan otomatis dari .env & local.properties
val envProperties = Properties()

// 1. Baca dari local.properties di root jika tersedia
val rootLocalPropsFile = rootProject.file("local.properties")
if (rootLocalPropsFile.exists()) {
    val lp = Properties()
    rootLocalPropsFile.inputStream().use { lp.load(it) }
    lp.forEach { (k, v) ->
        val str = v.toString().trim()
        if (str.isNotBlank()) {
            envProperties.setProperty(k.toString(), str)
        }
    }
}

// 2. Baca dari berkas .env di root jika tersedia (prioritas utama developer)
val rootEnvFile = rootProject.file(".env")
if (rootEnvFile.exists()) {
    rootEnvFile.forEachLine { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
            val key = trimmed.substringBefore("=").trim()
            val value = trimmed.substringAfter("=").trim().removeSurrounding("\"").removeSurrounding("'")
            if (value.isNotBlank()) {
                envProperties.setProperty(key, value)
            }
        }
    }
}

fun getEnvOrProp(key: String, default: String = ""): String {
    return envProperties.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: (project.findProperty(key) as? String)?.takeIf { it.isNotBlank() }
        ?: System.getenv(key)?.takeIf { it.isNotBlank() }
        ?: default
}

android {
    namespace = "id.maripartner.fuel"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.maripartner.fuel"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "1.7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Environment values injected safely via .env, local.properties, or environment variables
        val supabaseUrl = getEnvOrProp("SUPABASE_URL", "https://placeholder-project.supabase.co")
        val supabaseAnonKey = getEnvOrProp("SUPABASE_ANON_KEY", "placeholder-anon-key")
        val googleWebClientId = getEnvOrProp("GOOGLE_WEB_CLIENT_ID", "")

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = getEnvOrProp("RELEASE_STORE_FILE")
            if (storeFilePath.isNotBlank() && file(storeFilePath).exists()) {
                storeFile = file(storeFilePath)
                storePassword = getEnvOrProp("RELEASE_STORE_PASSWORD")
                keyAlias = getEnvOrProp("RELEASE_KEY_ALIAS")
                keyPassword = getEnvOrProp("RELEASE_KEY_PASSWORD")
            } else {
                // Fallback aman ke debug keystore untuk pengujian Release Candidate lokal/CI
                val debugKeystore = getByName("debug")
                storeFile = debugKeystore.storeFile
                storePassword = debugKeystore.storePassword
                keyAlias = debugKeystore.keyAlias
                keyPassword = debugKeystore.keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Supabase SDK & Ktor Client
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.functions)
    implementation(libs.ktor.client.okhttp)

    // Google ML Kit Text Recognition (Local OCR)
    implementation(libs.mlkit.text.recognition)

    // Google Sign-In & Credential Manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Google Play Billing System
    implementation(libs.play.billing.ktx)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
