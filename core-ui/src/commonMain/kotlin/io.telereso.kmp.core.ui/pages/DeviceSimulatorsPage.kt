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

package io.telereso.kmp.core.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.ktor.http.ContentType
import io.telereso.kmp.core.Platform
import io.telereso.kmp.core.getPlatform
import io.telereso.kmp.core.ui.browserDownloadFile
import io.telereso.kmp.core.ui.captureComposableAsBitmap
import io.telereso.kmp.core.ui.compontents.Symbol
import io.telereso.kmp.core.ui.models.AddAPhoto
import io.telereso.kmp.core.ui.models.Check
import io.telereso.kmp.core.ui.models.Code
import io.telereso.kmp.core.ui.models.Screenshot
import io.telereso.kmp.core.ui.models.Symbols
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

enum class DeviceBrand {
    Iphone, Pixel, Samsung, OnePlus, Huawei, Xiaomi, Sony
}

data class Device(
    val brand: DeviceBrand,
    val name: String,
    val screenWidth: Dp,
    val screenHeight: Dp,
    val screenShotRes: DrawableResource? = null
){
    fun fullName() :String = name.plus(" (${screenWidth.value.toInt()}x${screenHeight.value.toInt()})")
    fun fileName() :String = name.plus("_${screenWidth.value.toInt()}x${screenHeight.value.toInt()}")
        .replace(" ","_")
        .lowercase()
}

@Composable
fun DefaultDevicesMap(screenShotRes: DrawableResource? = null) = mapOf(
    // iPhones
    DeviceBrand.Iphone to listOf(
        Device(DeviceBrand.Iphone, "iPhone SE (2020)", 375.dp, 667.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPhone 11 Pro Max", 414.dp, 896.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPhone 12", 390.dp, 844.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPhone 12 Pro Max", 428.dp, 926.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPhone 13 Mini", 375.dp, 812.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPhone 14", 390.dp, 844.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPhone 14 Pro", 393.dp, 852.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPhone 14 Pro Max", 430.dp, 932.dp, screenShotRes),
        // tablets
        Device(DeviceBrand.Iphone, "iPad Mini (6th gen)", 744.dp, 1133.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPad 9th gen", 810.dp, 1080.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPad Air 4th gen", 820.dp, 1180.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPad Pro 11-inch", 834.dp, 1194.dp, screenShotRes),
        Device(DeviceBrand.Iphone, "iPad Pro 12.9-inch", 1024.dp, 1366.dp, screenShotRes),
    ),

    // Samsung Galaxy Series
    DeviceBrand.Samsung to listOf(
        Device(DeviceBrand.Samsung, "Galaxy S9", 360.dp, 740.dp, screenShotRes),
        Device(DeviceBrand.Samsung, "Galaxy S10", 360.dp, 760.dp, screenShotRes),
        Device(DeviceBrand.Samsung, "Galaxy S20 Ultra", 412.dp, 915.dp, screenShotRes),
        Device(DeviceBrand.Samsung, "Galaxy S22", 370.dp, 800.dp, screenShotRes),
        Device(DeviceBrand.Samsung, "Galaxy Note 10+", 412.dp, 898.dp, screenShotRes),
        Device(DeviceBrand.Samsung, "Galaxy Note 20", 412.dp, 883.dp, screenShotRes),

        // foldable
        Device(DeviceBrand.Samsung, "Galaxy Z Fold3 (unfolded)", 832.dp, 2260.dp, screenShotRes),
        Device(DeviceBrand.Samsung, "Galaxy Z Fold3 (folded)", 360.dp, 832.dp, screenShotRes),
        Device(DeviceBrand.Samsung, "Galaxy Z Flip3", 360.dp, 780.dp, screenShotRes),
    ),

    // Google Pixel Series

    DeviceBrand.Pixel to listOf(
        Device(DeviceBrand.Pixel, "Pixel 3", 393.dp, 786.dp, screenShotRes),
        Device(DeviceBrand.Pixel, "Pixel 3 XL", 412.dp, 847.dp, screenShotRes),
        Device(DeviceBrand.Pixel, "Pixel 4 XL", 411.dp, 869.dp, screenShotRes),
        Device(DeviceBrand.Pixel, "Pixel 5", 393.dp, 851.dp, screenShotRes),
        Device(DeviceBrand.Pixel, "Pixel 6 Pro", 411.dp, 946.dp, screenShotRes),
        Device(DeviceBrand.Pixel, "Pixel 7", 412.dp, 915.dp, screenShotRes),
        Device(DeviceBrand.Pixel, "Pixel 7 Pro", 412.dp, 928.dp, screenShotRes),
        // tablets
        Device(DeviceBrand.Samsung, "Galaxy Tab S7", 800.dp, 1280.dp, screenShotRes),
        Device(DeviceBrand.Samsung, "Galaxy Tab S8", 800.dp, 1340.dp, screenShotRes),
    ),

    // OnePlus Series
    DeviceBrand.OnePlus to listOf(
        Device(DeviceBrand.OnePlus, "OnePlus 7", 412.dp, 869.dp, screenShotRes),
        Device(DeviceBrand.OnePlus, "OnePlus 7 Pro", 412.dp, 899.dp, screenShotRes),
        Device(DeviceBrand.OnePlus, "OnePlus 10 Pro", 411.dp, 926.dp, screenShotRes),
    ),

    // Huawei
    DeviceBrand.Huawei to listOf(
        Device(DeviceBrand.Huawei, "Huawei P30", 360.dp, 800.dp, screenShotRes),
        Device(DeviceBrand.Huawei, "Huawei P40 Pro", 412.dp, 884.dp, screenShotRes),
        Device(DeviceBrand.Huawei, "Huawei Mate 30", 412.dp, 880.dp, screenShotRes),
        Device(DeviceBrand.Huawei, "Huawei Mate 40 Pro", 439.dp, 938.dp, screenShotRes),
        Device(DeviceBrand.Huawei, "Huawei Mate X2 (unfolded)", 882.dp, 2200.dp, screenShotRes),
    ),

    // Xiaomi
    DeviceBrand.Xiaomi to listOf(
        Device(DeviceBrand.Xiaomi, "Xiaomi Mi 10", 392.dp, 832.dp, screenShotRes),
        Device(DeviceBrand.Xiaomi, "Xiaomi Mi 11", 392.dp, 873.dp, screenShotRes),
        Device(DeviceBrand.Xiaomi, "Xiaomi Redmi Note 8", 392.dp, 830.dp, screenShotRes),
        Device(DeviceBrand.Sony, "Sony Xperia 1", 360.dp, 820.dp, screenShotRes),
    )
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class)
@Composable
fun DeviceSimulatorsPage(
    devicesMap: Map<DeviceBrand, List<Device>> = DefaultDevicesMap(),
    content: @Composable (Modifier) -> Unit = { modifier ->
        Column(
            modifier = modifier.padding(16.dp)
        ) {
            Text("Welcome to the Device Simulator!")
            Spacer(modifier = Modifier.height(16.dp))
            Text("This is how your app will look on the selected device.")
            // You can add more content to show in the simulated device here.
        }
    }
) {

    var expanded by remember { mutableStateOf(false) }
    var selectedDevices by remember {
        mutableStateOf(
            listOf<Device>(
                devicesMap[DeviceBrand.Pixel]!![0],
                devicesMap[DeviceBrand.Iphone]!![0],
                devicesMap[DeviceBrand.Samsung]!![0],
                devicesMap[DeviceBrand.Huawei]!![0],
                devicesMap[DeviceBrand.Xiaomi]!![0],
            )
        )
    }  // Track selected devices

    val leftDrawerState = rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(drawerState = leftDrawerState, drawerContent = {
        ModalDrawerSheet {
            Column(
                modifier = Modifier.width(250.dp)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Devices", style = MaterialTheme.typography.titleLarge)

                devicesMap.forEach { brand ->

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        brand.key.name.lowercase().capitalize(),
                        style = MaterialTheme.typography.titleMedium
                    )

                    brand.value.forEach { device ->
                        DropdownMenuItem(
                            text = {
                                Text(device.fullName())
                            },
                            onClick = {
                                // Add the selected device to the list
                                if (!selectedDevices.contains(device)) {
                                    selectedDevices = selectedDevices + device
                                }
                                expanded = false // Close the menu after selecting
                            },
                            leadingIcon = {
                                // Add check mark to indicate selection
                                if (selectedDevices.contains(device)) {
                                    Symbol(Symbols.Check)
                                }
                            }
                        )
                    }


                }
            }
        }
    }) {

        // Center Panel
        Column(
            modifier = Modifier.fillMaxHeight()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            TextButton(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
                onClick = {
                    scope.launch {
                        leftDrawerState.open()
                    }
                }
            ) {
                Text("More Devices")
            }

            // Display all selected devices in the FlowRow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                selectedDevices.forEach {
                    DeviceFrame(
                        device = it,
                        content = content,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun DeviceFrame(
    device: Device,
    content: @Composable (Modifier) -> Unit,
) {
    var alpha by remember { mutableStateOf(0.5f) }
    val contentModifier = Modifier
        .alpha(1 - alpha)
        .fillMaxSize()

    var capturingScreenShot by remember { mutableStateOf(false) }


    if (capturingScreenShot) {
        val ba = captureComposableAsBitmap(
            device.screenWidth.value.toInt(),
            device.screenHeight.value.toInt(),
            content
        )
        if (ba != null) {
            when (getPlatform().type) {
                Platform.Type.BROWSER -> {
                    browserDownloadFile(
                        ContentType.Image.PNG.contentType,
                        "${device.fileName()}.png",
                        Base64.encode(ba)
                    )
                }

                else -> {
                    SystemFileSystem.sink(Path("/tmp/screenshot.png"))
                        .buffered()
                        .use { sink ->
                            sink.write(ba)
                        }
                }
            }
        }

        capturingScreenShot = false
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(device.screenWidth + 32.dp) // 32 for frame 16 left and right
    ) {
        // Screenshot label
        Text(
            text = device.name.plus(if (device.screenShotRes != null) " ~ Screenshot" else ""),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (device.screenShotRes != null)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Symbol(
                    Symbols.AddAPhoto,
                    modifier = Modifier.clickable {
                        capturingScreenShot = true
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                Symbol(Symbols.Code)

                Slider(
                    value = alpha,
                    onValueChange = { alpha = it },
                    steps = 20,
                    valueRange = 0f..1f,
                    modifier = Modifier.width(200.dp)
                )

                Symbol(Symbols.Screenshot)

                Spacer(modifier = Modifier.weight(1f))
            }

        Box(
            modifier = Modifier
                .height(
                    device.screenHeight + 48.dp
                ) // Adding padding for the frame
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black) // Device frame background color
                .padding(16.dp) // Padding to simulate the outer device frame
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White) // The screen's background color (inside the device frame)
                    .clip(RoundedCornerShape(16.dp)) // Round the corners of the screen
            ) {
                // Draw the content of the device screen
                content(contentModifier)

                device.screenShotRes?.let {
                    Image(
                        painter = painterResource(it),
                        contentDescription = "ScreenShot",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize().alpha(alpha)
                    )
                }

            }
        }

    }
}