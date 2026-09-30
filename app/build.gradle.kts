plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "bo.dolar.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "bo.dolar.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Única dependencia: WorkManager para la consulta diaria de las 19:00
    implementation("androidx.work:work-runtime-ktx:2.9.1")
}
