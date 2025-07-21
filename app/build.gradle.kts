import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // 👈 agrégalo aquí
}

android {
    namespace = "com.dutisoft.places"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dutisoft.places"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Token desde local.properties
        val mapboxToken = project.findProperty("MAPBOX_TOKEN")
            ?: Properties().apply {
                load(File(rootDir, "local.properties").inputStream())
            }.getProperty("MAPBOX_TOKEN")


        buildConfigField("String", "MAPBOX_TOKEN", "\"$mapboxToken\"")

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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.android)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.code.gson:gson:2.10.1") // o la versión que uses

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.activity)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}