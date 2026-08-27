package com.shahsurveyors.myapplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.shahsurveyors.myapplication.R
import com.shahsurveyors.myapplication.data.local.BankDetails
import com.shahsurveyors.myapplication.data.local.BillingItemEntity
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.models.DocType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BillingDocumentGenerator {

    data class DocumentData(
        val company: CompanyProfile,
        val bank: BankDetails,
        val clientName: String,
        val clientAddress: String,
        val clientGstin: String?,
        val docType: DocType,
        val docNumber: String,
        val date: Long = System.currentTimeMillis(),
        val gstType: String,
        val gstPercentage: Double = 18.0,
        val items: List<BillingItemEntity>,
        val terms: List<String>
    )

    fun generatePdf(context: Context, data: DocumentData): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.DEFAULT
        }

        drawHeader(canvas, context, data.company)

        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        val title = when (data.docType) {
            DocType.TAX_INVOICE -> "TAX INVOICE"
            DocType.NON_GST_BILL -> "BILL"
            DocType.QUOTATION -> "QUOTATION"
        }
        canvas.drawText(title, 297f, 160f, paint)
        paint.textAlign = Paint.Align.LEFT

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Date: ${sdf.format(Date(data.date))}", 430f, 185f, paint)
        canvas.drawText("No: ${data.docNumber}", 430f, 200f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TO:", 40f, 185f, paint)
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(data.clientName, 40f, 200f, paint)
        canvas.drawText(data.clientAddress, 40f, 215f, paint)
        data.clientGstin?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("GSTIN: $it", 40f, 230f, paint)
        }

        if (data.docType == DocType.QUOTATION) {
            canvas.drawText("Dear Sir,", 40f, 260f, paint)
            canvas.drawText(
                "We thank you for the enquiry and are pleased to offer our prices for your kind consideration.",
                40f, 275f, paint
            )
        }

        val startY = 300f
        drawTable(canvas, data.items, startY)
        val tableBottom = startY + 25f + (data.items.size * 20f) + 20f
        val subTotal = data.items.sumOf { it.amount }
        var total = subTotal
        var calcY = tableBottom + 20f

        if (data.docType != DocType.NON_GST_BILL) {
            val gstRate = data.gstPercentage / 100.0
            if (data.gstType == "IGST") {
                val igst = subTotal * gstRate
                canvas.drawText("IGST (${data.gstPercentage}%):", 380f, calcY, textPaint)
                canvas.drawText(formatCurrency(igst), 480f, calcY, textPaint)
                total += igst
                calcY += 15f
            } else {
                val cgst = subTotal * (gstRate / 2)
                val sgst = subTotal * (gstRate / 2)
                canvas.drawText("CGST (${data.gstPercentage / 2}%):", 380f, calcY, textPaint)
                canvas.drawText(formatCurrency(cgst), 480f, calcY, textPaint)
                calcY += 15f
                canvas.drawText("SGST (${data.gstPercentage / 2}%):", 380f, calcY, textPaint)
                canvas.drawText(formatCurrency(sgst), 480f, calcY, textPaint)
                total += cgst + sgst
                calcY += 15f
            }
        }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GRAND TOTAL:", 380f, calcY + 5f, paint)
        canvas.drawText(formatCurrency(total), 480f, calcY + 5f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Amount in words: ${NumberToWords.convert(total)}", 40f, calcY + 25f, paint)

        calcY += 60f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TERMS & CONDITIONS:", 40f, calcY, paint)
        paint.typeface = Typeface.DEFAULT
        var termY = calcY + 15f
        data.terms.forEachIndexed { index, term ->
            canvas.drawText("${index + 1}. $term", 50f, termY, paint)
            termY += 15f
        }

        val bankY = 700f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BANK DETAILS:", 40f, bankY, paint)
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Bank: ${data.bank.bankName}", 40f, bankY + 15f, paint)
        canvas.drawText("A/C No: ${data.bank.accountNumber}", 40f, bankY + 30f, paint)
        canvas.drawText("IFSC: ${data.bank.ifscCode} | Branch: ${data.bank.branchAddress}", 40f, bankY + 45f, paint)

        drawBranding(canvas, context, data.company)

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(data.company.footerText, 297f, 830f, paint)
        paint.textAlign = Paint.Align.LEFT

        pdfDocument.finishPage(page)
        val fileName = "SSC_${title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { pdfDocument.writeTo(it) }
        pdfDocument.close()
        return file
    }

    private fun drawHeader(canvas: Canvas, context: Context, company: CompanyProfile) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Official app logo: always use app_logo.png for exported PDFs.
        loadDrawableBitmap(context, R.drawable.app_logo)?.let { bitmap ->
            val size = 80
            val scaled = Bitmap.createScaledBitmap(bitmap, size, size, true)
            canvas.drawBitmap(scaled, 40f, 40f, paint)
            if (scaled !== bitmap) scaled.recycle()
        }

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.BLACK
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(company.name, 297f, 60f, paint)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("ISO 9001:2015 CERTIFIED", 297f, 75f, paint)
        canvas.drawText(company.address, 297f, 90f, paint)
        canvas.drawText("Email: ${company.email} | Mobile: ${company.phone}", 297f, 105f, paint)
        paint.strokeWidth = 1f
        canvas.drawLine(40f, 120f, 555f, 120f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawTable(canvas: Canvas, items: List<BillingItemEntity>, startY: Float) {
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.BLACK
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.DEFAULT
        }
        val tableHeight = 25f + (items.size * 20f)
        canvas.drawRect(40f, startY, 555f, startY + tableHeight, borderPaint)
        canvas.drawLine(40f, startY + 25f, 555f, startY + 25f, borderPaint)
        canvas.drawLine(80f, startY, 80f, startY + tableHeight, borderPaint)
        canvas.drawLine(350f, startY, 350f, startY + tableHeight, borderPaint)
        canvas.drawLine(400f, startY, 400f, startY + tableHeight, borderPaint)
        canvas.drawLine(440f, startY, 440f, startY + tableHeight, borderPaint)
        canvas.drawLine(490f, startY, 490f, startY + tableHeight, borderPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("S.No", 45f, startY + 17f, textPaint)
        canvas.drawText("Description", 85f, startY + 17f, textPaint)
        canvas.drawText("Unit", 355f, startY + 17f, textPaint)
        canvas.drawText("Qty", 405f, startY + 17f, textPaint)
        canvas.drawText("Rate", 445f, startY + 17f, textPaint)
        canvas.drawText("Amount", 495f, startY + 17f, textPaint)

        textPaint.typeface = Typeface.DEFAULT
        var y = startY + 42f
        items.forEachIndexed { index, item ->
            canvas.drawText("${index + 1}", 50f, y, textPaint)
            canvas.drawText(truncateText(item.description, 40), 85f, y, textPaint)
            canvas.drawText(item.unit, 355f, y, textPaint)
            canvas.drawText(formatNumber(item.qty), 405f, y, textPaint)
            canvas.drawText(formatNumber(item.rate), 445f, y, textPaint)
            canvas.drawText(formatNumber(item.amount), 495f, y, textPaint)
            y += 20f
        }
    }

    private fun drawBranding(canvas: Canvas, context: Context, company: CompanyProfile) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Official combined seal + signature: always use seal_sign.png.
        loadDrawableBitmap(context, R.drawable.seal_sign)?.let { bitmap ->
            val scaled = Bitmap.createScaledBitmap(bitmap, 150, 150, true)
            canvas.drawBitmap(scaled, 380f, 640f, paint)
            if (scaled !== bitmap) scaled.recycle()
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
        }
        canvas.drawText("For ${company.name}", 380f, 795f, textPaint)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Authorized Signatory", 420f, 810f, textPaint)
    }

    private fun loadDrawableBitmap(context: Context, resourceId: Int): Bitmap? = try {
        BitmapFactory.decodeResource(context.resources, resourceId)
    } catch (_: Exception) {
        null
    }

    private fun formatCurrency(value: Double): String = "₹${String.format(Locale.US, "%.2f", value)}"
    private fun formatNumber(value: Double): String = String.format(Locale.US, "%.2f", value)
    private fun truncateText(text: String, maxLength: Int): String =
        if (text.length <= maxLength) text else text.take(maxLength - 3) + "..."
}
