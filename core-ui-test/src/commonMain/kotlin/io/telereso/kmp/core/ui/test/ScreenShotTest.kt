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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.telereso.kmp.core.ui.pages.DeviceInfo
import io.telereso.kmp.core.ui.pages.Devices
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

expect fun runScreenShotTest(
    device: DeviceInfo = Devices.Pixel_3,
    wait: Duration? = 500.toDuration(DurationUnit.MILLISECONDS),
    block: @Composable (Modifier) -> Unit
)

fun runScreenShotTest(
    devices: List<DeviceInfo>,
    wait: Duration? = 500.toDuration(DurationUnit.MILLISECONDS),
    block: @Composable (Modifier) -> Unit
) {
    devices.forEach { device ->
        runScreenShotTest(
            device = device,
            wait = wait,
            block = block
        )
    }
}
