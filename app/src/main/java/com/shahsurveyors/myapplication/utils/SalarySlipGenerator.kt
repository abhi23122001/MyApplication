package com.shahsurveyors.myapplication.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.shahsurveyors.myapplication.ui.finance.SalaryData
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SalarySlipGenerator {

    fun generatePdf(context: Context, data: SalaryData): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.DEFAULT
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            strokeWidth = 1f
        }

        canvas.drawText("SHAH ERP", 40f, 55f, titlePaint)
        canvas.drawText("SALARY SLIP", 455f, 55f, headingPaint)
        canvas.drawLine(40f, 70f, 555f, 70f, linePaint)

        // SalaryData.month already contains YYYY-MM, so do not append the year twice.
        val monthLabel = if (data.month.isBlank()) data.year.toString() else data.month
        canvas.drawText("Salary Month: $monthLabel", 40f, 95f, textPaint)
        canvas.drawText("Generated: ${SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date())}", 390f, 95f, textPaint)

        canvas.drawText("EMPLOYEE DETAILS", 40f, 130f, headingPaint)
        canvas.drawText("Name: ${data.name}", 40f, 150f, textPaint)
        canvas.drawText("Employee ID: ${data.id}", 40f, 168f, textPaint)
        canvas.drawText("Department: ${data.dept}", 300f, 150f, textPaint)
        canvas.drawText("Pay Type: ${data.payType}", 300f, 168f, textPaint)

        canvas.drawLine(40f, 185f, 555f, 185f, linePaint)
        canvas.drawText("ATTENDANCE & PAY DETAILS", 40f, 210f, headingPaint)

        drawRow(canvas, textPaint, 235f, "Basic Salary", money(data.basicSalary))
        drawRow(canvas, textPaint, 255f, "Present Days", data.presentDays.toString())
        drawRow(canvas, textPaint, 275f, "Approved Leave", data.approvedLeaveDays.toString())
        drawRow(canvas, textPaint, 295f, "Absent Days", data.absentDays.toString())
        drawRow(canvas, textPaint, 315f, "Late Count", data.lateCount.toString())
        drawRow(canvas, textPaint, 335f, "Early Out Count", data.earlyOutCount.toString())
        drawRow(canvas, textPaint, 355f, "Missing Punch Out", data.missingPunchOutCount.toString())
        drawRow(canvas, textPaint, 375f, "Overtime", "${data.overtimeMinutes} min")
        drawRow(canvas, textPaint, 395f, "Overtime Pay", money(data.overtimePay))

        canvas.drawLine(40f, 420f, 555f, 420f, linePaint)
        canvas.drawText("DEDUCTIONS", 40f, 445f, headingPaint)
        drawRow(canvas, textPaint, 470f, "Advance Salary Deduction", money(data.advances))
        val otherDeductions = (data.deductions - data.advances).coerceAtLeast(0.0)
        drawRow(canvas, textPaint, 490f, "Other Deductions", money(otherDeductions))
        drawRow(canvas, textPaint, 510f, "Total Deductions", money(data.deductions))

        canvas.drawLine(40f, 535f, 555f, 535f, linePaint)
        val netPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 110, 35)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("NET SALARY", 40f, 570f, netPaint)
        canvas.drawText(money(data.netSalary), 430f, 570f, netPaint)

        canvas.drawText("This salary slip is generated from the Shah ERP payroll calculation.", 40f, 625f, textPaint)
        canvas.drawLine(40f, 745f, 555f, 745f, linePaint)
        canvas.drawText("Authorized by: ____________________", 360f, 775f, textPaint)
        canvas.drawText("Shah ERP", 40f, 775f, headingPaint)

        document.finishPage(page)
        val file = File(context.cacheDir, "Salary_Slip_${data.id}_${data.year}_${data.month}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun drawRow(canvas: Canvas, paint: Paint, y: Float, label: String, value: String) {
        canvas.drawText(label, 55f, y, paint)
        canvas.drawText(value, 420f, y, paint)
    }

    private fun money(value: Double): String =
        "₹ " + String.format(Locale.ENGLISH, "%,.2f", value)
}
