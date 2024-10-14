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

package io.telereso.kmp.core.ui.preview

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import coil3.compose.AsyncImage

@Composable
fun CorePreview() {
    Text("test")

    AsyncImage(
        model = "https://i.imgur.com/fHyEMsl.jpg",
        contentDescription = null,
    )
}

val example = """
    {
      "name": "Sample Object",
      "age": 30,
      "isVerified": true,
      "preferences": {
        "notifications": {
          "email": true,
          "sms": false,
          "push": {
            "enabled": true,
            "frequency": "daily",
            "categories": [
              "news",
              "updates",
              {
                "sports": [
                  "football",
                  "basketball",
                  "tennis"
                ]
              }
            ]
          }
        },
        "language": "English"
      },
      "tags": ["developer", "json", "test"],
      "accounts": [
        {
          "platform": "Twitter",
          "username": "@sample_user",
          "stats": {
            "followers": 1200,
            "following": 300,
            "verified": false
          }
        },
        {
          "platform": "GitHub",
          "username": "sampleDev",
          "repos": [
            {
              "name": "project-1",
              "private": false,
              "stars": 150
            },
            {
              "name": "project-2",
              "private": true,
              "stars": 90
            }
          ]
        }
      ],
      "settings": {
        "theme": "dark",
        "layout": {
          "sidebar": true,
          "compact": false
        }
      },
      "meta": {
        "createdAt": "2024-10-07T10:00:00Z",
        "updatedAt": "2024-10-07T12:00:00Z",
        "valid": null
      },
      "complexArray": [
        {
          "id": 1,
          "data": [
            {"key": "value1", "numbers": [1, 2, 3]},
            {"key": "value2", "numbers": [4, 5, 6]}
          ]
        },
        {
          "id": 2,
          "data": [
            {"key": "value3", "numbers": [7, 8, 9]},
            {"key": "value4", "numbers": [10, 11, 12]}
          ]
        }
      ],
      "booleanFlag": false,
      "rating": 4.5,
      "nullField": null,
      "multiNested": {
        "level1": {
          "level2": {
            "level3": {
              "key": "deepValue",
              "array": [
                {"prop": "nested1"},
                {"prop": "nested2"},
                {"prop": "nested3"}
              ]
            }
          }
        }
      }
    }

""".trimIndent()
