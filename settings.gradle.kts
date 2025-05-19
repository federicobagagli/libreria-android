pluginManagement {
    repositories {
        google() // ✅ Qui basta che sia presente **senza filtri** per funzionare
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LibraryApp"
include(":app")
