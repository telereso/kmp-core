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

package io.telereso.kmp.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.scene.MultiLayerComposeScene
import androidx.compose.ui.scene.SingleLayerComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.telereso.kmp.core.Platform
import io.telereso.kmp.core.RunBlocking
import io.telereso.kmp.core.Task
import io.telereso.kmp.core.getPlatform
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import org.jetbrains.skia.Image.Companion.makeFromBitmap
import kotlin.time.Duration

@Composable
actual fun androidLocalContext(): Any? = null

@OptIn(InternalComposeUiApi::class, RunBlocking::class)
actual suspend fun captureComposableAsBitmap(
    width: Int,
    height: Int,
    wait: Duration?,
    context: Any?,
    content: @Composable (Modifier) -> Unit
): ByteArray? {
    // Create a ComposeScene to host the Composable content
    val scene = MultiLayerComposeScene(
        density = Density(1f), // Adjust density as per your requirements
        layoutDirection = LayoutDirection.Ltr
    )

    // Set content in the scene
    scene.setContent {
        Box(
            modifier = Modifier
                .width(width.dp)
                .height(height.dp)
        ) {
            content(
                Modifier.fillMaxSize()
            )
        }
    }

    scene.calculateContentSize()

    wait?.let {
        when (getPlatform().type) {
            Platform.Type.BROWSER, Platform.Type.NODE -> delay(it)
            else -> Task.execute { delay(it) }.get()
        }
    }

    // Create a Bitmap to render into
    val imageBitmap = ImageBitmap(width, height)
    val canvas = Canvas(imageBitmap)
    // Render the Composable content into the Bitmap
    scene.render(canvas, (Clock.System.now().toEpochMilliseconds()) * 1000)

    // Convert the ImageBitmap to a PNG ByteArray
    val skiaBitmap = imageBitmap.asSkiaBitmap()
    val skiaImage = makeFromBitmap(skiaBitmap)
    scene.close()
    return skiaImage.encodeToData()?.bytes
}

//fun ByteArray.toBitmap(width: Int, height: Int): Bitmap {
//    val imageInfo = ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.OPAQUE)
//    return Bitmap().also {
//        it.allocPixels(imageInfo)
//        it.installPixels(this)
//    }
//}