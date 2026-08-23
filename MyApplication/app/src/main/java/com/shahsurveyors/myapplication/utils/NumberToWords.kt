package com.shahsurveyors.myapplication.utils

object NumberToWords {
    private val units = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
    private val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

    fun convert(number: Double): String {
        val n = number.toLong()
        if (n == 0L) return "Zero"
        
        var words = ""
        if (n >= 1_00_00_000) {
            words += convertPart(n / 1_00_00_000) + " Crore "
            words += convert( (n % 1_00_00_000).toDouble() )
        } else if (n >= 1_00_000) {
            words += convertPart(n / 1_00_000) + " Lakh "
            words += convert( (n % 1_00_000).toDouble() )
        } else if (n >= 1_000) {
            words += convertPart(n / 1_000) + " Thousand "
            words += convert( (n % 1_000).toDouble() )
        } else if (n >= 100) {
            words += convertPart(n / 100) + " Hundred "
            words += convert( (n % 100).toDouble() )
        } else {
            words += convertPart(n)
        }
        
        return words.trim() + " Rupees Only"
    }

    private fun convertPart(n: Long): String {
        var part = ""
        val num = n.toInt()
        if (num < 20) {
            part = units[num]
        } else {
            part = tens[num / 10] + " " + units[num % 10]
        }
        return part.trim()
    }
}
