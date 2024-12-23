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
//            version("teleresoKmp", "0.0.1-local")
        }
    }
}

rootProject.name = "core"
include(":lib")
project(":lib").name = rootProject.name
include(":core-test")
include(":core-ui")
include(":core-ui-test")
include(":core-icons")
include(":core-preview")
include(":catalog")
include(":androidApp")
include(":jvmApi")

// This is used while working with code generation project `kmp-annotations` (https://github.com/telereso/kmp-annotations)
val localProps = java.util.Properties().apply {
    File("${rootDir}/local.properties").apply {
        if (exists())
            inputStream().use { fis ->
                load(fis)
            }
    }
}

val teleresoKmpDevelopmentPath = localProps["teleresoKmpDevelopmentPath"] as String?

if (!teleresoKmpDevelopmentPath.isNullOrEmpty()) {
    include(":annotations")
    project(":annotations").projectDir =
        file("$teleresoKmpDevelopmentPath/annotations")

    include(":processor")
    project(":processor").projectDir =
        file("$teleresoKmpDevelopmentPath/processor")

    includeBuild("$teleresoKmpDevelopmentPath/convention-plugins")
    includeBuild("$teleresoKmpDevelopmentPath/gradle-plugin")
} else {
    includeBuild("convention-plugins")
}
