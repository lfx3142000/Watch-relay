plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val ciVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
val ciVersionName = System.getenv("VERSION_NAME") ?: "0.1.0-dev"
val relayDebugKeystorePath = System.getenv("RELAY_DEBUG_KEYSTORE_PATH")

android {
    namespace = "com.example.chatgptwatchrelay"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chatgptwatchrelay"
        minSdk = 26
        targetSdk = 35
        versionCode = ciVersionCode
        versionName = ciVersionName
    }

    signingConfigs {
        if (!relayDebugKeystorePath.isNullOrBlank()) {
            create("relayCiDebug") {
                storeFile = file(relayDebugKeystorePath)
                storePassword = System.getenv("RELAY_DEBUG_KEYSTORE_PASSWORD") ?: "relayupdate"
                keyAlias = System.getenv("RELAY_DEBUG_KEY_ALIAS") ?: "relayupdate"
                keyPassword = System.getenv("RELAY_DEBUG_KEY_PASSWORD") ?: "relayupdate"
            }
        }
    }

    buildTypes {
        getByName("debug") {
            if (!relayDebugKeystorePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("relayCiDebug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}
