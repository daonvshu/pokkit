plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

group = "com.daonvshu"
version = "1.0-SNAPSHOT"

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.components.resources)
                implementation(project(":shared"))
                //jetbrains
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta01")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
                //3rd
                implementation("com.squareup.retrofit2:retrofit:3.0.0")
                implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")
                implementation("com.squareup.retrofit2:converter-simplexml:3.0.0")
                implementation("com.squareup.okhttp3:okhttp:4.11.0")
                implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
                implementation("org.jsoup:jsoup:1.20.1")
                implementation("io.github.mataku:middle-ellipsis-text:1.2.0")
                implementation("cafe.adriel.bonsai:bonsai-core:1.2.0")
                implementation("cafe.adriel.bonsai:bonsai-file-system:1.2.0")
                implementation("cafe.adriel.bonsai:bonsai-json:1.2.0")
                implementation("io.coil-kt.coil3:coil-compose:3.3.0")
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
            }
        }
    }
}