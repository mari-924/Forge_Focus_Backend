pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.google.com") } // ✅ add Google Maven repo here too
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT) // ✅ prefer project repos (not override them)
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.google.com") }   // ✅ add Google Maven repo again
    }
}

rootProject.name = "Forge_Focus_Backend"