plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.mykasir"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mykasir"
        minSdk = 24
        targetSdk = 36
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
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // ✅ Library utama Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("androidx.compose.material:material-icons-extended")

    // Navigasi (Sudah ada)
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Coil (Sudah ada)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // --- TAMBAHAN BARU DI SINI ---

    // 1. ViewModel
    // Dibutuhkan untuk `viewModel()` dan `lifecycle-viewmodel-ktx`
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // 2. Retrofit (Untuk koneksi API)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // 3. Gson Converter (Untuk mengubah JSON -> Kotlin)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 4. OkHttp (Dibutuhkan oleh Retrofit)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")


    // ✅ Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}