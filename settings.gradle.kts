pluginManagement {
    repositories {
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/gradle-plugin")
        }
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
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/public")
        }
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/apache-snapshots")
        }
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/central")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "EsNote"
include(":app")
