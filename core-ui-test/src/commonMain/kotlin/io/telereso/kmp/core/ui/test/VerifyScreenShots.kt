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

import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.format.PNG
import korlibs.image.format.readBitmap
import korlibs.io.stream.openAsync

suspend fun verify(
    baseScreenShotByteArray: ByteArray,
    newScreenShotByteArray: ByteArray,
    tolerance: Float
): ByteArray? {
    require(tolerance in 0.0..100.0) { "Tolerance must be between 0.0 and 100.0" }

    // Decode images
    val baseImage = baseScreenShotByteArray.openAsync().readBitmap().toBMP32()
    val newImage = newScreenShotByteArray.openAsync().readBitmap().toBMP32()

    require(baseImage.width == newImage.width && baseImage.height == newImage.height) {
        "Images must have the same dimensions"
    }

    val width = baseImage.width
    val height = baseImage.height
    val normalizedTolerance = tolerance / 100f

    var hasDifference = false
    val diffImage = Bitmap32(width, height)

    for (x in 0 until width) {
        for (y in 0 until height) {
            val basePixel = baseImage.getRgba(x, y)
            val newPixel = newImage.getRgba(x, y)

            if (basePixel.a == 0 && newPixel.a == 0) {
                // Both pixels are transparent; keep as-is
                diffImage.setRgba(x, y, basePixel)
            } else {
                val diff = colorDifference(basePixel, newPixel)

                if (diff > normalizedTolerance) {
                    hasDifference = true
                    // Highlight differences
                    when {
                        basePixel.a == 0 -> diffImage.setRgba(x, y, Colors.GREEN) // New pixel (green)
                        newPixel.a == 0 -> diffImage.setRgba(x, y, Colors.RED)   // Removed pixel (red)
                        else -> diffImage.setRgba(x, y, Colors.GREEN)            // Significant difference
                    }
                } else {
                    // No significant difference; copy the base pixel
                    diffImage.setRgba(x, y, basePixel)
                }
            }
        }
    }

    // If no differences were found, return null (images match within tolerance)
    return if (hasDifference) {
        PNG.encode(diffImage)
    } else {
        null
    }
}

// Function to compute the color difference
private fun colorDifference(color1: RGBA, color2: RGBA): Float {
    val redDiff = kotlin.math.abs(color1.r - color2.r) / 255f
    val greenDiff = kotlin.math.abs(color1.g - color2.g) / 255f
    val blueDiff = kotlin.math.abs(color1.b - color2.b) / 255f
    val alphaDiff = kotlin.math.abs(color1.a - color2.a) / 255f
    return (redDiff + greenDiff + blueDiff + alphaDiff) / 4
}
