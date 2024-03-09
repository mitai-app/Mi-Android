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
        //maven { url= URI.create("https://jitpack.io" ) }
        //maven { url= URI.create("https://oss.sonatype.org/content/repositories/snapshots" ) }
        //maven { url= URI.create("https://oss.jfrog.org/artifactory/oss-snapshot-local" ) }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //jcenter() // Warning: this repository is going to shut down soon
    }
}
rootProject.name = "Mi"
include(":app")