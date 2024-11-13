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

package io.telereso.kmp.core.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import coil3.util.DebugLogger
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.telereso.kmp.core.preview.pages.JsonComparePage
import io.telereso.kmp.core.preview.pages.SymbolsPreviewPage
import io.telereso.kmp.core.ui.compontents.Symbol
import io.telereso.kmp.core.ui.getCurrentDeeplink
import io.telereso.kmp.core.ui.models.SentimentSatisfied
import io.telereso.kmp.core.ui.models.Symbols
import io.telereso.kmp.core.ui.models.TextCompare
import io.telereso.kmp.core.ui.widgets.DeeplinkNavHost
import io.telereso.kmp.core.ui.widgets.base
import io.telereso.kmp.core.ui.widgets.deeplink
import io.telereso.kmp.core.ui.widgets.route

enum class Pages {
    Symbols, JsonCompare
}

@Composable
fun App() {
    setupApp()
    val navHostController = rememberNavController()
    val currentUrl = getCurrentDeeplink().base()

    Row(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF0F2F5))
    ) {
        SidebarNavigationRail(navHostController)
        DeeplinkNavHost(
            navController = navHostController,
            startDestination = Pages.Symbols.name
        ) {

            composable(currentUrl.route(Pages.Symbols.name)) {
                SymbolsPreviewPage()
            }

            composable(currentUrl.route(Pages.JsonCompare.name)) {
                JsonComparePage()
            }
        }
    }
}

@Composable
fun SidebarNavigationRail(
    navHostController: NavHostController = rememberNavController()
) {
    var selected by remember { mutableStateOf(Pages.Symbols) }

    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFFE7EBF0)),
    ) {
        NavigationRailItem(
            icon = { Symbol(Symbols.SentimentSatisfied, contentDescription = "Icons") },
            selected = selected == Pages.Symbols,
            onClick = {
                selected = Pages.Symbols
                navHostController.deeplink(selected.name)
            }
        )
        NavigationRailItem(
            icon = { Symbol(Symbols.TextCompare, contentDescription = "JsonCompare") },
            selected = selected == Pages.JsonCompare,
            onClick = {
                selected = Pages.JsonCompare
                navHostController.deeplink(selected.name)
            }
        )
    }
}

@Composable
fun setupApp() {
    val debug = true
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
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
            .crossfade(true)
            // Enable logging if this is a debug build.
            .apply {
                if (debug) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}