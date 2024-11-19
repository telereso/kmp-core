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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.telereso.kmp.core.ui.compontents.Icon
import io.telereso.kmp.core.ui.compontents.Symbol
import io.telereso.kmp.core.ui.models.ArrowBack
import io.telereso.kmp.core.ui.models.Symbols
import io.telereso.kmp.core.ui.models.Visibility

@Composable
fun LoginPage() {
    val backgroundColor = Color.White
    val primaryTextColor = Color(0xFF1E1E1E)
    val secondaryTextColor = Color(0xFF9E9E9E)
    val accentColor = Color(0xFF00B4D8)
    val buttonColor = Color(0xFF1E1E1E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Symbol(
                Symbols.ArrowBack,
                contentDescription = "Back",
                tint = primaryTextColor,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { /* Handle back navigation */ }
            )
        }

        // Welcome Message
        Text(
            text = "Welcome back! Glad to see you, Again!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = primaryTextColor
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Email & Password Fields
        Column {
            TextField(
                value = "",
                onValueChange = { /* Handle email input */ },
                placeholder = { Text("Enter your email", color = secondaryTextColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            TextField(
                value = "",
                onValueChange = { /* Handle password input */ },
                placeholder = { Text("nter your password", color = secondaryTextColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                trailingIcon = {
                    Symbol(
                        Symbols.Visibility,
                        contentDescription = "Toggle Password Visibility",
                        tint = secondaryTextColor
                    )
                },
                colors = TextFieldDefaults.colors(
//                    containerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = secondaryTextColor,
                    textAlign = TextAlign.End
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clickable { /* Handle Forgot Password */ }
            )
        }

        // Login Button
        Button(
            onClick = { /* Handle Login */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Text(
                text = "Login",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        // Register Now
        Text(
            text = "Don’t have an account? Register Now",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = primaryTextColor
            ),
            modifier = Modifier.clickable { /* Handle Register Now */ },
            textAlign = TextAlign.Center
        )
    }
}
