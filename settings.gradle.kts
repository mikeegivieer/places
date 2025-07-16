pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = providers.gradleProperty("MAPBOX_USERNAME").get()
                password = providers.gradleProperty("MAPBOX_TOKEN").get()
            }
            content {
                includeGroupByRegex("com\\.mapbox.*") // 🔥 Importante para permitir TODO Mapbox
            }
        }
    }
}


rootProject.name = "Places"
include(":app")
 