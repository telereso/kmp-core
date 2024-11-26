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

package io.telereso.kmp.core.ui.test

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.serviceLoaderEnabled
import coil3.svg.SvgDecoder
import coil3.test.FakeImage
import coil3.test.FakeImageLoaderEngine
import coil3.util.DebugLogger
import io.telereso.kmp.core.ui.LoginPage
import io.telereso.kmp.core.ui.pages.Devices
import kotlin.test.Test
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class ScreenShotTest2 {

    @Composable
    fun setup() {
        val engine = FakeImageLoaderEngine.Builder()
            .intercept(
                "https://raw.githubusercontent.com/google/material-design-icons/refs/heads/master/symbols/web/visibility/materialsymbolsoutlined/visibility_24px.svg",
                FakeImage(color = 0xF00)
            ) // Red
            .intercept(
                { it is String && it.contains("arrow_back") }, FakeImage(color = 0x0F0)
            ) // Green
            .default(FakeImage(color = 0x00F)) // Blue
            .build()

        val debug = false
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components {
//                    add(engine)
                    add(SvgDecoder.Factory())
                }
                .memoryCache {
                    MemoryCache.Builder()
                        // Set the max size to 25% of the app's available memory.
                        .maxSizePercent(context, percent = 0.25)
                        .build()
                }
//            .diskCache {
//                DiskCache.Builder()
//                    .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
//                    .maxSizePercent(percent = 0.50)
//                    .build()
//            }
                // Show a short crossfade when loading images asynchronously.
                .crossfade(false)
                // Enable logging if this is a debug build.
                .apply {
                    if (debug) {
                        logger(DebugLogger())
                    }
                }
                .build()
        }
    }

    @Test
    fun testPage() = runScreenShotTest(
        devices = listOf(
            Devices.Pixel_3
        ),
        wait = 2.toDuration(DurationUnit.SECONDS)
    ) {
        setup()
        LoginPage()
    }

    @Test
    fun testPage2() = runScreenShotTest(
        devices = listOf(
            Devices.Pixel_3, Devices.Pixel_4_XL
        ),
        wait = 4.toDuration(DurationUnit.SECONDS)
    ) {
        setup()
        LoginPage()
    }

}
