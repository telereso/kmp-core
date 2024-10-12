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
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.Modifier

const val monacoHtml = """
    <!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Monaco Editor Example</title>
    <style>
        /* Set up the height for the Monaco Editor */
        #editor {
            width: 100%;
            height: 90vh;
            border: 1px solid #ccc;
        }
    </style>
</head>
<body>
    <h1>Monaco Editor in HTML</h1>
    <div id="editor"></div>

    <!-- Load Monaco Editor from CDN -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.34.0/min/vs/loader.min.js"></script>

    <script>
        require.config({ paths: { 'vs': 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.34.0/min/vs' }});
        
        require(['vs/editor/editor.main'], function() {
            monaco.editor.create(document.getElementById('editor'), {
                value: `function helloMonaco() {
    console.log('Hello, Monaco Editor!');
}`,
                language: 'javascript'
            });
        });
    </script>
</body>
</html>
"""

//@Composable
//expect fun WebView(
//    modifier: Modifier = Modifier,
//    url: String? = null,
//    html: String? = null
//)


