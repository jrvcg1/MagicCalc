package com.magic.calculator.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackgroundColor = Color(0xFF1E1F22)
val NumberButtonColor = Color(0xFF2E3033)
val FunctionButtonColor = Color(0xFF3A3D40)
val OperatorButtonColor = Color(0xFF004A77)
val EqualsButtonColor = Color(0xFFA8C7FA)

val NumberTextColor = Color(0xFFE2E2E6)
val FunctionTextColor = Color(0xFFD3E3FD)
val OperatorTextColor = Color(0xFFD3E3FD)
val EqualsTextColor = Color(0xFF042E6F)

private val DarkColorScheme = darkColorScheme(
    background = DarkBackgroundColor,
    surface = DarkBackgroundColor,
    onBackground = NumberTextColor,
    onSurface = NumberTextColor
)

@Composable
fun MagicCalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
