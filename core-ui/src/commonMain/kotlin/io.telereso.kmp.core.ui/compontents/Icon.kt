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

package io.telereso.kmp.core.ui.compontents

import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import io.telereso.kmp.core.ui.models.SymbolConfig
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun Icon(
    url: String? = null, drawableResource: DrawableResource? = null,
    placeholder : DrawableResource? = drawableResource,
    error : DrawableResource? = drawableResource,
    fallback : DrawableResource? = drawableResource,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier.size(size),
    contentDescription: String? = null
) {
    val context = LocalPlatformContext.current
    AsyncImage(
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint),
        placeholder = placeholder?.let { painterResource(it) },
        error = error?.let { painterResource(error) },
        fallback = fallback?.let { painterResource(fallback) },
        model = ImageRequest.Builder(context).data(url ?: drawableResource)
            .build(), contentDescription = contentDescription ?: "Icon"
    )
}

@Composable
fun Symbol(
    symbolConfig: SymbolConfig,
    error: DrawableResource? = null,
    fallback: DrawableResource? = null,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier.size(size),
    contentDescription: String? = null
) {
    Icon(
        url = symbolConfig.url(),
        error = error,
        fallback = fallback,
        tint = tint,
        size = symbolConfig.size.name.removePrefix("_").toInt().dp,
        modifier = modifier,
        contentDescription = contentDescription
    )
}

@Composable
fun Icon(
    drawableResource: DrawableResource,
    error: DrawableResource? = drawableResource,
    fallback: DrawableResource? = drawableResource,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier.size(size),
    contentDescription: String? = null
) {
    Icon(
        url = null,
        drawableResource = drawableResource,
        error = error,
        fallback = fallback,
        tint = tint,
        size = size,
        modifier = modifier,
        contentDescription = contentDescription
    )
}