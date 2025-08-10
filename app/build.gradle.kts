import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
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
                implementation(project(":bangumi"))
                //jetbrains
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta01")
                //3rd
                implementation("io.coil-kt.coil3:coil-compose:3.3.0")
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.daonvshu.pokkit.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "pokkit"
            packageVersion = "1.0.0"
        }
    }
}