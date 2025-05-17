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

import io.ktor.http.Url
import kotlinx.browser.window

actual fun getCurrentDeeplink(): Url {
    _currentDeeplink.value = Url(window.location.href.removeSuffix("/"))
    return _currentDeeplink.value
}

actual fun browserSetCurrentPath(newPath: String) {
    window.history.pushState(null, "", "/$newPath")
}


actual suspend fun browserDownloadFile(type: String, filename: String, base64Content: String) {
    downloadFile(type, filename, base64Content)
}

actual suspend fun browserZipAndDownloadFiles(filesJson:String){
    zipAndDownloadFiles(filesJson)
}


private fun downloadFile(type: String, filename: String, base64Content: String): Unit = js(
    """{
        const binaryString = atob(base64Content); // Decode base64 string to binary
        const byteArray = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
            byteArray[i] = binaryString.charCodeAt(i);
        }
        const blob = new Blob([byteArray], { type: type });
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement("a");
        anchor.href = url;
        anchor.download = filename;
        document.body.appendChild(anchor);
        anchor.click();
        document.body.removeChild(anchor);
        URL.revokeObjectURL(url);
    }"""
)


private fun zipAndDownloadFiles(filesJson: String): Unit = js(
    """{      
        const jszipScript = document.createElement("script");
        jszipScript.src = "https://cdnjs.cloudflare.com/ajax/libs/jszip/3.10.1/jszip.min.js";
        jszipScript.onload = () => {
            const zip = new JSZip();
            // Parse the input JSON (an array of file objects)
            const filesArray = JSON.parse(filesJson);

            // Iterate over each file object
            filesArray.forEach(file => {
                const { filename, contentBase64 } = file;

                // Add the file to the ZIP
                if (filename && contentBase64) {
                    zip.file(filename, atob(contentBase64), { binary: true });
                }
            });

            // Generate the ZIP and trigger the download
            zip.generateAsync({ type: "blob" }).then(blob => {
                const url = URL.createObjectURL(blob);
                const anchor = document.createElement("a");
                anchor.href = url;
                anchor.download = "screenshots.zip";
                document.body.appendChild(anchor);
                anchor.click();
                document.body.removeChild(anchor);
                URL.revokeObjectURL(url);
            });
        };
        document.head.appendChild(jszipScript);
        }"""
)