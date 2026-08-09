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
     * Generates timestamp based on Date/Time 1 minute in the future without seconds.
     * Format: DMMYYHHmm (9 digits if day < 10) or DDMMYYHHmm (10 digits if day >= 10).
     * Guaranteed to NEVER start with 0.
     */
    fun generateTargetTimestamp(): Long {
        val targetTime = LocalDateTime.now().plusMinutes(1)
        val day = targetTime.dayOfMonth // 1 to 31
        val month = String.format("%02d", targetTime.monthValue)
        val year = targetTime.format(DateTimeFormatter.ofPattern("yy"))
        val hour = String.format("%02d", targetTime.hour)
        val minute = String.format("%02d", targetTime.minute)

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
     * Returns the character to display:
     * - The forced character for the first 3 digits of forcedDigitsString.
     * - The real digit pressed by spectator for subsequent digits (digits 4 to 9).
     * - Null once 9 digits have been entered (ignoring extra digits).
     */
    fun getNextForcedDigit(realDigit: Char): Char? {
        if (!isMagicActive || !isForcingPhase || forcedDigitsString.isEmpty()) {
            return null
        }

        if (spectatorDigitIndex >= 9) {
            return null
        }

        val digitToUse = if (spectatorDigitIndex < 3 && spectatorDigitIndex < forcedDigitsString.length) {
            forcedDigitsString[spectatorDigitIndex]
        } else {
            realDigit
        }
        spectatorDigitIndex++
        return digitToUse
    }

    /**
     * Returns the full complement string required for the trick.
     */
    fun getFullForcedComplement(): String = forcedDigitsString

    /**
     * Replaces the last number in currentExpression with the full forced complement X.
     */
    fun getExpressionWithFullComplement(currentExpression: String): String {
        if (!isForcingPhase || forcedDigitsString.isEmpty()) return currentExpression

        val lastOpIndex = currentExpression.lastIndexOfAny(charArrayOf('+', '×', '-', '÷'))
        return if (lastOpIndex != -1) {
            currentExpression.substring(0, lastOpIndex + 1) + forcedDigitsString
        } else {
            forcedDigitsString
        }
    }

    /**
     * Returns true if forcing is active and fewer than 9 digits have been entered.
     */
    fun hasPendingForcedDigits(): Boolean {
        return isMagicActive && isForcingPhase && spectatorDigitIndex < 9
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
     * Always returns the expected Target Timestamp DDMMYYHH(mm+1).
     */
    fun handleEquals(currentExpression: String): String? {
        if (!isMagicActive) return null

        if (isForcingPhase && initialProduct > 0) {
            val targetTimestampStr = generateTargetTimestamp().toString()
            resetTrickState()
            return targetTimestampStr
        }
        return null
    }

    fun isForcingActive(): Boolean = isMagicActive && isForcingPhase
}
