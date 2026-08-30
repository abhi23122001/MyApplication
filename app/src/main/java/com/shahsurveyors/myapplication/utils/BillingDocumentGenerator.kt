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
    data class DocumentData(val company: CompanyProfile,val bank: BankDetails,val clientName:String,val clientAddress:String,val clientGstin:String?,val docType:DocType,val docNumber:String,val date:Long=System.currentTimeMillis(),val gstType:String,val gstPercentage:Double=18.0,val items:List<BillingItemEntity>,val terms:List<String>)
    fun generatePdf(context:Context,data:DocumentData):File{
        val pdf=PdfDocument();val page=pdf.startPage(PdfDocument.PageInfo.Builder(595,842,1).create());val c=page.canvas
        val p=Paint(Paint.ANTI_ALIAS_FLAG);val tp=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.BLACK;textSize=10f;typeface=Typeface.DEFAULT}
        drawHeader(c,context,data.company);p.color=Color.BLACK;p.textSize=14f;p.typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);p.textAlign=Paint.Align.CENTER
        val title=when(data.docType){DocType.TAX_INVOICE->"TAX INVOICE";DocType.NON_GST_BILL->"BILL";DocType.QUOTATION->"QUOTATION"};c.drawText(title,297f,160f,p);p.textAlign=Paint.Align.LEFT
        val sdf=SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());p.textSize=10f;p.typeface=Typeface.DEFAULT;c.drawText("Date: ${sdf.format(Date(data.date))}",430f,185f,p);c.drawText("No: ${data.docNumber}",430f,200f,p)
        p.typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);c.drawText("TO:",40f,185f,p);p.typeface=Typeface.DEFAULT;c.drawText(data.clientName,40f,200f,p);c.drawText(data.clientAddress,40f,215f,p);data.clientGstin?.takeIf{it.isNotBlank()}?.let{c.drawText("GSTIN: $it",40f,230f,p)}
        if(data.docType==DocType.QUOTATION){c.drawText("Dear Sir,",40f,260f,p);c.drawText("We thank you for the enquiry and are pleased to offer our prices for your kind consideration.",40f,275f,p)}
        val startY=300f;drawTable(c,data.items,startY);val sub=data.items.sumOf{it.amount};var total=sub;var y=startY+25f+data.items.size*20f+40f
        if(data.docType!=DocType.NON_GST_BILL){val rate=data.gstPercentage/100.0;if(data.gstType=="IGST"){val gst=sub*rate;c.drawText("IGST (${data.gstPercentage}%):",380f,y,tp);c.drawText(formatCurrency(gst),480f,y,tp);total+=gst;y+=15f}else{val cgst=sub*(rate/2);val sgst=sub*(rate/2);c.drawText("CGST (${data.gstPercentage/2}%):",380f,y,tp);c.drawText(formatCurrency(cgst),480f,y,tp);y+=15f;c.drawText("SGST (${data.gstPercentage/2}%):",380f,y,tp);c.drawText(formatCurrency(sgst),480f,y,tp);total+=cgst+sgst;y+=15f}}
        p.typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);c.drawText("GRAND TOTAL:",380f,y+5f,p);c.drawText(formatCurrency(total),480f,y+5f,p);p.typeface=Typeface.create(Typeface.DEFAULT,Typeface.ITALIC);c.drawText("Amount in words: ${NumberToWords.convert(total)}",40f,y+25f,p)
        y+=60f;p.typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);c.drawText("TERMS & CONDITIONS:",40f,y,p);p.typeface=Typeface.DEFAULT;var ty=y+15f;data.terms.forEachIndexed{i,t->c.drawText("${i+1}. $t",50f,ty,p);ty+=15f}
        val by=700f;p.typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);c.drawText("BANK DETAILS:",40f,by,p);p.typeface=Typeface.DEFAULT;c.drawText("Bank: ${data.bank.bankName}",40f,by+15f,p);c.drawText("A/C No: ${data.bank.accountNumber}",40f,by+30f,p);c.drawText("IFSC: ${data.bank.ifscCode} | Branch: ${data.bank.branchAddress}",40f,by+45f,p)
        drawBranding(c,context,data.company);p.textAlign=Paint.Align.CENTER;p.textSize=8f;p.typeface=Typeface.DEFAULT;c.drawText(data.company.footerText,297f,830f,p);p.textAlign=Paint.Align.LEFT
        pdf.finishPage(page);val file=File(context.cacheDir,"SSC_${title.replace(" ","_")}_${System.currentTimeMillis()}.pdf");FileOutputStream(file).use{pdf.writeTo(it)};pdf.close();return file
    }
    private fun drawHeader(c:Canvas,context:Context,company:CompanyProfile){val p=Paint(Paint.ANTI_ALIAS_FLAG);loadDrawableBitmap(context,R.drawable.app_logo)?.let{b->val s=Bitmap.createScaledBitmap(b,80,80,true);c.drawBitmap(s,40f,40f,p);if(s!==b)s.recycle()};p.textAlign=Paint.Align.CENTER;p.color=Color.BLACK;p.textSize=20f;p.typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);c.drawText(company.name,297f,60f,p);p.textSize=9f;p.typeface=Typeface.DEFAULT;c.drawText("ISO 9001:2015 CERTIFIED",297f,75f,p);c.drawText(company.address,297f,90f,p);c.drawText("Email: ${company.email} | Mobile: ${company.phone}",297f,105f,p);c.drawLine(40f,120f,555f,120f,p);p.textAlign=Paint.Align.LEFT}
    private fun drawTable(c:Canvas,items:List<BillingItemEntity>,startY:Float){val b=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeWidth=1f;color=Color.BLACK};val p=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.BLACK;textSize=9f};val h=25f+items.size*20f;c.drawRect(40f,startY,555f,startY+h,b);c.drawLine(40f,startY+25f,555f,startY+25f,b);listOf(80f,350f,400f,440f,490f).forEach{x->c.drawLine(x,startY,x,startY+h,b)};p.typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);c.drawText("S.No",45f,startY+17f,p);c.drawText("Description",85f,startY+17f,p);c.drawText("Unit",355f,startY+17f,p);c.drawText("Qty",405f,startY+17f,p);c.drawText("Rate",445f,startY+17f,p);c.drawText("Amount",495f,startY+17f,p);p.typeface=Typeface.DEFAULT;var y=startY+42f;items.forEachIndexed{i,item->c.drawText("${i+1}",50f,y,p);c.drawText(truncateText(item.description,40),85f,y,p);c.drawText(item.unit,355f,y,p);c.drawText(formatNumber(item.qty),405f,y,p);c.drawText(formatNumber(item.rate),445f,y,p);c.drawText(formatNumber(item.amount),495f,y,p);y+=20f}}
    private fun drawBranding(c:Canvas,context:Context,company:CompanyProfile){val p=Paint(Paint.ANTI_ALIAS_FLAG);loadDrawableBitmap(context,R.drawable.seal_sign)?.let{b->val s=Bitmap.createScaledBitmap(b,150,150,true);c.drawBitmap(s,380f,640f,p);if(s!==b)s.recycle()};val t=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.BLACK;textSize=10f};c.drawText("For ${company.name}",380f,795f,t);t.typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD);c.drawText("Authorized Signatory",420f,810f,t)}
    private fun loadDrawableBitmap(context:Context,id:Int):Bitmap?=try{BitmapFactory.decodeResource(context.resources,id)}catch(_:Exception){null}
    private fun formatCurrency(v:Double)="₹${String.format(Locale.US,"%.2f",v)}";private fun formatNumber(v:Double)=String.format(Locale.US,"%.2f",v);private fun truncateText(t:String,n:Int)=if(t.length<=n)t else t.take(n-3)+"..."
}
