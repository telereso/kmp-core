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

package io.telereso.kmp.jsoncompare

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import getPreferredWindowSize

@Composable
fun ApplicationScope.AppDesktop() {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Json Compare",
        state = WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = getPreferredWindowSize(720, 857)
        ),
//        icon = painterResource(Res.drawable.ic_imageviewer_round),
        // https://github.com/JetBrains/compose-jb/issues/2741
        onKeyEvent = {
//            if (it.type == KeyEventType.KeyUp) {
//                when (it.key) {
//                    Key.DirectionLeft -> externalNavigationEventBus.produceEvent(
//                        ExternalImageViewerEvent.Back
//                    )
//
//                    Key.DirectionRight -> externalNavigationEventBus.produceEvent(
//                        ExternalImageViewerEvent.Foward
//                    )
//                }
//            }
            false
        }
    ) {
        App()
    }

}