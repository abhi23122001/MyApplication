package com.shahsurveyors.myapplication.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.models.PayrollRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SalarySlipGenerator {

    /**
     * Generates a professional PDF Salary Slip for an employee.
     */
    fun generateSalarySlipPdf(
        context: Context,
        record: PayrollRecord,
        company: CompanyProfile = CompanyProfile()
    ): File {
        val pdfDocument = PdfDocument()

        // Standard A4 dimensions (595 x 842 points)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.DEFAULT
        }

        // 1. Header Background & Company Details
        paint.color = Color.parseColor("#1B5E20") // ShahDarkGreen
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        // Company Title
        paint.color = Color.WHITE
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(company.name.uppercase(Locale.ENGLISH), 297.5f, 38f, paint)

        // Subtitle / Address
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(company.address, 297.5f, 58f, paint)
        canvas.drawText("Email: ${company.email} | Phone: ${company.phone}", 297.5f, 74f, paint)

        // 2. Slip Title Banner
        paint.color = Color.parseColor("#2E7D32") // ShahGreen
        canvas.drawRect(40f, 115f, 555f, 145f, paint)

        paint.color = Color.WHITE
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val slipTitle = "SALARY SLIP FOR THE MONTH OF ${record.monthName.uppercase(Locale.ENGLISH)}"
        canvas.drawText(slipTitle, 297.5f, 135f, paint)

        paint.textAlign = Paint.Align.LEFT

        // 3. Employee Info Grid Box
        var y = 170f
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(40f, y, 555f, y + 80f, 8f, 8f, paint)

        paint.color = Color.parseColor("#E0E0E0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(40f, y, 555f, y + 80f, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Column 1: Name & ID
        drawLabelValue(canvas, textPaint, "Employee Name:", record.name, 55f, y + 25f)
        drawLabelValue(canvas, textPaint, "Employee ID:", record.employeeId.ifBlank { "N/A" }, 55f, y + 45f)
        drawLabelValue(canvas, textPaint, "Department:", record.dept, 55f, y + 65f)

        // Column 2: Role & Effective Period
        drawLabelValue(canvas, textPaint, "Designation / Role:", record.role.ifBlank { "Staff" }, 320f, y + 25f)
        drawLabelValue(canvas, textPaint, "Effective Period:", record.effectiveSalaryPeriod.ifBlank { "Full Month" }, 320f, y + 45f)
        drawLabelValue(canvas, textPaint, "Pay Status:", record.status, 320f, y + 65f)

        // 4. Attendance Summary Box
        y += 95f
        paint.color = Color.parseColor("#F9F9F9")
        canvas.drawRoundRect(40f, y, 555f, y + 45f, 6f, 6f, paint)
        paint.color = Color.parseColor("#E0E0E0")
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(40f, y, 555f, y + 45f, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        textPaint.textSize = 9f
        textPaint.color = Color.parseColor("#555555")
        canvas.drawText("Working Days: ${record.workingDaysInMonth}", 55f, y + 26f, textPaint)
        canvas.drawText("Present: ${record.presentDays} days", 165f, y + 26f, textPaint)
        canvas.drawText("Approved Leave: ${record.approvedLeaveDays} days", 275f, y + 26f, textPaint)
        canvas.drawText("Absent: ${record.absentDays} days", 410f, y + 26f, textPaint)
        canvas.drawText("Overtime: ${record.overtimeHours} hrs", 490f, y + 26f, textPaint)

        // 5. Earnings and Deductions Table
        y += 60f

        // Table Headers
        paint.color = Color.parseColor("#E8F5E9") // Light green background
        canvas.drawRect(40f, y, 290f, y + 25f, paint) // Earnings header
        canvas.drawRect(305f, y, 555f, y + 25f, paint) // Deductions header

        paint.color = Color.parseColor("#1B5E20")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("EARNINGS", 55f, y + 17f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (₹)", 275f, y + 17f, paint)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("DEDUCTIONS", 320f, y + 17f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (₹)", 540f, y + 17f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Items Row 1
        y += 35f
        textPaint.textSize = 9.5f
        textPaint.color = Color.BLACK
        canvas.drawText("Basic Monthly Salary", 55f, y, textPaint)
        drawRightText(canvas, textPaint, formatCurrency(record.baseMonthlySalary), 275f, y)

        canvas.drawText("Absence Deduction (${record.absentDays} days)", 320f, y, textPaint)
        drawRightText(canvas, textPaint, formatCurrency(record.absenceDeduction), 540f, y)

        // Items Row 2
        y += 22f
        canvas.drawText("Overtime Pay (${record.overtimeHours} hrs)", 55f, y, textPaint)
        drawRightText(canvas, textPaint, formatCurrency(record.overtimePay), 275f, y)

        canvas.drawText("Advance Salary EMI", 320f, y, textPaint)
        drawRightText(canvas, textPaint, formatCurrency(record.advanceDeduction), 540f, y)

        // Items Row 3
        y += 22f
        canvas.drawText("Other Allowances", 55f, y, textPaint)
        drawRightText(canvas, textPaint, formatCurrency(0.0), 275f, y)

        canvas.drawText("Other Deductions", 320f, y, textPaint)
        drawRightText(canvas, textPaint, formatCurrency(record.otherDeductions), 540f, y)

        // Divider
        y += 15f
        paint.color = Color.parseColor("#BDBDBD")
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y, 290f, y, paint)
        canvas.drawLine(305f, y, 555f, y, paint)

        // Totals Row
        y += 20f
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText("Gross Earnings", 55f, y, paint)
        drawRightText(canvas, paint, formatCurrency(record.grossSalaryEarned), 275f, y)

        canvas.drawText("Total Deductions", 320f, y, paint)
        drawRightText(canvas, paint, formatCurrency(record.totalDeductions), 540f, y)

        // 6. Net Salary Highlight Banner
        y += 35f
        paint.color = Color.parseColor("#E8F5E9")
        canvas.drawRoundRect(40f, y, 555f, y + 55f, 8f, 8f, paint)

        paint.color = Color.parseColor("#2E7D32")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(40f, y, 555f, y + 55f, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#1B5E20")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NET PAYABLE AMOUNT:", 55f, y + 33f, paint)

        paint.textSize = 16f
        drawRightText(canvas, paint, "₹ ${formatCurrency(record.netSalary)}", 540f, y + 34f)

        // Amount in Words
        y += 75f
        val amountInWords = try {
            NumberToWords.convert(record.netSalary.toLong()) + " Rupees Only"
        } catch (e: Exception) {
            "Rupees ${formatCurrency(record.netSalary)} Only"
        }

        textPaint.textSize = 9.5f
        textPaint.color = Color.parseColor("#333333")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Amount in Words: $amountInWords", 45f, y, textPaint)

        // 7. Signature & Footer
        y = 750f
        paint.color = Color.parseColor("#CCCCCC")
        canvas.drawLine(40f, y, 555f, y, paint)

        textPaint.textSize = 8.5f
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = Color.parseColor("#777777")
        val printDate = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH).format(Date())
        canvas.drawText("Generated on: $printDate | Shah ERP", 45f, y + 20f, textPaint)
        canvas.drawText(company.footerText, 45f, y + 35f, textPaint)

        drawRightText(canvas, textPaint, "Authorized Signatory", 540f, y + 20f)
        drawRightText(canvas, textPaint, "Shah Surveyors & Consultancy", 540f, y + 35f)

        pdfDocument.finishPage(page)

        // Save PDF file
        val outputDir = File(context.filesDir, "salary_slips").apply {
            if (!exists()) mkdirs()
        }
        val safeName = record.name.replace("\\s+".toRegex(), "_")
        val outputFile = File(outputDir, "SalarySlip_${safeName}_${record.salaryMonth}.pdf")

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawLabelValue(
        canvas: android.graphics.Canvas,
        paint: Paint,
        label: String,
        value: String,
        x: Float,
        y: Float
    ) {
        val originalTypeface = paint.typeface
        val originalColor = paint.color

        paint.color = Color.parseColor("#777777")
        paint.textSize = 9f
        canvas.drawText(label, x, y, paint)

        val labelWidth = paint.measureText(label)
        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(" $value", x + labelWidth, y, paint)

        paint.typeface = originalTypeface
        paint.color = originalColor
    }

    private fun drawRightText(
        canvas: android.graphics.Canvas,
        paint: Paint,
        text: String,
        rightX: Float,
        y: Float
    ) {
        val originalAlign = paint.textAlign
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(text, rightX, y, paint)
        paint.textAlign = originalAlign
    }

    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.ENGLISH, "%,.2f", amount)
    }
}
