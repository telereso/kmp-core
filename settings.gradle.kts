val teleresoKmpCatalog: String by settings

pluginManagement {
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven { url = uri("https://s01.oss.sonatype.org/content/groups/staging") }
    }
    versionCatalogs {
        create("kmpLibs") {
            from(files("catalog/kmpLibs.versions.toml"))
        }
    }
}

rootProject.name = "core"

include(":catalog")
include(":annotations")
include(":processor")
includeBuild("convention-plugins")

val publishGradlePlugin: String by settings

if (publishGradlePlugin.toBoolean()) {
    include("gradle-plugin")
} else {
    includeBuild("gradle-plugin")

    include(":lib")
    project(":lib").name = rootProject.name


    include(":core-icons")
    include(":core-ui")
    include(":core-preview")
    include(":core-test")
    include(":core-ui-test")

    include(":androidApp")
    include(":jvmApi")

}
