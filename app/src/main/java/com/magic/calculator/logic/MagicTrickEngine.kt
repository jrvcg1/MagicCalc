package com.magic.calculator.logic

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MagicTrickEngine {

    var isMagicActive: Boolean = false
        private set

    private var forcedDigitsString: String = ""
    private var spectatorDigitIndex: Int = 0
    private var isForcingPhase: Boolean = false
    private var initialProduct: Long = 0L

    fun toggleMagicMode(): Boolean {
        isMagicActive = !isMagicActive
        resetTrickState()
        return isMagicActive
    }

    fun resetTrickState() {
        forcedDigitsString = ""
        spectatorDigitIndex = 0
        isForcingPhase = false
        initialProduct = 0L
    }

    /**
     * Generates timestamp based on current Date/Time without seconds.
     * Format: DMMYYHHmm (9 digits if day < 10) or DDMMYYHHmm (10 digits if day >= 10).
     * Guaranteed to NEVER start with 0.
     */
    fun generateTargetTimestamp(): Long {
        val now = LocalDateTime.now()
        val day = now.dayOfMonth // 1 to 31
        val month = String.format("%02d", now.monthValue)
        val year = now.format(DateTimeFormatter.ofPattern("yy"))
        val hour = String.format("%02d", now.hour)
        val minute = String.format("%02d", now.minute)

        val timestampStr = "$day$month$year$hour$minute"
        return timestampStr.toLongOrNull() ?: 608260626L
    }

    /**
     * Option B: Triggered when 3 numbers from audience are entered: N1 × N2 × N3 + or N1 × N2 × N3 ×
     * All 3 numbers come 100% from 3 real audience members. No fake constants needed!
     */
    fun checkAndStartForcing(expression: String): String? {
        if (!isMagicActive || isForcingPhase) return null

        val isMult = expression.endsWith("×")
        val isAdd = expression.endsWith("+")

        if (isMult || isAdd) {
            val cleanExpr = expression.dropLast(1)
            val parts = cleanExpr.split("×", "+")
            if (parts.size == 3) {
                val n1 = parts[0].trim().toLongOrNull()
                val n2 = parts[1].trim().toLongOrNull()
                val n3 = parts[2].trim().toLongOrNull()

                if (n1 != null && n2 != null && n3 != null && n1 > 0 && n2 > 0 && n3 > 0) {
                    val targetTimestamp = generateTargetTimestamp()
                    val p = n1 * n2 * n3
                    initialProduct = p

                    if (isAdd) {
                        // SUM MODE: X = TargetTimestamp - (N1 * N2 * N3)
                        val requiredX = targetTimestamp - p
                        forcedDigitsString = requiredX.toString()
                        spectatorDigitIndex = 0
                        isForcingPhase = true
                    } else if (isMult) {
                        // MULTIPLICATION MODE: X = TargetTimestamp / (N1 * N2 * N3)
                        val requiredX = targetTimestamp.toDouble() / p.toDouble()
                        forcedDigitsString = String.format(Locale.US, "%.4f", requiredX)
                        spectatorDigitIndex = 0
                        isForcingPhase = true
                    }
                }
            }
        }
        return null
    }

    /**
     * Called when a spectator presses any digit key ('0'..'9') during forcing mode.
     * Returns the next forced character (digit or decimal point), or null if forcing is not active.
     */
    fun getNextForcedDigit(): Char? {
        if (!isMagicActive || !isForcingPhase || forcedDigitsString.isEmpty()) {
            return null
        }

        if (spectatorDigitIndex < forcedDigitsString.length) {
            val digit = forcedDigitsString[spectatorDigitIndex]
            spectatorDigitIndex++
            return digit
        }
        return null
    }

    /**
     * Returns true if forcing is active and there are still forced digits left to be typed by spectator.
     */
    fun hasPendingForcedDigits(): Boolean {
        return isMagicActive && isForcingPhase && spectatorDigitIndex < forcedDigitsString.length
    }

    /**
     * Called when backspace is pressed during spectator digit entry.
     */
    fun handleBackspace() {
        if (isForcingPhase && spectatorDigitIndex > 0) {
            spectatorDigitIndex--
        }
    }

    /**
     * Evaluates the final result when '=' is pressed.
     * Evaluates the actual mathematical expression so that it matches 100% on any device.
     */
    fun handleEquals(currentExpression: String): String? {
        if (!isMagicActive) return null

        if (isForcingPhase && initialProduct > 0) {
            val evaluated = CalculatorEngine.evaluate(currentExpression)
            resetTrickState()
            if (evaluated.isNotEmpty()) {
                return evaluated
            }
            return generateTargetTimestamp().toString()
        }
        return null
    }

    fun isForcingActive(): Boolean = isMagicActive && isForcingPhase
}
