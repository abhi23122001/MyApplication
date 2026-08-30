package com.shahsurveyors.myapplication.utils

import java.util.Locale
import kotlin.math.roundToLong

object NumberToWords {

    private val units = arrayOf(
        "",
        "One",
        "Two",
        "Three",
        "Four",
        "Five",
        "Six",
        "Seven",
        "Eight",
        "Nine",
        "Ten",
        "Eleven",
        "Twelve",
        "Thirteen",
        "Fourteen",
        "Fifteen",
        "Sixteen",
        "Seventeen",
        "Eighteen",
        "Nineteen"
    )

    private val tens = arrayOf(
        "",
        "",
        "Twenty",
        "Thirty",
        "Forty",
        "Fifty",
        "Sixty",
        "Seventy",
        "Eighty",
        "Ninety"
    )

    /**
     * Converts amount into Indian currency words.
     *
     * Examples:
     * 1000       -> One Thousand Rupees Only
     * 125000     -> One Lakh Twenty Five Thousand Rupees Only
     * 1840800    -> Eighteen Lakh Forty Thousand Eight Hundred Rupees Only
     * 1250.50    -> One Thousand Two Hundred Fifty Rupees and Fifty Paise Only
     */
    fun convert(number: Double): String {

        if (number < 0) {
            return "Minus ${convert(-number)}"
        }

        val totalPaise = (number * 100).roundToLong()

        val rupees = totalPaise / 100
        val paise = totalPaise % 100

        if (rupees == 0L && paise == 0L) {
            return "Zero Rupees Only"
        }

        val result = StringBuilder()

        if (rupees > 0) {
            result.append(convertIndianNumber(rupees))
            result.append(" Rupees")
        }

        if (paise > 0) {

            if (rupees > 0) {
                result.append(" and ")
            }

            result.append(convertIndianNumber(paise))
            result.append(" Paise")
        }

        result.append(" Only")

        return result.toString()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Converts Indian numbering system:
     *
     * Crore
     * Lakh
     * Thousand
     * Hundred
     */
    private fun convertIndianNumber(number: Long): String {

        if (number == 0L) {
            return "Zero"
        }

        var n = number
        val words = StringBuilder()

        // Crore
        if (n >= 1_00_00_000L) {

            val crore = n / 1_00_00_000L

            words.append(
                convertIndianNumber(crore)
            )

            words.append(" Crore ")

            n %= 1_00_00_000L
        }

        // Lakh
        if (n >= 1_00_000L) {

            val lakh = n / 1_00_000L

            words.append(
                convertIndianNumber(lakh)
            )

            words.append(" Lakh ")

            n %= 1_00_000L
        }

        // Thousand
        if (n >= 1_000L) {

            val thousand = n / 1_000L

            words.append(
                convertIndianNumber(thousand)
            )

            words.append(" Thousand ")

            n %= 1_000L
        }

        // Hundred
        if (n >= 100L) {

            val hundred = n / 100L

            words.append(
                convertIndianNumber(hundred)
            )

            words.append(" Hundred ")

            n %= 100L
        }

        // Last two digits
        if (n > 0L) {

            if (words.isNotEmpty()) {
                words.append("")
            }

            words.append(convertBelowHundred(n))
        }

        return words
            .toString()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun convertBelowHundred(number: Long): String {

        return when {
            number < 20L -> {
                units[number.toInt()]
            }

            else -> {
                val ten = number / 10
                val unit = number % 10

                if (unit == 0L) {
                    tens[ten.toInt()]
                } else {
                    "${tens[ten.toInt()]} ${units[unit.toInt()]}"
                }
            }
        }
    }

    /**
     * Convenience function for integer amounts.
     */
    fun convert(number: Long): String {
        return convert(number.toDouble())
    }

    /**
     * Returns currency format with Indian Rupee symbol.
     *
     * Example:
     * ₹ 18,40,800.00
     */
    fun formatIndianCurrency(amount: Double): String {

        return String.format(
            Locale.US,
            "₹ %,.2f",
            amount
        )
    }
}