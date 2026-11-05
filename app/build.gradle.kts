plugins {
    id("com.android.application")
}
android {
    namespace = "com.braininking.watweaks"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.braininking.watweaks"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}
