plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    kotlin("kapt")
}

// Configuración de Hilt para deshabilitar agregación que causa conflicto con javapoet
hilt {
    enableAggregatingTask = false
}

android {
    namespace = "com.example.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        // languageVersion removido - usar versión por defecto de Kotlin 2.0.21
    }

    buildFeatures {
        compose = true
    }
}


dependencies {
    // Compose BOM: usa versiones gestionadas por el BOM para artefactos Compose
    implementation(platform(libs.androidx.compose.bom))

    // Core / lifecycle base (desde tu catálogo)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // javapoet - no declarar explícitamente, Hilt lo traerá como dependencia transitiva
    // Activity + Compose (una sola vez)
    implementation(libs.androidx.activity.compose)

    // Compose UI (sin versiones explícitas gracias al BOM)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Material 3 (una sola línea; quita duplicados y <latest_version>)
    implementation(libs.androidx.material3)

    // ViewModel Compose (elige UNA versión; me quedo con 2.6.2 que ya usas)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Navigation Compose (una sola versión, la más reciente que tienes)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Material icons (una sola)
    implementation("androidx.compose.material:material-icons-extended")

    // Runtime LiveData (si realmente la usas; deja una vez y sin versión por BOM)
    implementation("androidx.compose.runtime:runtime-livedata")

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Retrofit (elige 2.10.0 y elimina 2.9.0)
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")

    // OkHttp (elige 4.12.0 y elimina 4.9.3)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines (elige 1.8.1 y elimina 1.7.3)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Coil (ok)
    implementation("io.coil-kt:coil-compose:2.2.2")

    // Supabase (ok; si no lo usas aún, puedes quitarlo)
    implementation("io.github.jan-tennert.supabase:supabase-kt:2.1.5")

    // QR (ok)
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}
