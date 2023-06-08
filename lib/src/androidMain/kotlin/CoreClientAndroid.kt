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

package io.telereso.kmp.core

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.telereso.kmp.core.models.ClientException
import java.security.MessageDigest
import java.util.*
import kotlin.jvm.JvmOverloads

actual class CoreClient(private val application: Application) {

    actual companion object {
        /**
         * Called from the client to initialize Napier logger
         */
        actual fun debugLogger() {
            Napier.base(DebugAntilog())
        }
    }

    /**
     * Use this function to check if an application is installed on the user's device
     * @param packageName android application package name
     * @param fingerprint the singing fingerprint
     * @param alg [fingerprint]'s algorithm , defaulted to "SHA-256"
     */
    @SuppressLint("PackageManagerGetSignatures")
    @JvmOverloads
    actual fun isAppInstalled(
        packageName: String,
        fingerprint: String?,
        alg: String
    ): Boolean {
        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = application.packageManager.getPackageInfo(
                    packageName, PackageManager.GET_SIGNING_CERTIFICATES
                )

                packageInfo.signingInfo?.apkContentsSigners
            } else {
                val packageInfo =
                    application.packageManager.getPackageInfo(
                        packageName,
                        PackageManager.GET_SIGNATURES
                    )
                packageInfo.signatures
            } ?: emptyArray()

            if (fingerprint == null) return true

            val md = MessageDigest.getInstance(alg)

            for (s in signatures) {
                val signatureBytes = s.toByteArray()
                val hashBytes = md.digest(signatureBytes)
                val hexString = byte2HexFormatted(hashBytes)
                if (hexString.equals(fingerprint, true))
                    return true
            }
            false
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    /**
     * This is to be used by a sdk only,
     * Verify that the current application is allowed to use the calling library
     * @param allowed a list of [Consumer] depending on the sdk platforms
     */
    actual fun verifyConsumer(allowed: List<Consumer>) {
        val t = ClientException("App (${application.packageName}) is not allowed to use this sdk")

        if (!allowed.any { c ->
                c.platform == Platform.Type.ANDROID
                        && c.appId == application.packageName
                        && isAppInstalled(c.appId, c.fingerprint, c.alg)
            }) {
            throw t
        }
    }

    /**
     * This is to be used by a sdk only,
     * Verify that the current application is allowed to use the calling library
     * @param allowed a list of [Consumer] depending on the sdk platforms
     */
    actual fun verifyConsumer(vararg allowed: Consumer) {
        verifyConsumer(allowed.toList())
    }

    private fun byte2HexFormatted(arr: ByteArray): String {
        val str = StringBuilder(arr.size * 2)
        for (i in arr.indices) {
            var h = Integer.toHexString(arr[i].toInt())
            val l = h.length
            if (l == 1) h = "0$h"
            if (l > 2) h = h.substring(l - 2, l)
            str.append(h.uppercase(Locale.getDefault()))
            if (i < arr.size - 1) str.append(':')
        }
        return str.toString()
    }

}