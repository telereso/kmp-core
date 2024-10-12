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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.sebastianneubauer.jsontree.JsonSearchResult
import com.sebastianneubauer.jsontree.JsonSearchResultState
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.rememberJsonSearchResultState
import io.telereso.kmp.core.Difference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import io.telereso.kmp.core.compareJson


val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}


@Composable
fun handleState(state: String, jsonElement: MutableState<JsonElement?>) {
    LaunchedEffect(state) {
        runCatching {
            jsonElement.value = json.parseToJsonElement(state)
        }
    }
}

@Composable
fun JsonEditor(state: MutableState<String>, jsonSearchResultState: JsonSearchResultState) {

    val width = 300.dp

    Column {

        Box(
            modifier = Modifier
                .width(width)
                .height(400.dp)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp) // Rounded corners with 8.dp radius
                ) // Add an outline with primary color
                .padding(4.dp),
        ) {
            JsonTree(
                json = state.value,
                jsonSearchResultState = jsonSearchResultState,
                onLoading = {},
                onError = { },
                initialState = TreeState.EXPANDED,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            modifier = Modifier.width(width).height(300.dp)
                .verticalScroll(rememberScrollState()),
            value = state.value,
            onValueChange = {
                state.value = it
            }
        )

    }
}

@Composable
fun App() {

    var differences by remember { mutableStateOf(listOf<Difference>()) }
    var currentDifference by remember { mutableStateOf(-1) }
    val diffState = rememberRichTextState()

    fun prevDifference(): Int {
        when {
            differences.isEmpty() -> currentDifference = -1
            currentDifference == 0 -> currentDifference = differences.lastIndex
            else -> currentDifference--
        }
        return currentDifference
    }

    fun nextDifference(): Int {
        when {
            differences.isEmpty() -> currentDifference = -1
            currentDifference == differences.lastIndex -> currentDifference = 0
            else -> currentDifference++
        }
        return currentDifference
    }


    val state1 = remember { mutableStateOf(example) }
    val state2 = remember { mutableStateOf("") }

    val jsonElement1 = remember(state1) { mutableStateOf<JsonElement?>(null) }
    val jsonElement2 = remember(state2) { mutableStateOf<JsonElement?>(null) }

    val jsonSearchResult = rememberJsonSearchResultState()

    handleState(state1.value, jsonElement1)
    handleState(state2.value, jsonElement2)

    LaunchedEffect(jsonElement1.value, jsonElement2.value) {
        withContext(Dispatchers.Default) {
            val je1 = jsonElement1.value ?: return@withContext
            val je2 = jsonElement2.value ?: return@withContext
            val result = compareJson(je1, je2)


            differences = result.differences
            diffState.setHtml("<ul>" + differences.joinToString("\n") { "<li>${it.path}</li>" } + "</ul>")

        }
    }


    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Telereso Json Compare")
        }

        Column(
            modifier = Modifier
                .weight(3f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                JsonEditor(state1, jsonSearchResult)

                Spacer(modifier = Modifier.width(24.dp))

                JsonEditor(state2, jsonSearchResult)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {

            if (differences.isNotEmpty())
                Row(
                    modifier = Modifier
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text("Diff")

                    IconButton(onClick = {
                        jsonSearchResult.state =
                            JsonSearchResult(jsonQuery = differences[prevDifference()].path)
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "prev"
                        )
                    }

                    Text("Found: ${currentDifference + 1}/${differences.size}")

                    IconButton(onClick = {
                        jsonSearchResult.state =
                            JsonSearchResult(jsonQuery = differences[nextDifference()].path)
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "next"
                        )
                    }
                }


            Spacer(modifier = Modifier.height(24.dp))

            RichText(
                diffState,
                modifier = Modifier.fillMaxWidth()
            )
        }


    }

}


val example = """{
  "string": "This is a string",
  "number": 123,
  "boolean": true,
  "nullValue": null,
  "object": {
    "key1": "value1",
    "key2": 2,
    "key3": false,
    "nestedObject": {
      "nestedKey1": "nestedValue1",
      "nestedKey2": 42
    }
  },
  "arrayOfObjects": [
    {
      "object1Key1": "value1",
      "object1Key2": true
    },
    {
      "object2Key1": 123,
      "object2Key2": null,
      "nestedArrayInObject": [
        "stringInArray",
        456,
        false,
        { "deepNestedKey": "deepNestedValue" }
      ]
    }
  ],
  "arrayOfPrimitives": [
    "stringInArray",
    456,
    true,
    null
  ],
  "nestedArray": [
    [
      "nestedArrayValue1",
      "nestedArrayValue2"
    ],
    [
      { "deepNestedArrayObject": 999 },
      false
    ]
  ],
  "complexObject": {
    "simpleKey": "simpleValue",
    "arrayInObject": [
      {
        "nestedArrayObjectKey": "nestedObjectValue",
        "moreNestedArrays": [
          {
            "deepArrayObject": "deepValue"
          },
          101
        ]
      }
    ]
  }
}"""