/*
 * MIT License
 *
 * Copyright (c) 2023 Telereso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.telereso.kmp


import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import java.io.File
import javax.inject.Inject


open class TeleresoKmpExtension @Inject constructor(
    objects: ObjectFactory
) {

    /**
     * set to true to stop copying generated model files with json converters extensions into src dir
     */
    var disableJsonConverters: Boolean = false
    /**
     * set to true to stop copying generated reactNative files react native dir
     */
    var enableReactNativeExport: Boolean = false

    var disableReactNativeGradle8Workaround: Boolean = false
    /**
     * set to true to stop copying generated flutter files into flutter dir
     */
    var disableFlutterExport: Boolean = false
    /**
     * set to true to start using [JvmOverloads] as a SwiftOverloads annotation , to avoid updating all models in old project
     */
    var swiftOverloadsByJvmOverloads: Boolean = false

    /**
     * in case of naming conflict or project's conventional naming you can change the name of function factory that create the object,
     * By default it s as following
     *
     * ```
     * User.companion().object("123")
     * ```
     * if changed to "instance" , it will be
     *
     * ```
     * User.companion().instance("123")
     * ```
     *
     */
    var createObjectFunctionName: String = "`object`"

    var umbrellaFrameworkName: String? = null

    var nodeModulesDirectory: File? = null

    var reactNativePackageDirectory: File? = null

    var removeStringErrorExtension: Boolean = true

    /**
     * Disabled by defaulted
     * Set to true if project needs to enable screen shot testing
     */
    var enableScreenShots: Boolean = false

    /**
     * ScreenShot testing tolerance to changes
     * This might include coloring and position positioning
     * It range from 0 to 100 , so 1f means 0.1% and it's strict to changes and 99% is very relaxed
     * Recommended to keep at 0.1%
     */
    var screenShotsTolerance: Float = 1f

    internal val exportedReactNativePackages = mutableListOf<ReactNativePackage>()


    fun rnp(name: String): ReactNativePackage {
        return ReactNativePackage(null, name)
    }

    fun framework(name: String): String {
        return name
    }

    fun rnp(name: String, framework: () -> String): ReactNativePackage {
        return ReactNativePackage(null, name, framework())
    }

    fun rnp(scope: String, name: String): ReactNativePackage {
        return ReactNativePackage(scope, name)
    }

    fun rnp(scope: String, name: String, framework: String): ReactNativePackage {
        return ReactNativePackage(scope, name, framework)
    }

    fun export(reactNativePackage: ReactNativePackage) {
        exportedReactNativePackages.add(reactNativePackage)
    }

    companion object {
        fun Project.teleresoKmp(): TeleresoKmpExtension {
            return extensions.create("teleresoKmp", TeleresoKmpExtension::class.java)
        }
    }
}