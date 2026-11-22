plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// TOTP kalit: GitHub Secret (TOTP_SECRET) yoki muhit o'zgaruvchisidan olinadi.
// Bo'lmasa quyidagi default kalit ishlatiladi.
val totpSecret: String = System.getenv("TOTP_SECRET")?.trim()?.takeIf { it.isNotEmpty() }
    ?: "HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ"

android {
    namespace = "uz.ilhomvich.iptv"
    compileSdk = 34

    defaultConfig {
        applicationId = "uz.ilhomvich.iptv"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "TOTP_SECRET", "\"$totpSecret\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
}
