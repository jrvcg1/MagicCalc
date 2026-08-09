package com.magic.calculator.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magic.calculator.logic.CalculatorEngine
import com.magic.calculator.logic.MagicTrickEngine

@Composable
fun CalculatorScreen() {
    val context = LocalContext.current
    val magicEngine = remember { MagicTrickEngine() }

    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var isMagicActive by remember { mutableStateOf(false) }

    fun triggerVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(80)
            }
        }
    }

    fun onDigitPressed(digitChar: Char) {
        if (magicEngine.isForcingActive()) {
            val forcedDigit = magicEngine.getNextForcedDigit()
            if (forcedDigit != null) {
                expression += forcedDigit
            }
            // If spectator completed forced number and taps extra keys, ignore them!
            resultText = ""
            return
        }

        expression += digitChar

        val forcedExpr = magicEngine.checkAndStartForcing(expression)
        if (forcedExpr != null) {
            expression = forcedExpr
        }

        resultText = CalculatorEngine.evaluate(expression)
    }

    fun onOperatorPressed(op: String) {
        if (magicEngine.isForcingActive()) {
            // Ignore extra operator presses while spectator is typing forced digits
            return
        }

        val newExpr = expression + op
        val forcedExpr = magicEngine.checkAndStartForcing(newExpr)
        expression = forcedExpr ?: newExpr

        if (magicEngine.isForcingActive()) {
            resultText = ""
        } else {
            resultText = CalculatorEngine.evaluate(expression)
        }
    }

    fun onBackspacePressed() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            magicEngine.handleBackspace()
            if (expression.isEmpty()) {
                resultText = ""
            } else if (!magicEngine.isForcingActive()) {
                resultText = CalculatorEngine.evaluate(expression)
            }
        }
    }

    fun onClearPressed() {
        expression = ""
        resultText = ""
        magicEngine.resetTrickState()
    }

    fun onEqualsPressed() {
        val magicResult = magicEngine.handleEquals(expression)
        if (magicResult != null) {
            resultText = magicResult
            expression = ""
        } else {
            val evaluated = CalculatorEngine.evaluate(expression)
            if (evaluated.isNotEmpty() && evaluated != "Error") {
                resultText = evaluated
                expression = ""
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackgroundColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle visual indicator for Magic Mode (imperceptible dot at top-left corner)
            if (isMagicActive) {
                val dotColor = if (magicEngine.hasPendingForcedDigits()) Color.White else Color(0xFF3B3E42)
                Box(
                    modifier = Modifier
                        .padding(top = 36.dp, start = 16.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Display Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    // Secondary / Expression line
                    Text(
                        text = expression,
                        color = Color(0xFF9CA3AF),
                        fontSize = if (expression.length > 15) 24.sp else 32.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Result line
                    Text(
                        text = resultText,
                        color = Color.White,
                        fontSize = if (resultText.length > 10) 42.sp else 56.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Keypad Grid (5 Rows x 4 Columns)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1: AC, ( ), %, ÷
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalcButton(
                            text = if (expression.isEmpty() && resultText.isEmpty()) "AC" else "C",
                            containerColor = FunctionButtonColor,
                            contentColor = FunctionTextColor,
                            modifier = Modifier.weight(1f),
                            onLongClick = {
                                isMagicActive = magicEngine.toggleMagicMode()
                                triggerVibration()
                            },
                            onClick = { onClearPressed() }
                        )
                        CalcButton(
                            text = "( )",
                            containerColor = FunctionButtonColor,
                            contentColor = FunctionTextColor,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val openCount = expression.count { it == '(' }
                                val closeCount = expression.count { it == ')' }
                                val toAdd = if (openCount > closeCount && expression.lastOrNull()?.isDigit() == true) ")" else "("
                                onOperatorPressed(toAdd)
                            }
                        )
                        CalcButton(
                            text = "%",
                            containerColor = FunctionButtonColor,
                            contentColor = FunctionTextColor,
                            modifier = Modifier.weight(1f),
                            onClick = { onOperatorPressed("%") }
                        )
                        CalcButton(
                            text = "÷",
                            containerColor = OperatorButtonColor,
                            contentColor = OperatorTextColor,
                            modifier = Modifier.weight(1f),
                            onClick = { onOperatorPressed("÷") }
                        )
                    }

                    // Row 2: 7, 8, 9, ×
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalcButton("7", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('7') }
                        CalcButton("8", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('8') }
                        CalcButton("9", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('9') }
                        CalcButton("×", OperatorButtonColor, OperatorTextColor, Modifier.weight(1f)) { onOperatorPressed("×") }
                    }

                    // Row 3: 4, 5, 6, -
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalcButton("4", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('4') }
                        CalcButton("5", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('5') }
                        CalcButton("6", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('6') }
                        CalcButton("-", OperatorButtonColor, OperatorTextColor, Modifier.weight(1f)) { onOperatorPressed("-") }
                    }

                    // Row 4: 1, 2, 3, +
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalcButton("1", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('1') }
                        CalcButton("2", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('2') }
                        CalcButton("3", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('3') }
                        CalcButton("+", OperatorButtonColor, OperatorTextColor, Modifier.weight(1f)) { onOperatorPressed("+") }
                    }

                    // Row 5: 0, ., ⌫, =
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalcButton("0", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('0') }
                        CalcButton(".", NumberButtonColor, NumberTextColor, Modifier.weight(1f)) { onDigitPressed('.') }
                        IconButton(
                            containerColor = FunctionButtonColor,
                            modifier = Modifier.weight(1f),
                            onClick = { onBackspacePressed() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = FunctionTextColor
                            )
                        }
                        CalcButton("=", EqualsButtonColor, EqualsTextColor, Modifier.weight(1f)) { onEqualsPressed() }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalcButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(containerColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun IconButton(
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() }
    ) {
        content()
    }
}
