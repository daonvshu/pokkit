pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "pokkit"

include(":app")
include(":bangumi")
include(":pixiv")
include(":shared")