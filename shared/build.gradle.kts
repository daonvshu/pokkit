import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

group = "com.eant"
version = "1.0-SNAPSHOT"

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.components.resources)
                implementation("org.jetbrains.compose.material:material-icons-core:1.6.11")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }
        }

    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
}