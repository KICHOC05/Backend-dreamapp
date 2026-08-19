plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
}

val dreamAppApiUrl = providers.gradleProperty("DREAMAPP_API_URL")
    .orElse(providers.environmentVariable("DREAMAPP_API_URL"))
    .orElse("https://example.invalid/").get().trimEnd('/') + "/"

android {
    namespace = "com.example.appmobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appmobile"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Habilitar Database Inspector
        buildConfigField("boolean", "DEBUG_DATABASE", "false")
        buildConfigField("String", "API_BASE_URL", "\"$dreamAppApiUrl\"")
        buildConfigField("String", "WS_BASE_URL", "\"${dreamAppApiUrl.replace("https://", "wss://").replace("http://", "ws://").trimEnd('/')}\"")
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.wearable)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.androidx.material.icons.extended.v178)
    // implementation(libs.androidx.ui.desktop)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    // Debug Tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.coil.compose)
    implementation("com.google.code.gson:gson:2.10.1")
    // Room (Database)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    // Retrofit & Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp Logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Retrofit para API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Lifecycle ViewModel (Compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")

    // WebSocket support
    implementation("org.java-websocket:Java-WebSocket:1.5.3")

    // Conectar con el módulo wear
    wearApp(project(":wear"))
}

kapt {
    correctErrorTypes = true
}
