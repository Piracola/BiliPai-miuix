// 根目录 build.gradle.kts
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Kotlin 2.4 metadata 格式较新，升级 R8 以兼容
        classpath("com.android.tools:r8:9.1.31")
    }
}

plugins {
    // 1. Android 插件 (版本号要固定)
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("com.android.test") version "8.13.2" apply false
    id("androidx.baselineprofile") version "1.4.1" apply false

    id("com.google.devtools.ksp") version "2.3.10" apply false

    // 2. Kotlin 全家桶
    id("org.jetbrains.kotlin.android") version "2.4.0" apply false
    // Compose 编译器插件
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0" apply false
    // 序列化插件
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0" apply false
    
    // 3. Firebase 相关插件
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
}
