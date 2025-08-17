import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    kotlin("plugin.serialization") version "2.1.20"
    id("org.jetbrains.compose")
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
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta01")
                //3rd
                implementation("com.squareup.okhttp3:okhttp:4.11.0")
                implementation("org.jetbrains.exposed:exposed-core:1.0.0-beta-2")
                implementation("org.jetbrains.exposed:exposed-crypt:1.0.0-beta-2")
                implementation("org.jetbrains.exposed:exposed-dao:1.0.0-beta-2")
                implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0-beta-2")
                implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.0.0-beta-2")
                implementation("org.jetbrains.exposed:exposed-json:1.0.0-beta-2")
                implementation("org.xerial:sqlite-jdbc:3.44.1.0")
                implementation("io.github.mataku:middle-ellipsis-text:1.2.0")

                implementation(files("src/libs/ProtocolCodecEngine-1.0.1.jar"))
            }
        }
    }
}

/*sqldelight {
    databases {
        create("AppDatabase") {
            packageName = "com.daonvshu.shared.database"
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:2.1.0")
        }
    }
}*/

compose.resources {
    publicResClass = true
    generateResClass = always
}