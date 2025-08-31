import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

group = "com.daonvshu"
version = "3.0.0"

val generateBuildConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/buildConfig")
    inputs.property("version", version)
    outputs.dir(outputDir)

    doLast {
        val packageName = "com.daonvshu"
        val file = outputDir.get().dir(packageName.replace(".", "/"))
            .file("BuildConfig.kt").asFile

        file.parentFile.mkdirs()
        file.writeText(
            """
            package $packageName

            object BuildConfig {
                const val VERSION = "$version"
            }
            """.trimIndent()
        )
    }
}

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
                implementation("io.coil-kt.coil3:coil-compose:3.3.0")
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
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
                implementation("org.jetbrains.exposed:exposed-core:1.0.0-beta-2")
                implementation("org.jetbrains.exposed:exposed-crypt:1.0.0-beta-2")
                implementation("org.jetbrains.exposed:exposed-dao:1.0.0-beta-2")
                implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0-beta-2")
                implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.0.0-beta-2")
                implementation("org.jetbrains.exposed:exposed-json:1.0.0-beta-2")
                implementation("org.xerial:sqlite-jdbc:3.44.1.0")
                implementation(files("src/libs/ProtocolCodecEngine-1.0.2.jar"))
            }

            kotlin.srcDir(generateBuildConfig.map { it.outputs.files })
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.daonvshu.pokkit.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "pokkit"
            packageVersion = version.toString()
            outputBaseDir = rootProject.layout.projectDirectory.dir("build")

            buildTypes.release.proguard {
                configurationFiles.from(project.file("proguard-rules.pro"))
            }

            modules("java.sql")
            windows {
                //console = true
                iconFile.set(project.file("../shared/src/commonMain/composeResources/drawable/logo.ico"))
            }

            jvmArgs += listOf(
                "--strip-native-commands",
                "--strip-native-debug-symbols"
            )
        }
    }
}

tasks.named("compileKotlinJvm") {
    dependsOn(generateBuildConfig)
}

tasks.matching {  it.name == "createReleaseDistributable"}.configureEach {
    doLast {
        //clear msvc runtime
        val runtimeDir = file(rootProject.layout.projectDirectory.dir("build/main-release/app/pokkit/runtime/bin"))
        runtimeDir.listFiles()?.forEach { f ->
            if (f.name.startsWith("msvcp") ||
                f.name.startsWith("vcruntime") ||
                f.name.startsWith("ucrtbase") ||
                f.name.startsWith("api-ms-win")) {
                println("Deleting bundled runtime dll: ${f.name}")
                f.delete()
            }
        }
        copy {
            from(rootProject.layout.projectDirectory.file("shared/src/commonMain/composeResources/drawable/logo.ico"))
            into(rootProject.layout.projectDirectory.dir("build/main-release/app/pokkit"))
        }
    }
}