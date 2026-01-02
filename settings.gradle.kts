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
                username = "mapbox"
                // Replace with your secret downloads token (starts with sk.)
                password =
                    "sk.eyJ1IjoiemltYWwyMiIsImEiOiJjbWpoNWxobTUxMTh5M2RzODdpdGY4aGxlIn0.NX3BESzl4etWCHyMF2D7TQ"

            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

rootProject.name = "Viaterra"
include(":app")
