import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    kotlin("plugin.serialization") version "2.1.20"
    id("org.jetbrains.compose")
    id("app.cash.sqldelight") version "2.1.0"
}

group = "com.daonvshu"
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
                //jetbrains
                implementation("org.jetbrains.compose.material:material-icons-core:1.6.11")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
                //3rd
                implementation("app.cash.sqldelight:sqlite-driver:2.1.0")
                implementation("com.squareup.okhttp3:okhttp:4.11.0")
            }
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName = "com.daonvshu.shared.database"
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.1.0")
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
}