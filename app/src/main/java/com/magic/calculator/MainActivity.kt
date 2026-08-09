package com.magic.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.magic.calculator.ui.CalculatorScreen
import com.magic.calculator.ui.MagicCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagicCalculatorTheme {
                CalculatorScreen()
            }
        }
    }
}
