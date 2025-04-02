val teleresoKmpCatalog: String by settings
val localProperties = loadLocalProperties()

pluginManagement {
    fun loadLocalProperties() = java.util.Properties().apply { File(rootDir, "local.properties").takeIf { it.exists() }?.inputStream()?.use(::load) }
    val localProperties = loadLocalProperties()
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/edna-aa/sqldelight")
            credentials {
                username = localProperties.getProperty("gihtub.username")
                password = localProperties.getProperty("gihtub.pat")
            }
            // Restrict this repository to specific versions containing "-wasm"
            content {
                includeGroup("app.cash.sqldelight") // Restrict to the group
                includeVersionByRegex("app.cash.sqldelight", ".*", ".*-wasm.*") // Match any artifact in the group with versions containing "-wasm"
            }
        }

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
        maven {
            url = uri("https://maven.pkg.github.com/edna-aa/sqldelight")
            credentials {
                username = localProperties.getProperty("gihtub.username")
                password = localProperties.getProperty("gihtub.pat")
            }
            // Restrict this repository to specific versions containing "-wasm"
            content {
                includeGroup("app.cash.sqldelight") // Restrict to the group
                includeVersionByRegex("app.cash.sqldelight", ".*", ".*-wasm.*") // Match any artifact in the group with versions containing "-wasm"
            }
        }

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

fun loadLocalProperties() = java.util.Properties().apply { File(rootDir, "local.properties").takeIf { it.exists() }?.inputStream()?.use(::load) }
