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

package io.telereso.kmp.core.preview.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.Composable


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import io.telereso.kmp.core.icons.MaterialIcons
import io.telereso.kmp.core.icons.resources.Check
import io.telereso.kmp.core.icons.resources.Delete
import io.telereso.kmp.core.preview.pages.MySymbolConfig.Companion.defaultHost
import io.telereso.kmp.core.preview.pages.MySymbolConfig.Companion.iconsHost
import io.telereso.kmp.core.preview.resources.Res
import io.telereso.kmp.core.ui.compontents.Icon
import io.telereso.kmp.core.ui.compontents.Symbol
import io.telereso.kmp.core.ui.models.Close
import io.telereso.kmp.core.ui.models.ContentCopy
import io.telereso.kmp.core.ui.models.SymbolConfig
import io.telereso.kmp.core.ui.models.SymbolConfig.Grade
import io.telereso.kmp.core.ui.models.SymbolConfig.Size
import io.telereso.kmp.core.ui.models.SymbolConfig.Type
import io.telereso.kmp.core.ui.models.SymbolConfig.Weight
import io.telereso.kmp.core.ui.models.Symbols
import io.telereso.kmp.core.ui.models.Weight
import io.telereso.kmp.core.ui.models.getUrlFromSymbolConfig
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class, ExperimentalRichTextApi::class)
@Composable
fun SymbolsPreviewPage() {

    var symbols by remember { mutableStateOf<List<MySymbolConfig>>(emptyList()) }
    var selected by remember { mutableStateOf<MySymbolConfig?>(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var codeSnippet by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var customization by remember {
        mutableStateOf(
            MySymbolConfig(
                name = "symbols",
                type = Type.OUTLINED,
                isFilled = false,
                size = Size._24,
                grade = null,
                weight = null
            )
        )
    }

    LaunchedEffect(selected) {
        if (selected == null) {
            drawerState.close()
        } else {
            drawerState.open()

            fun setSymbolCode() {
                val s = selected!!
                var fillText = ""
                var sizeText = ""
                var gradeText = ""
                var weightText = ""
                val intend = "            "
                if (s.isFilled) {
                    fillText = "\n$intend.filled()"
                }
                if (s.size != Size._24) {
                    sizeText = "\n$intend.size(Size.${s.size.name})"
                }
                if (s.grade != null) {
                    gradeText = "\n$intend.grade(Grade.${s.grade.name})"
                }
                if (s.weight != null) {
                    weightText = "\n$intend.weight(Weight.${s.weight.name})"
                }

                codeSnippet =
                    "Symbol(\n    Symbols.${s.name.symbolName()}$fillText$sizeText$weightText$gradeText\n)"
            }

            fun setIconCode() {
                val s = selected!!
                codeSnippet =
                    "Icon(\n    MaterialIcons.${s.name.symbolName()}\n)"
            }

            if (customization.host == iconsHost) {
                setIconCode()
            } else {
                setSymbolCode()
            }
        }
    }

    LaunchedEffect(customization.host) {
        if (customization.host == iconsHost) scope.launch {
            symbols = Res.readBytes("files/iconNames.txt").decodeToString().split(",").map {
                MySymbolConfig(it.trim().symbolName(), host = iconsHost)
            }
        } else {
            symbols = Res.readBytes("files/symbolsNames.txt").decodeToString().split(",").map {
                MySymbolConfig(it.trim().lowercase().removePrefix("_"))
            }
        }
    }

    LaunchedEffect(customization) {

        if (customization.host != iconsHost) {
            SymbolConfig.setHost(customization.host)
            SymbolConfig.setProtocol(customization.protocol)
            symbols = symbols.map {
                customization.copy(name = it.name)
            }
        }

    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {

            ModalDrawerSheet {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Import",
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = {
                                selected = null
                            }) {
                                Symbol(Symbols.Close)
                            }
                        }

                        SelectionContainer {
                            CodeSnippetWithCopy(
                                code = if (customization.host == iconsHost) importIcons else importSymbols,
                            )
                        }


                        Text(
                            "Code",
                            style = MaterialTheme.typography.titleMedium
                        )

                        SelectionContainer {
                            CodeSnippetWithCopy(
                                code = codeSnippet,
                            )
                        }

                        if (customization.host != iconsHost) {
                            Text(
                                "Source",
                                style = MaterialTheme.typography.titleMedium
                            )

                            SelectionContainer {
                                selected?.let { s ->
                                    CodeSnippetWithCopy(
                                        code = getUrlFromSymbolConfig(
                                            name = s.name,
                                            size = s.size,
                                            weight = s.weight,
                                            grade = s.grade,
                                            type = s.type,
                                            isFilled = s.isFilled
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Panel - Customize Section
                    Box(
                        modifier = Modifier.width(250.dp)
                            .fillMaxHeight()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CustomizeSection(customization) { newValue ->
                            customization = newValue
                        }
                    }

                    // Center Panel - Icons Grid
                    Box(
                        modifier = Modifier.fillMaxHeight().padding(16.dp).weight(1f)
                    ) {
                        SymbolGrid(symbols) { symbol ->
                            selected = symbol
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun CustomizeSection(
    customization: MySymbolConfig, onChange: (customization: MySymbolConfig) -> Unit
) {
    val iconsSelected = customization.host == iconsHost


    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!iconsSelected) {
            Text("Customize", style = MaterialTheme.typography.titleLarge)

            // Fill Toggle
            Text("Fill", style = MaterialTheme.typography.labelMedium)
            Switch(checked = customization.isFilled, onCheckedChange = {
                onChange(customization.copy(isFilled = it))
            })
            // Weight Slider
            Text("Weight", style = MaterialTheme.typography.labelMedium)
            StepSliderWithLabel(400, listOf(100, 200, 300, 400, 500, 600, 700)) {
                onChange(
                    customization.copy(
                        weight = if (it == 400) null else SymbolConfig.Weight.valueOf(
                            "_$it"
                        )
                    )
                )

            }

            // Grade Slider
            Text("Grade", style = MaterialTheme.typography.labelMedium)
            StepSliderWithLabel(0, listOf(-25, 0, 200)) {
                onChange(
                    customization.copy(
                        grade = when (it) {
                            -25 -> SymbolConfig.Grade.N25
                            200 -> SymbolConfig.Grade._200
                            else -> null
                        }
                    )
                )
            }

            // Optical Size Slider
            Text("Optical Size", style = MaterialTheme.typography.labelMedium)
            StepSliderWithLabel(24, listOf(20, 24, 40, 48)) {
                onChange(customization.copy(size = SymbolConfig.Size.valueOf("_$it")))
            }

            // Filter Section
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("Filter", style = MaterialTheme.typography.titleMedium)

        // Style Dropdown
        DropdownMenuSelector(
            "Material Symbol", listOf("Material Symbols", "Material Icons")
        ) {

            onChange(
                customization.copy(
                    host = if (it == "Material Icons") iconsHost else defaultHost
                )
            )
        }

        if (!iconsSelected) {
            // Style Dropdown
            DropdownMenuSelector(
                "Outlined", listOf("Outlined", "Rounded", "Sharp")
            ) {
                onChange(customization.copy(type = SymbolConfig.Type.valueOf(it.uppercase())))
            }

            Text("Host", style = MaterialTheme.typography.titleMedium)

            TextField(
                customization.host,
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                placeholder = { Text("Source Host") },
                onValueChange = {
                    onChange(customization.copy(host = it))
                }
            )

            Text("Https", style = MaterialTheme.typography.labelMedium)
            Switch(checked = customization.protocol == "https", onCheckedChange = {
                onChange(customization.copy(protocol = if (it) "https" else "http"))
            })
        }
    }

}

@Composable
fun SliderWithLabel(min: Int, max: Int) {
    var sliderPosition by remember { mutableStateOf((max - min) / 2f) }
    Column {
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = min.toFloat()..max.toFloat(),
        )
        Text(text = "${sliderPosition.toInt()}", fontSize = 12.sp)
    }
}

@Composable
fun StepSliderWithLabel(
    defaultStep: Int, steps: List<Int>, onValueChangeFinished: (Int) -> Unit
) {
    var selectedIndex by remember { mutableStateOf(steps.indexOf(defaultStep)) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Slider
        Slider(value = selectedIndex.toFloat(),
            onValueChange = { selectedIndex = it.toInt() },
            valueRange = 0f..(steps.size - 1).toFloat(),
            steps = steps.size - 2,
            onValueChangeFinished = { onValueChangeFinished(steps[selectedIndex]) })

        // Display the step labels
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEach { step ->
                Text(text = step.toString(), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DropdownMenuSelector(label: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(label) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selectedOption)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    selectedOption = option
                    expanded = false
                    onSelected(option)
                })
            }
        }
    }
}

@Composable
fun SymbolGrid(symbols: List<MySymbolConfig>, onSelected: (MySymbolConfig) -> Unit) {

    var searchKey by remember { mutableStateOf("") }
    val state = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    val filteredSymbols by remember(symbols, searchKey) {
        if (searchKey.isBlank()) mutableStateOf(symbols)
        else mutableStateOf(symbols.filter { it.name.contains(searchKey, true) })
    }


    Column {
        TextField(placeholder = { Text("Search Symbols") }, value = searchKey, onValueChange = {
            searchKey = it
        })

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            state = state,
            columns = GridCells.Adaptive(minSize = 160.dp), // Adjust minSize for column width
            contentPadding = PaddingValues(8.dp),
        ) {
            items(filteredSymbols) { symbol ->
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp).heightIn(min = 120.dp).widthIn(max = 130.dp)
                        .clickable {
                            onSelected(symbol)
                            scope.launch {
//                                state.animateScrollToItem(index)
                            }
                        }) {

                    if (symbol.host == iconsHost) {
                        Icon(symbol.toIcon())
                    } else {
                        Symbol(symbol.toConfig()) // Replace with actual Icon rendering code
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    SelectionContainer {
                        Text(
                            symbol.name,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

data class MySymbolConfig constructor(
    val name: String,
    internal val type: Type = Type.OUTLINED,
    internal val isFilled: Boolean = false,
    internal val size: Size = Size._24,
    internal val grade: Grade? = null,
    internal val weight: Weight? = null,
    internal val host: String = defaultHost,
    internal val protocol: String = "https",
) {
    companion object {
        val defaultHost = "raw.githubusercontent.com/google/material-design-icons/refs/heads/master"
        val iconsHost = "_icons"
    }
    fun toConfig(): SymbolConfig {
        return SymbolConfig(
            name = name,
            type = type,
            isFilled = isFilled,
            size = size,
            grade = grade,
            weight = weight,
        )
    }

    fun toIcon(): DrawableResource {
        return iconMap[name] ?: MaterialIcons.Delete
    }
}

@Composable
fun CodeSnippetWithCopy(code: String) {
    val backgroundColor = Color(0xFFF2F4F8) // Light gray background
    val iconTint = Color(0xFF6A1B9A) // Purple icon color (matching theme)
    val textColor = Color(0xFF333333) // Dark text color

    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(8.dp)).padding(16.dp),
    ) {
        // Code Text
        Text(
            text = code, color = textColor, style = TextStyle(
                fontFamily = FontFamily.Monospace, fontSize = 14.sp
            ), modifier = Modifier
                .wrapContentHeight()
                .horizontalScroll(rememberScrollState())
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Copy button
            IconButton(onClick = {
                clipboardManager.setText(AnnotatedString(code))
            }) {
                Symbol(
                    symbolConfig = Symbols.ContentCopy,
                    contentDescription = "Copy code",
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Copy code", color = iconTint, fontSize = 14.sp
            )
        }

    }
}

fun String.symbolName(): String {
    var str = this.split("_")
        .joinToString("") { it.capitalize() }
    if (str.first().isDigit())
        str = "_$str"
    return str
}


val importSymbols = """
        |// build.gradle.kts
        |kotlin {
        |    sourceSets {
        |        commonMain.dependencies {
        |            implementation("io.telereso.kmp:core-ui:0.5.0")
        |        }   
        |    }
        |}
""".trimMargin()

val importIcons = """
        |// build.gradle.kts
        |kotlin {
        |    sourceSets {
        |        commonMain.dependencies {
        |            implementation("io.telereso.kmp:core-icons:0.5.0")
        |        }   
        |    }
        |}
""".trimMargin()