Plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.offlinemessage.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.offlinemessage.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
Dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
}
