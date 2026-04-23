import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.services)

    id("com.google.dagger.hilt.android")
}

// Puente para que el proyecto pueda leer y cargar el archivo local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.gamevault"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.gamevault"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Centralizar todas las claves leyendo del local.properties
        buildConfigField("String", "IGDB_CLIENT_ID", "\"${localProperties.getProperty("IGDB_CLIENT_ID", "")}\"")
        buildConfigField("String", "IGDB_ACCESS_TOKEN", "\"${localProperties.getProperty("IGDB_ACCESS_TOKEN", "")}\"")
        buildConfigField("String", "STEAM_API_KEY", "\"${localProperties.getProperty("STEAM_API_KEY", "")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
        }
        getByName("release") {
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // DEPENDENCIAS GAMEVAULT

    // Navegación en Compose
    implementation(libs.androidx.navigation.compose)

    // Retrofit para llamar a las APIs de IGDB y Steam
    implementation(libs.retrofit2.retrofit)
    implementation(libs.converter.gson)

    // Room (Base de datos local)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coil (Para cargar las portadas de los juegos desde URLs)
    implementation(libs.coil.compose)

    // Hilt (Inyección de dependencias)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // ViewModel para Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // OkHttp (Para las peticiones HTTP)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    // Google authenticator
    implementation(libs.play.services.auth)
}