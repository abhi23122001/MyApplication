package com.shahsurveyors.myapplication.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.shahsurveyors.myapplication.models.AttendanceRecord
import com.shahsurveyors.myapplication.models.ExpenseRecord
import com.shahsurveyors.myapplication.models.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmployeeReportPdfGenerator {
    data class DayRow(val date: String, val status: String, val location: String, val punchIn: String, val punchOut: String)

    fun generatePdf(context: Context, employee: UserProfile, month: String, days: List<DayRow>, expenses: List<ExpenseRecord>): File {
        val document = PdfDocument()
        val width = 595
        val height = 842
        var pageNumber = 0
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = 0f

        val title = paint(18f, true)
        val heading = paint(11f, true)
        val text = paint(8.5f, false)
        val small = paint(7.5f, false)
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.DKGRAY; strokeWidth = 0.8f }

        fun newPage() {
            page?.let { document.finishPage(it) }
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(width, height, pageNumber).create())
            canvas = page!!.canvas
            y = 38f
            canvas!!.drawText("SHAH ERP", 36f, y, title)
            canvas!!.drawText("EMPLOYEE ATTENDANCE & EXPENSE REPORT", 250f, y, heading)
            y += 12f
            canvas!!.drawLine(36f, y, 559f, y, line)
            y += 18f
        }
        fun row(label: String, value: String) {
            canvas!!.drawText(label, 40f, y, heading)
            canvas!!.drawText(value.take(70), 155f, y, text)
            y += 15f
        }
        fun tableHeader(cols: List<Pair<String, Float>>) {
            canvas!!.drawLine(36f, y - 5f, 559f, y - 5f, line)
            cols.forEach { canvas!!.drawText(it.first, it.second, y, heading) }
            y += 13f
            canvas!!.drawLine(36f, y - 5f, 559f, y - 5f, line)
        }
        fun tableRow(values: List<Pair<String, Float>>) {
            if (y > 790f) newPage()
            values.forEach { canvas!!.drawText(it.first.take(24), it.second, y, text) }
            y += 13f
        }

        newPage()
        row("Employee Name", employee.name)
        row("Employee ID", employee.uid)
        row("Department", employee.department)
        row("Report Month", month)
        row("Generated", SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date()))

        y += 5f
        canvas!!.drawText("ATTENDANCE SUMMARY", 40f, y, heading)
        y += 18f
        val present = days.count { it.status == "PRESENT" || it.status == "PUNCHED IN" }
        val leave = days.count { it.status == "APPROVED LEAVE" }
        val absent = days.count { it.status == "ABSENT" }
        row("Present Days", present.toString())
        row("Approved Leave", leave.toString())
        row("Absent Days", absent.toString())
        row("Total Expense Claims", expenses.size.toString())
        row("Total Expense Amount", "₹ ${String.format(Locale.ENGLISH, "%,.2f", expenses.sumOf { it.amount })}")

        y += 5f
        canvas!!.drawText("DAILY ATTENDANCE", 40f, y, heading)
        y += 18f
        tableHeader(listOf("Date" to 40f, "Status" to 105f, "Location" to 195f, "Punch In" to 365f, "Punch Out" to 450f))
        days.forEach { d ->
            if (y > 790f) { newPage(); canvas!!.drawText("DAILY ATTENDANCE (CONTINUED)", 40f, y, heading); y += 18f; tableHeader(listOf("Date" to 40f, "Status" to 105f, "Location" to 195f, "Punch In" to 365f, "Punch Out" to 450f)) }
            tableRow(listOf(d.date to 40f, d.status to 105f, d.location to 195f, d.punchIn to 365f, d.punchOut to 450f))
        }

        y += 10f
        if (y > 760f) newPage()
        canvas!!.drawText("EXPENSE CLAIMS", 40f, y, heading)
        y += 18f
        tableHeader(listOf("Date" to 40f, "Category" to 105f, "Amount" to 245f, "Status" to 335f, "Payment" to 430f))
        expenses.forEach { e ->
            if (y > 790f) { newPage(); canvas!!.drawText("EXPENSE CLAIMS (CONTINUED)", 40f, y, heading); y += 18f; tableHeader(listOf("Date" to 40f, "Category" to 105f, "Amount" to 245f, "Status" to 335f, "Payment" to 430f)) }
            val date = e.date?.toDate()?.let { SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(it) } ?: "-"
            tableRow(listOf(date to 40f, e.category.ifBlank { "Expense" } to 105f, "₹%.2f".format(Locale.ENGLISH, e.amount) to 245f, e.status to 335f, e.paymentStatus to 430f))
        }

        y += 14f
        if (y > 770f) newPage()
        canvas!!.drawLine(36f, y, 559f, y, line)
        y += 16f
        canvas!!.drawText("This report is generated from Shah ERP attendance and expense records.", 40f, y, small)
        page?.let { document.finishPage(it) }
        val safeName = employee.name.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(context.cacheDir, "Employee_Report_${safeName}_$month.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun paint(size: Float, bold: Boolean) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = size
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }
}
