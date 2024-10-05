---
layout: default
title: Migrations
nav_order: 8
---


# Catalog 1.7 to 1.8

The major breaking change was upgrading [sqldelight](https://sqldelight.github.io/sqldelight/2.0.2/multiplatform_sqlite/) to `2.*` you can visit their [official docs](https://sqldelight.github.io/sqldelight/2.0.2/upgrading-2.0/) for detailed steps 

use latest catalog version or staring from `1.8`

---
### _settings.gradle.kts_

```diff
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("kmpLibs") { 
-            from("io.telereso.kmp:catalog:1.7")
+            from("io.telereso.kmp:catalog:1.8")
            // override versions
            // version("teleresoCore", "0.3.0")
        }
    }
}
```
---

### _build.gradle.kts_ (root project)
```diff

plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(kmpLibs.plugins.kotlin.android) apply false
    alias(kmpLibs.plugins.android.application) apply false
    alias(kmpLibs.plugins.android.library) apply false
    alias(kmpLibs.plugins.kotlin.jvm) apply false
    alias(kmpLibs.plugins.kotlin.multiplatform) apply false
    alias(kmpLibs.plugins.kotlin.serialization) apply false
    alias(kmpLibs.plugins.kotlin.parcelize) apply false
    alias(kmpLibs.plugins.kotlin.native.cocoapods) apply false
    alias(kmpLibs.plugins.buildkonfig) apply false
+    alias(kmpLibs.plugins.sqldelight) apply false
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
    dependencies {
-        classpath("com.squareup.sqldelight:gradle-plugin:${kmpLibs.versions.sqldelight.get()}")
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:latest.release")
    }
}


```
---

### _my-client/build.gradle.kts_ (module project)
```diff

plugins {
    alias(kmpLibs.plugins.android.library)
    alias(kmpLibs.plugins.kotlin.multiplatform)
    alias(kmpLibs.plugins.kotlin.native.cocoapods)
    alias(kmpLibs.plugins.kotlin.serialization)
    alias(kmpLibs.plugins.kotlin.parcelize)
    alias(kmpLibs.plugins.kotlinx.kover)
    alias(kmpLibs.plugins.test.logger)
    alias(kmpLibs.plugins.dokka)
    alias(kmpLibs.plugins.buildkonfig)
    alias(kmpLibs.plugins.telereso.kmp)
+   alias(kmpLibs.plugins.sqldelight)
    alias(airasiaLibs.plugins.detekt)

-   id("com.squareup.sqldelight")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        val platform = when (it.name) {
            "iosSimulatorArm64", "iosX64" -> "iphonesimulator"
            else -> "iphoneos"
        }

        it.binaries.all {
            linkerOpts("-ObjC")
            linkerOpts("-L/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/$platform")
            linkerOpts("-L/usr/lib/swift/")
        }
    }


    jvm()

    js {
        moduleName = "my-client"
        version = project.version as String

-       browser {
-           testTask {
-               useMocha{
-                   timeout = "5000"
-               }
-           }
-       }
-
-       nodejs{
-           testTask {
-               useMocha{
-                   timeout = "5000"
-               }
-           }
-       }
+       browser()

        binaries.library()

        binaries.executable()
        generateTypeScriptDefinitions()
    }

    sourceSets {

        all {
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }

        val commonMain by getting {
            dependencies {
                implementation(kmpLibs.bundles.ktor)

                implementation(kmpLibs.bundles.kotlinx)
                implementation(kmpLibs.bundles.sqldelight)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(kmpLibs.test.kotlinx.coroutines.test)
                implementation(kmpLibs.bundles.test.kotest)
                // Ktor Server Mock
                implementation(kmpLibs.test.ktor.client.mock)

                implementation(kmpLibs.test.turbine)
+               implementation(kmpLibs.test.telereso.core)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kmpLibs.ktor.client.okhttp)
                implementation(kmpLibs.okhttp.logging)
                implementation(kmpLibs.sqldelight.runtime.jvm)
                implementation(kmpLibs.sqldelight.sqlite.driver)
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
+               implementation(kmpLibs.sqldelight.sqlite.driver)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(kmpLibs.ktor.client.okhttp)
                implementation(kmpLibs.okhttp.logging)
+               implementation(kmpLibs.sqldelight.android.driver)
            }
        }
        val androidUnitTest by getting {
            dependsOn(commonTest)
            dependencies {
+               implementation(kmpLibs.sqldelight.sqlite.driver)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        /**
         * By using the by creating scope, we ensure the rest of the Darwin targets
         * pick dependecies from the iOSMain.
         * Note using this actual implementations should only exist in the iosMain else
         * the project will complain.
         */
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(kmpLibs.ktor.client.darwin)
                implementation(kmpLibs.sqldelight.native.driver)
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting {
            dependsOn(commonTest)
        }
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
        }

        /**
         * Adding main and test for JS.
         */
        val jsMain by getting {
            dependencies {
                implementation(kmpLibs.ktor.client.js)

-               implementation(kmpLibs.sqldelight.sqljs.driver)
+               implementation(kmpLibs.sqldelight.web.worker.driver)
+               implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.0.2"))
+               implementation(devNpm("copy-webpack-plugin", "9.1.0"))

                implementation(npm("sql.js", kmpLibs.versions.sqlJs.get()))
            }
        }
        val jsTest by getting {
            dependsOn(commonTest)
            dependencies {
-               implementation(kmpLibs.sqldelight.sqljs.driver)
+               implementation(kmpLibs.sqldelight.web.worker.driver)
            }
        }
    }
}


-sqldelight {
-    database("MyClientDatabase") {
-        packageName = "com.example.myclient"
-
-        sourceFolders = listOf("sqldelight")
-
-        schemaOutputDirectory = file("src/commonMain/sqldelight/com/example/myclient/cache")
-
-        verifyMigrations = true
-}

+sqldelight {
+    databases {
+        create("MyClientDatabase") {
+            packageName = dbPackage
+            schemaOutputDirectory = file("src/commonMain/sqldelight/com/example/myclient/cache")
+            verifyMigrations = true
+            generateAsync.set(true)
+        }
+    }
+}

```
---

### _my-client/karma.config.d/sqljs-config.js_

Add karma test config in the module you used sqldelight plugin

```javascript
const path = require("path");
const os = require("os");
const dist = path.resolve("../../../node_modules/sql.js/dist/")
const wasm = path.join(dist, "sql-wasm.wasm")

config.files.push({
    pattern: wasm,
    served: true,
    watched: false,
    included: false,
    nocache: false,
});

config.proxies["/sql-wasm.wasm"] = path.join("/absolute/", wasm)

// Adapted from: https://github.com/ryanclark/karma-webpack/issues/498#issuecomment-790040818
const output = {
  path: path.join(os.tmpdir(), '_karma_webpack_') + Math.floor(Math.random() * 1000000),
}
config.set({
  webpack: {...config.webpack, output},
  client: {
      mocha: {
        timeout: 10000
      }
    }
});

config.files.push({
  pattern: `${output.path}/**/*`,
  watched: false,
  included: false,
});

config.files.push({
   pattern: 'kotlin/*.json',
   watched: true,
   served: true,
   included: false
});

```
---

### _my-client/webpack.config.d/node.js_

update your webpack config

```diff
config.resolve.alias = {
    fs: false,
    path: false,
    crypto: false,
}

+ const CopyWebpackPlugin = require('copy-webpack-plugin');
+ config.plugins.push(
+     new CopyWebpackPlugin({
+         patterns: [
+             '../../../node_modules/sql.js/dist/sql-wasm.wasm'
+         ]
+     })
+ );

```
---

### _my-client/src/commonMain/kotlin/com/example/my/client/cache/Dao.kt_

Update the following 

* `executeAsList` -> `awaitAsList`
* `executeAsOne` -> `awaitAsOne`
* `executeAsOneOrNull` -> `awaitAsOneOrNull`
* `mapToList()` -> `mapToList(DispatchersProvider.Default)`

---

### _my-client/src/commonMain/kotlin/com/example/my/client/cache/SharedDatabase.kt_

```diff
internal class SharedDatabase(
-    private val driverProvider: suspend (schema: SqlDriver.Schema, databaseDriverFactory: MyClientDatabaseDriverFactory?) -> SqlDriver,
-    private val databaseDriverFactory: MyClientDatabaseDriverFactory?
+    private val databaseDriverFactory: SqlDriverFactory
{
    private var database: MyClientDatabase? = null

    private suspend fun initDatabase() {
        if (database == null) {
-            database = Dao.getDatabase(driverProvider(MyClientDatabase.Schema, databaseDriverFactory))
+            database = Dao.getDatabase(databaseDriverFactory.createDriver())
        }
    }

    suspend operator fun <R> invoke(block: suspend (MyClientDatabase) -> R): R {
        initDatabase()
        return block(database!!)
    }

    suspend fun <R> queries(block: suspend (MyClientDatabaseQueries) -> R): R {
        initDatabase()
        return block(database!!.myClientDatabaseQueries)
    }
}
```
---

### _my-client/src/commonMain/kotlin/com/example/my/client/MyClientManger.kt_

```diff
    private val dataBase: SharedDatabase by lazy {
_        SharedDatabase(::provideDbDriver, databaseDriverFactory)
+        SharedDatabase(databaseDriverFactory ?: MyClientDatabaseDriverFactory.default())
    }
```
---

### _my-client/src/commonMain/kotlin/com/example/my/client/Platform.kt_

```diff

-expect interface Parcelable

-@OptIn(ExperimentalMultiplatform::class)
-@OptionalExpectation
-@Target(AnnotationTarget.CLASS)
-@Retention(AnnotationRetention.BINARY)
-expect annotation class CommonParcelize()

-expect suspend fun provideDbDriver(
-    schema: SqlDriver.Schema,
-    databaseDriverFactory: MyClientDatabaseDriverFactory?
-): SqlDriver

-expect class MyClientDatabaseDriverFactory {
-    fun createDriver(): SqlDriver
-}

+expect open class MyClientDatabaseDriverFactory : SqlDriverFactory {
+    companion object {
+        fun default(databaseName: String? = null): SqlDriverFactory
+    }
+    override fun getAsyncSchema(): SqlSchema<QueryResult.AsyncValue<Unit>>
+}
```
---

### _my-client/src/androidMain/kotlin/PlatformAndroid.kt_

```diff
- /**
-  * Android we type the alias to the real Parcelable
-  */
- actual typealias  Parcelable = android.os.Parcelable
- 
- actual suspend fun provideDbDriver(
-     schema: SqlDriver.Schema,
-     databaseDriverFactory: MyClientDatabaseDriverFactory?
- ): SqlDriver {
-     return databaseDriverFactory!!.createDriver()
- }
+ 
+ actual class MyClientDatabaseDriverFactory(private val context: Context) {
+     actual fun createDriver(): SqlDriver {
+         return AndroidSqliteDriver(MyClientDatabase.Schema, context, Dao.DATABASE_NAME)
+ actual open class MyClientDatabaseDriverFactory(
+     context: Context?,
+     databaseName: String? = null
+ ) :
+     SqlDriverFactory(databaseName ?: Dao.DATABASE_NAME, context) {
+     actual companion object {
+         actual fun default(databaseName: String?): SqlDriverFactory {
+             return MyClientDatabaseDriverFactory(null, databaseName)
+         }
+     }
+ }
+     actual override fun getAsyncSchema() = MyClientDatabase.Schema
+     override fun getSchema(): SqlSchema<QueryResult.Value<Unit>>? =MyClientDatabase.Schema.synchronous()
+ }
```
---

### _my-client/src/commonTest/kotlin/cache/DaoTest.kt_

```diff

class DaoTest {

+    companion object {
+        fun getTestFactory(): SqlDriverFactory {
+            return TestSqlDriverFactory(MyClientDatabaseDriverFactory.default())
+        }
+    }
    
    
}

```
---

### _my-client/src/androidUnitTest/kotlin/PlatformAndroidTest.kt_

```diff

- import com.squareup.sqldelight.db.SqlDriver
- import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
- import java.io.File
- 
- actual class Resource actual constructor(actual val name: String) {
-     private val file = File("${TestUtils.RESOURCE_PATH}/$name")
- 
-     actual fun exists(): Boolean = file.exists()
- 
-     actual fun readText(): String = file.readText()
- }
- 
- actual suspend fun provideDbDriverTest(schema: SqlDriver.Schema,
-                                        databaseDriverFactory: MyClientDatabaseDriverFactory?): SqlDriver {
-     return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
-         schema.create(this)
-     }
- }

```
---


### _my-client/src/iosTest/kotlin/PlatformIosTest.kt_

```diff

-private var dbIndex = 0

-@OptIn(ExperimentalForeignApi::class)
-actual class Resource actual constructor(actual val name: String) {
-    val pathParts = name.split("[.|/]".toRegex())
-    val path = NSBundle.mainBundle.pathForResource("resources/${pathParts[0]}", pathParts[1])
-    private val file: CPointer<FILE>? = fopen(path, "r")
-    actual fun exists(): Boolean = file != null
-
-    actual fun readText(): String {
-        fseek(file, 0, SEEK_END)
-        val size = ftell(file)
-        rewind(file)
-
-        return memScoped {
-            val tmp = allocArray<ByteVar>(size)
-            fread(tmp, sizeOf<ByteVar>().convert(), size.convert(), file)
-            tmp.toKString()
-        }
-    }
-}

-actual suspend fun provideDbDriverTest(schema: SqlDriver.Schema,
-                                       databaseDriverFactory: MyClientDatabaseDriverFactory?): SqlDriver {
-    return NativeSqliteDriver(
-        DatabaseConfiguration(
-            name = "${++dbIndex}-${Dao.DATABASE_NAME}",
-            version = schema.version,
-            create = { connection ->
-                wrapConnection(connection) { schema.create(it) }
-            },
-            upgrade = { connection, oldVersion, newVersion ->
-                wrapConnection(connection) {
-                    schema.migrate(it, oldVersion, newVersion)
-                }
-            },
-            inMemory = true
-        )
-    )

```
---



### run 
```shell
./gradlew kotlinUpgradeYarnLock
```
---