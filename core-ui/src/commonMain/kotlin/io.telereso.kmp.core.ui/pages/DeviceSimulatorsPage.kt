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
import androidx.compose.runtime.LaunchedEffect
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
import io.ktor.utils.io.core.toByteArray
import io.telereso.kmp.core.DispatchersProvider
import io.telereso.kmp.core.Http
import io.telereso.kmp.core.Log
import io.telereso.kmp.core.Platform
import io.telereso.kmp.core.getPlatform
import io.telereso.kmp.core.ui.browserDownloadFile
import io.telereso.kmp.core.ui.browserZipAndDownloadFiles
import io.telereso.kmp.core.ui.captureComposableAsBitmap
import io.telereso.kmp.core.ui.compontents.Symbol
import io.telereso.kmp.core.ui.models.AddAPhoto
import io.telereso.kmp.core.ui.models.Check
import io.telereso.kmp.core.ui.models.Code
import io.telereso.kmp.core.ui.models.Screenshot
import io.telereso.kmp.core.ui.models.Symbols
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.encodeToString
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

enum class DeviceBrand {
    Iphone, Pixel, Samsung, OnePlus, Huawei, Xiaomi, Sony
}

object Devices {
    val iPhone_SE get() = DeviceInfo(DeviceBrand.Iphone, "iPhone SE (2020)", 375.dp, 667.dp)
    val iPhone_11_Pro_Max
        get() = DeviceInfo(
            DeviceBrand.Iphone, "iPhone 11 Pro Max", 414.dp, 896.dp
        )
    val iPhone_12 get() = DeviceInfo(DeviceBrand.Iphone, "iPhone 12", 390.dp, 844.dp)
    val iPhone_12_Pro_Max
        get() = DeviceInfo(
            DeviceBrand.Iphone, "iPhone 12 Pro Max", 428.dp, 926.dp
        )
    val iPhone_13_Mini get() = DeviceInfo(DeviceBrand.Iphone, "iPhone 13 Mini", 375.dp, 812.dp)
    val iPhone_14 get() = DeviceInfo(DeviceBrand.Iphone, "iPhone 14", 390.dp, 844.dp)
    val iPhone_14_Pro get() = DeviceInfo(DeviceBrand.Iphone, "iPhone 14 Pro", 393.dp, 852.dp)
    val iPhone_14_Pro_Max
        get() = DeviceInfo(
            DeviceBrand.Iphone, "iPhone 14 Pro Max", 430.dp, 932.dp
        )

    // tablets
    val iPad_Mini get() = DeviceInfo(DeviceBrand.Iphone, "iPad Mini (6th gen)", 744.dp, 1133.dp)
    val iPad_9th_gen get() = DeviceInfo(DeviceBrand.Iphone, "iPad 9th gen", 810.dp, 1080.dp)
    val iPad_Air_4th_gen get() = DeviceInfo(DeviceBrand.Iphone, "iPad Air 4th gen", 820.dp, 1180.dp)
    val iPad_Pro_11_inch get() = DeviceInfo(DeviceBrand.Iphone, "iPad Pro 11-inch", 834.dp, 1194.dp)
    val iPad_Pro_12_9_inch
        get() = DeviceInfo(
            DeviceBrand.Iphone, "iPad Pro 12.9-inch", 1024.dp, 1366.dp
        )


    val Galaxy_S9 get() = DeviceInfo(DeviceBrand.Samsung, "Galaxy S9", 360.dp, 740.dp)
    val Galaxy_S10 get() = DeviceInfo(DeviceBrand.Samsung, "Galaxy S10", 360.dp, 760.dp)
    val Galaxy_S20_Ultra get() = DeviceInfo(DeviceBrand.Samsung, "Galaxy S20 Ultra", 412.dp, 915.dp)
    val Galaxy_S22 get() = DeviceInfo(DeviceBrand.Samsung, "Galaxy S22", 370.dp, 800.dp)
    val Galaxy_Note_10_plus
        get() = DeviceInfo(
            DeviceBrand.Samsung, "Galaxy Note 10+", 412.dp, 898.dp
        )
    val Galaxy_Note_20 get() = DeviceInfo(DeviceBrand.Samsung, "Galaxy Note 20", 412.dp, 883.dp)

    // foldable
    val Galaxy_Z_Fold3_unfolded
        get() = DeviceInfo(
            DeviceBrand.Samsung, "Galaxy Z Fold3 (unfolded)", 832.dp, 2260.dp
        )
    val Galaxy_Z_Fold3_folded
        get() = DeviceInfo(
            DeviceBrand.Samsung, "Galaxy Z Fold3 (folded)", 360.dp, 832.dp
        )

    // tablets
    val Galaxy_Tab_S7 get() = DeviceInfo(DeviceBrand.Samsung, "Galaxy Tab S7", 800.dp, 1280.dp)
    val Galaxy_Tab_S8 get() = DeviceInfo(DeviceBrand.Samsung, "Galaxy Tab S8", 800.dp, 1340.dp)

    val Pixel_3 get() = DeviceInfo(DeviceBrand.Pixel, "Pixel 3", 393.dp, 786.dp)
    val Pixel_3_XL get() = DeviceInfo(DeviceBrand.Pixel, "Pixel 3 XL", 412.dp, 847.dp)
    val Pixel_4_XL get() = DeviceInfo(DeviceBrand.Pixel, "Pixel 4 XL", 411.dp, 869.dp)
    val Pixel_5 get() = DeviceInfo(DeviceBrand.Pixel, "Pixel 5", 393.dp, 851.dp)
    val Pixel_6_Pro get() = DeviceInfo(DeviceBrand.Pixel, "Pixel 6 Pro", 411.dp, 946.dp)
    val Pixel_7 get() = DeviceInfo(DeviceBrand.Pixel, "Pixel 7", 412.dp, 915.dp)
    val Pixel_7_Pro get() = DeviceInfo(DeviceBrand.Pixel, "Pixel 7 Pro", 412.dp, 928.dp)

    val OnePlus_7 get() = DeviceInfo(DeviceBrand.OnePlus, "OnePlus 7", 412.dp, 869.dp)
    val OnePlus_7_Pro get() = DeviceInfo(DeviceBrand.OnePlus, "OnePlus 7 Pro", 412.dp, 899.dp)
    val OnePlus_10_Pro get() = DeviceInfo(DeviceBrand.OnePlus, "OnePlus 10 Pro", 411.dp, 926.dp)

    val Huawei_P30 get() = DeviceInfo(DeviceBrand.Huawei, "Huawei P30", 360.dp, 800.dp)
    val Huawei_P40_Pro get() = DeviceInfo(DeviceBrand.Huawei, "Huawei P40 Pro", 412.dp, 884.dp)
    val Huawei_Mate_30 get() = DeviceInfo(DeviceBrand.Huawei, "Huawei Mate 30", 412.dp, 880.dp)
    val Huawei_Mate_40_Pro
        get() = DeviceInfo(
            DeviceBrand.Huawei, "Huawei Mate 40 Pro", 439.dp, 938.dp
        )
    val Huawei_Mate_X2_unfolded
        get() = DeviceInfo(
            DeviceBrand.Huawei, "Huawei Mate X2 (unfolded)", 882.dp, 2200.dp
        )

    val Xiaomi_Mi_10 get() = DeviceInfo(DeviceBrand.Xiaomi, "Xiaomi Mi 10", 392.dp, 832.dp)
    val Xiaomi_Mi_11 get() = DeviceInfo(DeviceBrand.Xiaomi, "Xiaomi Mi 11", 392.dp, 873.dp)
    val Xiaomi_Redmi_Note_8
        get() = DeviceInfo(
            DeviceBrand.Xiaomi, "Xiaomi Redmi Note 8", 392.dp, 830.dp
        )
    val Sony_Xperia_1 get() = DeviceInfo(DeviceBrand.Sony, "Sony Xperia 1", 360.dp, 820.dp)
}

data class DeviceInfo(
    val brand: DeviceBrand,
    val name: String,
    val screenWidth: Dp,
    val screenHeight: Dp,
    val screenShotRes: DrawableResource? = null
) {
    fun fullName(): String =
        name.plus(" (${screenWidth.value.toInt()}x${screenHeight.value.toInt()})")

    fun fileName(): String =
        name.plus("_${screenWidth.value.toInt()}x${screenHeight.value.toInt()}")
            .replace(" ", "_")
            .lowercase()
}

@Composable
fun DefaultDevicesMap(screenShotRes: DrawableResource? = null) = mapOf(
    // iPhones
    DeviceBrand.Iphone to listOf(
        Devices.iPhone_SE.copy(screenShotRes = screenShotRes),
        Devices.iPhone_11_Pro_Max.copy(screenShotRes = screenShotRes),
        Devices.iPhone_12.copy(screenShotRes = screenShotRes),
        Devices.iPhone_12_Pro_Max.copy(screenShotRes = screenShotRes),
        Devices.iPhone_13_Mini.copy(screenShotRes = screenShotRes),
        Devices.iPhone_14.copy(screenShotRes = screenShotRes),
        Devices.iPhone_14_Pro.copy(screenShotRes = screenShotRes),
        Devices.iPhone_14_Pro_Max.copy(screenShotRes = screenShotRes),

        // tablets
        Devices.iPad_Mini.copy(screenShotRes = screenShotRes),
        Devices.iPad_9th_gen.copy(screenShotRes = screenShotRes),
        Devices.iPad_Air_4th_gen.copy(screenShotRes = screenShotRes),
        Devices.iPad_Pro_11_inch.copy(screenShotRes = screenShotRes),
        Devices.iPad_Pro_12_9_inch.copy(screenShotRes = screenShotRes),
    ),

    // Samsung Galaxy Series
    DeviceBrand.Samsung to listOf(
        Devices.Galaxy_S9.copy(screenShotRes = screenShotRes),
        Devices.Galaxy_S10.copy(screenShotRes = screenShotRes),
        Devices.Galaxy_S20_Ultra.copy(screenShotRes = screenShotRes),
        Devices.Galaxy_S22.copy(screenShotRes = screenShotRes),
        Devices.Galaxy_Note_10_plus.copy(screenShotRes = screenShotRes),
        Devices.Galaxy_Note_20.copy(screenShotRes = screenShotRes),

        // foldable
        Devices.Galaxy_Z_Fold3_unfolded.copy(screenShotRes = screenShotRes),
        Devices.Galaxy_Z_Fold3_folded.copy(screenShotRes = screenShotRes),

        Devices.Galaxy_Tab_S7.copy(screenShotRes = screenShotRes),
        Devices.Galaxy_Tab_S8.copy(screenShotRes = screenShotRes),
    ),

    // Google Pixel Series
    DeviceBrand.Pixel to listOf(
        Devices.Pixel_3.copy(screenShotRes = screenShotRes),
        Devices.Pixel_3_XL.copy(screenShotRes = screenShotRes),
        Devices.Pixel_4_XL.copy(screenShotRes = screenShotRes),
        Devices.Pixel_5.copy(screenShotRes = screenShotRes),
        Devices.Pixel_6_Pro.copy(screenShotRes = screenShotRes),
        Devices.Pixel_7.copy(screenShotRes = screenShotRes),
        Devices.Pixel_7_Pro.copy(screenShotRes = screenShotRes),
    ),

    // OnePlus Series
    DeviceBrand.OnePlus to listOf(
        Devices.OnePlus_7.copy(screenShotRes = screenShotRes),
        Devices.OnePlus_7_Pro.copy(screenShotRes = screenShotRes),
        Devices.OnePlus_10_Pro.copy(screenShotRes = screenShotRes),
    ),

    // Huawei
    DeviceBrand.Huawei to listOf(
        Devices.Huawei_P30.copy(screenShotRes = screenShotRes),
        Devices.Huawei_P40_Pro.copy(screenShotRes = screenShotRes),
        Devices.Huawei_Mate_30.copy(screenShotRes = screenShotRes),
        Devices.Huawei_Mate_40_Pro.copy(screenShotRes = screenShotRes),
        Devices.Huawei_Mate_X2_unfolded.copy(screenShotRes = screenShotRes),
    ),

    // Xiaomi
    DeviceBrand.Xiaomi to listOf(
        Devices.Xiaomi_Mi_10.copy(screenShotRes = screenShotRes),
        Devices.Xiaomi_Mi_11.copy(screenShotRes = screenShotRes),
        Devices.Xiaomi_Redmi_Note_8.copy(screenShotRes = screenShotRes),
        Devices.Sony_Xperia_1.copy(screenShotRes = screenShotRes),
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimulatorsPage(
    state: SimulatorsState = rememberSimulatorsState(),
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

    val leftDrawerState = rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    val screenShotsReady = state.simulatorsStates.all { it.screenShotByteArray != null }

    LaunchedEffect(state.requestToDownloadScreenShots, screenShotsReady) {
        if (state.requestToDownloadScreenShots && screenShotsReady)
            state.downloadScreenShots()
    }

    ModalNavigationDrawer(drawerState = leftDrawerState, drawerContent = {
        ModalDrawerSheet {
            Column(
                modifier = Modifier.width(250.dp)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Devices", style = MaterialTheme.typography.titleLarge)

                state.initDevicesInfoMap.forEach { brand ->

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
                                if (!state.selectedDevices.any { it.name == device.name }) {
                                    state.selectedDevices += device.copy(screenShotRes = state.simulatorsStates.firstOrNull()?.deviceInfo?.screenShotRes)
                                } else
                                    state.selectedDevices =
                                        state.selectedDevices.filter { it.name != device.name }
                                expanded = false // Close the menu after selecting
                            },
                            leadingIcon = {
                                // Add check mark to indicate selection
                                if (state.selectedDevices.any { it.name == device.name }) {
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

            Row {
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

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
                    enabled = !state.requestToDownloadScreenShots,
                    onClick = {
                        if (state.alwaysCaptureScreenShots)
                            scope.launch(DispatchersProvider.Default) {
                                state.downloadScreenShots()
                            }
                        else
                            state.captureAndDownloadScreenShot()
                    }
                ) {
                    Text("Capture Screenshots")
                }

            }

            Spacer(modifier = Modifier.height(4.dp))

            // Display all selected devices in the FlowRow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.selectedSimulatorsStates.forEach {
                        Simulator(
                            state = it,
                            content = content,
                    )
                }
            }
        }
    }
}

@Composable
fun Simulator(
    state: SimulatorState = rememberSimulatorState(),
    content: @Composable (Modifier) -> Unit,
) {
    var alpha by remember { mutableStateOf(0.5f) }

    val contentModifier = Modifier
        .alpha(1 - alpha)
        .fillMaxSize()

    LaunchedEffect(Unit) {
        if (state.alwaysCaptureScreenShots)
            state.captureScreenShot()
    }

    if (state.capturingScreenShot) {
        state.captureScreenShot(content)
    }

    LaunchedEffect(state.requestToDownloadScreenShot, state.screenShotByteArray != null) {
        if (state.requestToDownloadScreenShot && state.screenShotByteArray != null)
            withContext(DispatchersProvider.Default) {
                state.downloadScreenShot()
            }

    }

    state.deviceInfo.apply {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(state.deviceInfo.screenWidth + 32.dp) // 32 for frame 16 left and right
        ) {
            // Screenshot label
            Text(
                text = state.deviceInfo.name.plus(if (screenShotRes != null) " ~ Screenshot" else ""),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (screenShotRes != null)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Symbol(
                        Symbols.AddAPhoto,
                        modifier = Modifier.clickable {
                            state.captureAndDownloadScreenShot()
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
                        state.deviceInfo.screenHeight + 48.dp
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

                    state.deviceInfo.screenShotRes?.let {
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
}

@Composable
fun rememberSimulatorState(
    deviceInfo: DeviceInfo = DeviceInfo(DeviceBrand.Pixel, "Pixel 7", 412.dp, 915.dp),
    alwaysCaptureScreenShots: Boolean = false,
): SimulatorState {
    return remember {
        SimulatorState(
            deviceInfo = deviceInfo,
            alwaysCaptureScreenShots = alwaysCaptureScreenShots
        )
    }
}

class SimulatorState constructor(
    val deviceInfo: DeviceInfo,
    val alwaysCaptureScreenShots: Boolean = false,
) {
    private var _capturingScreenShot by mutableStateOf(false)
    val capturingScreenShot get() = _capturingScreenShot

    private var _screenShotByteArray by mutableStateOf<ByteArray?>(null)
    val screenShotByteArray get() = _screenShotByteArray

    private var _requestToDownloadScreenShot by mutableStateOf(false)
    val requestToDownloadScreenShot get() = _requestToDownloadScreenShot

    fun captureScreenShot() {
        _capturingScreenShot = true
    }

    fun captureAndDownloadScreenShot() {
        _capturingScreenShot = true
        _requestToDownloadScreenShot = true
    }

    @Composable
    fun captureScreenShot(content: @Composable (Modifier) -> Unit) {
        _screenShotByteArray = captureComposableAsBitmap(
            deviceInfo.screenWidth.value.toInt(),
            deviceInfo.screenHeight.value.toInt(),
            content
        )

        if (_screenShotByteArray == null)
            Log.e("SimulatorState", Throwable("Failed to take screenshot"))

        _capturingScreenShot = false
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun downloadScreenShot() {
        _requestToDownloadScreenShot = false
        _screenShotByteArray?.apply {
            when (getPlatform().type) {
                Platform.Type.BROWSER -> {
                    browserDownloadFile(
                        ContentType.Image.PNG.contentType,
                        "${deviceInfo.fileName()}.png",
                        Base64.encode(this)
                    )
                }

                else -> {
                    SystemFileSystem.sink(Path("/tmp/screenshot.png"))
                        .buffered()
                        .use { sink ->
                            sink.write(this)
                        }
                }
            }
        }
    }
}


@Composable
fun rememberSimulatorsState(
    initDevicesInfoMap: Map<DeviceBrand, List<DeviceInfo>> = DefaultDevicesMap(),
    initSelectedDevices: List<DeviceInfo> = listOf(
        Devices.Pixel_3,
        Devices.iPhone_SE,
        Devices.Galaxy_S9,
        Devices.Huawei_P30,
        Devices.Xiaomi_Mi_10,
    ),
    alwaysCaptureScreenShots: Boolean = true,
): SimulatorsState {
    return remember {
        SimulatorsState(
            initDevicesInfoMap = initDevicesInfoMap,
            initSelectedDevices = initSelectedDevices,
            alwaysCaptureScreenShots = alwaysCaptureScreenShots
        )
    }
}

class SimulatorsState(
    val initDevicesInfoMap: Map<DeviceBrand, List<DeviceInfo>>,
    initSelectedDevices: List<DeviceInfo> = listOf(),
    val alwaysCaptureScreenShots: Boolean = false,
) {
    val simulatorsStates =
        initDevicesInfoMap.flatMap { it.value }
            .map {
                SimulatorState(
                    deviceInfo = it,
                    alwaysCaptureScreenShots = alwaysCaptureScreenShots
                )
            }

    var selectedDevices by mutableStateOf(initSelectedDevices)

    private var _requestToDownloadScreenShots by mutableStateOf(false)
    val requestToDownloadScreenShots get() = _requestToDownloadScreenShots

    fun captureScreenShots() {
        simulatorsStates.forEach { it.captureScreenShot() }
    }

    fun captureAndDownloadScreenShot() {
        _requestToDownloadScreenShots = true
        simulatorsStates.forEach {
            it.captureScreenShot()
        }
    }

    val selectedSimulatorsStates
        get() = simulatorsStates.filter { i ->
            selectedDevices.any { it.name == i.deviceInfo.name }
        }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun downloadScreenShots() {
        if (_requestToDownloadScreenShots) return
        _requestToDownloadScreenShots = true
        runCatching {
            when (getPlatform().type) {
                Platform.Type.BROWSER -> {
                    val filesJson = Http.ktorConfigJson.encodeToString(
                        selectedSimulatorsStates.map {
                            mapOf(
                                "filename" to it.deviceInfo.fileName().plus(".png"),
                                "contentBase64" to Base64.encode(
                                    (it.screenShotByteArray ?: "".toByteArray())
                                )
                            )
                        }.toTypedArray()
                    )
//                    Log.d("SimulatorsState",filesJson)
                    browserZipAndDownloadFiles(filesJson)
                }

                else -> {
//                SystemFileSystem.sink(Path("/tmp/screenshot.png"))
//                    .buffered()
//                    .use { sink ->
//                        sink.write(this)
//                    }
                }
            }
        }.getOrElse {
            Log.e("SimulatorState", it)
        }
        _requestToDownloadScreenShots = false

    }
}