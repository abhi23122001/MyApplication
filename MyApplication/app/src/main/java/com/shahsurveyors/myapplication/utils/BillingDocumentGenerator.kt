package com.shahsurveyors.myapplication.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.shahsurveyors.myapplication.R
import com.shahsurveyors.myapplication.models.BillingDocument
import com.shahsurveyors.myapplication.models.DocType
import java.io.File
import java.io.FileOutputStream
import java.util.*

object BillingDocumentGenerator {

    fun generatePdf(context: Context, document: BillingDocument): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var currentY = 40f

        // 1. Header
        drawHeader(canvas, paint, document.docType)
        currentY = 150f

        // 2. Company Details
        drawCompanyDetails(canvas, paint)
        
        // 3. Client Details
        drawClientDetails(canvas, paint, document)
        currentY = 300f

        // 4. Table Header
        drawTableHeader(canvas, paint, currentY)
        currentY += 25f

        // 5. Items
        var subTotal = 0.0
        document.items.forEachIndexed { index, item ->
            drawItemRow(canvas, paint, index + 1, item, currentY)
            subTotal += item.taxableAmount
            currentY += 20f
        }

        currentY += 10f

        // 6. Tax Calculations
        val taxAmount = if (document.docType == DocType.TAX_INVOICE) {
            drawTaxCalculations(canvas, paint, subTotal, document.isInterState, currentY)
        } else {
            0.0
        }
        
        val grandTotal = subTotal + taxAmount

        // 7. Total in Words
        drawTotalInWords(canvas, paint, grandTotal, 650f)

        // 8. Bank Details & Terms
        drawBankDetails(canvas, paint, 700f)
        
        // 9. Stamp
        drawStamp(context, canvas, paint, 450f, 700f)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "${document.docType}_${System.currentTimeMillis()}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }

    private fun drawHeader(canvas: Canvas, paint: Paint, docType: DocType) {
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText(docType.name.replace("_", " "), 297f, 40f, paint)
    }

    private fun drawCompanyDetails(canvas: Canvas, paint: Paint) {
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("SHAH SURVEYORS AND CONSULTANCY", 40f, 80f, paint)
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("(ISO 9001:2015 CERTIFIED)", 40f, 95f, paint)
        canvas.drawText("GSTIN: 23JZEPS5792H1ZQ | PAN: JZEPS5792H | State Code: 23", 40f, 110f, paint)
        canvas.drawText("Sai College Road Gahilgarh (East) Waidhan, Singrauli, M.P. 486886", 40f, 125f, paint)
        canvas.drawText("shahsurveyors2022@gmail.com | Mob: +91 7974831659", 40f, 140f, paint)
    }

    private fun drawClientDetails(canvas: Canvas, paint: Paint, doc: BillingDocument) {
        paint.isFakeBoldText = true
        canvas.drawText("BILL TO:", 40f, 180f, paint)
        paint.isFakeBoldText = false
        canvas.drawText(doc.clientName, 40f, 195f, paint)
        canvas.drawText(doc.clientAddress, 40f, 210f, paint)
        doc.clientGstin?.let { canvas.drawText("GSTIN: $it", 40f, 225f, paint) }
    }

    private fun drawTableHeader(canvas: Canvas, paint: Paint, y: Float) {
        paint.isFakeBoldText = true
        canvas.drawText("S.No", 40f, y, paint)
        canvas.drawText("Work Description", 80f, y, paint)
        canvas.drawText("Qty", 350f, y, paint)
        canvas.drawText("Unit", 400f, y, paint)
        canvas.drawText("Rate", 450f, y, paint)
        canvas.drawText("Amount", 520f, y, paint)
        canvas.drawLine(40f, y + 5, 555f, y + 5, paint)
    }

    private fun drawItemRow(canvas: Canvas, paint: Paint, index: Int, item: com.shahsurveyors.myapplication.models.BillingItem, y: Float) {
        paint.isFakeBoldText = false
        canvas.drawText(index.toString(), 40f, y, paint)
        canvas.drawText(item.description, 80f, y, paint)
        canvas.drawText(item.qty.toString(), 350f, y, paint)
        canvas.drawText(item.unit, 400f, y, paint)
        canvas.drawText(String.format("%.2f", item.rate), 450f, y, paint)
        canvas.drawText(String.format("%.2f", item.taxableAmount), 520f, y, paint)
    }

    private fun drawTaxCalculations(canvas: Canvas, paint: Paint, subTotal: Double, isInterState: Boolean, y: Float): Double {
        var currentY = y
        paint.textAlign = Paint.Align.RIGHT
        if (isInterState) {
            val igst = subTotal * 0.18
            canvas.drawText("IGST (18%): ${String.format("%.2f", igst)}", 555f, currentY, paint)
            return igst
        } else {
            val cgst = subTotal * 0.09
            val sgst = subTotal * 0.09
            canvas.drawText("CGST (9%): ${String.format("%.2f", cgst)}", 555f, currentY, paint)
            canvas.drawText("SGST (9%): ${String.format("%.2f", sgst)}", 555f, currentY + 15, paint)
            return cgst + sgst
        }
    }

    private fun drawTotalInWords(canvas: Canvas, paint: Paint, total: Double, y: Float) {
        paint.textAlign = Paint.Align.RIGHT
        paint.isFakeBoldText = true
        canvas.drawText("Grand Total: ${String.format("%.2f", total)}", 555f, y, paint)
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = false
        canvas.drawText("Amount in words: ${convertAmountToWords(total)}", 40f, y + 20, paint)
    }

    private fun drawBankDetails(canvas: Canvas, paint: Paint, y: Float) {
        paint.isFakeBoldText = true
        canvas.drawText("Bank Details:", 40f, y, paint)
        paint.isFakeBoldText = false
        canvas.drawText("Bank: Union Bank of India", 40f, y + 15, paint)
        canvas.drawText("A/C: 436701010250644", 40f, y + 30, paint)
        canvas.drawText("IFSC: UBIN0543675", 40f, y + 45, paint)
        canvas.drawText("Branch: Vindhyanagar Singrauli", 40f, y + 60, paint)
    }

    private fun drawStamp(context: Context, canvas: Canvas, paint: Paint, x: Float, y: Float) {
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.seal_sign)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, true)
            canvas.drawBitmap(scaledBitmap, x, y, paint)
            canvas.drawText("Authorized Signatory", x, y + 95, paint)
        } catch (e: Exception) {
            canvas.drawText("[Stamp Placeholder]", x, y + 40, paint)
        }
    }

    private fun convertAmountToWords(amount: Double): String {
        return "RUPEES ${amount.toLong()} ONLY"
    }
}
