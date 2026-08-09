package com.magic.calculator.logic

import java.math.BigDecimal
import java.math.RoundingMode

object CalculatorEngine {

    fun evaluate(expression: String): String {
        if (expression.isBlank()) return ""
        
        // Remove any trailing operators so evaluation doesn't fail while typing
        var cleanExpr = expression.trim()
        while (cleanExpr.isNotEmpty() && (
                    cleanExpr.endsWith("×") || 
                    cleanExpr.endsWith("÷") || 
                    cleanExpr.endsWith("+") || 
                    cleanExpr.endsWith("-") || 
                    cleanExpr.endsWith("%") ||
                    cleanExpr.endsWith(".")
                )) {
            cleanExpr = cleanExpr.dropLast(1).trim()
        }
        
        if (cleanExpr.isEmpty()) return ""

        val formattedExpr = cleanExpr
            .replace("×", "*")
            .replace("÷", "/")

        return try {
            val result = parseAndEvaluate(formattedExpr)
            formatResult(result)
        } catch (e: Exception) {
            "" // Never return "Error" to ensure clean UI
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return ""
        val bd = BigDecimal(value).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros()
        val plain = bd.toPlainString()
        return if (plain == "-0") "0" else plain
    }

    private fun parseAndEvaluate(expr: String): Double {
        return Parser(expr).parse()
    }

    private class Parser(private val str: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) return 0.0
                        x /= divisor
                    }
                    eat('%'.code) -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) return 0.0
                        x %= divisor
                    }
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                val sub = str.substring(startPos, pos)
                x = sub.toDoubleOrNull() ?: 0.0
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            return x
        }
    }
}
