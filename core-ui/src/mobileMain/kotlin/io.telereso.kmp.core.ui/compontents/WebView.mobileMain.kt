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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData

@Composable
actual fun WebView(
    modifier: Modifier,
    url: String?,
    html: String?,
    state: Any?,
    captureBackPresses: Boolean,
    navigator: Any?,
    webViewJsBridge: Any?,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
    content: (@Composable () -> Unit)?
) {
    content?.invoke()?: run {
        val webViewState = when {
            state != null -> state as WebViewState
            url != null -> rememberWebViewState(url)
            html != null -> rememberWebViewStateWithHTMLData(html)
            else -> return
        }

        com.multiplatform.webview.web.WebView(
            state = webViewState,
            modifier = modifier,
            captureBackPresses = captureBackPresses,
            navigator = (navigator as? WebViewNavigator) ?: rememberWebViewNavigator(),
            webViewJsBridge = webViewJsBridge as? WebViewJsBridge,
            onCreated = onCreated,
            onDispose = onDispose,
        )
    }
}