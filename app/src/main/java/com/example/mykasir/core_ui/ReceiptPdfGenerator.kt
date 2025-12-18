package com.example.mykasir.core_ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.mykasir.feature_transaksi.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility untuk generate struk transaksi dalam format PDF
 */
object ReceiptPdfGenerator {
    
    private const val PAGE_WIDTH = 300
    private const val PAGE_HEIGHT = 600
    private const val MARGIN = 20f
    private const val LINE_HEIGHT = 18f
    
    /**
     * Generate PDF struk dan return file path
     */
    fun generateReceipt(
        context: Context,
        transaction: Transaction,
        customerName: String
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        var yPos = MARGIN
        
        // Paints
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        val subtitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        
        val normalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        
        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        
        val linePaint = Paint().apply {
            color = Color.GRAY
            strokeWidth = 1f
        }
        
        val centerX = PAGE_WIDTH / 2f
        
        // Header
        canvas.drawText("MYKASIR", centerX, yPos + 16f, titlePaint)
        yPos += 24f
        canvas.drawText("Struk Pembayaran", centerX, yPos + 12f, subtitlePaint)
        yPos += 24f
        
        // Separator line
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, linePaint)
        yPos += 12f
        
        // Customer & Transaction Info
        canvas.drawText("Pelanggan: $customerName", MARGIN, yPos + 10f, normalPaint)
        yPos += LINE_HEIGHT
        
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date(transaction.timestamp))
        canvas.drawText("Tanggal: $dateStr", MARGIN, yPos + 10f, normalPaint)
        yPos += LINE_HEIGHT
        
        val txNum = "#" + transaction.id.toString().takeLast(6)
        canvas.drawText("No. Transaksi: $txNum", MARGIN, yPos + 10f, normalPaint)
        yPos += LINE_HEIGHT + 8f
        
        // Separator line
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, linePaint)
        yPos += 12f
        
        // Table header
        canvas.drawText("Produk", MARGIN, yPos + 10f, boldPaint)
        canvas.drawText("Qty", PAGE_WIDTH - 100f, yPos + 10f, boldPaint)
        canvas.drawText("Subtotal", PAGE_WIDTH - 60f, yPos + 10f, boldPaint)
        yPos += LINE_HEIGHT
        
        // Dashed line
        drawDashedLine(canvas, MARGIN, yPos, PAGE_WIDTH - MARGIN, linePaint)
        yPos += 8f
        
        // Items
        for (item in transaction.items) {
            val subtotal = item.unitPrice * item.quantity
            
            // Potong nama produk jika terlalu panjang
            val productName = if (item.productName.length > 18) {
                item.productName.take(16) + ".."
            } else {
                item.productName
            }
            
            canvas.drawText(productName, MARGIN, yPos + 10f, normalPaint)
            canvas.drawText("x${item.quantity}", PAGE_WIDTH - 100f, yPos + 10f, normalPaint)
            canvas.drawText(formatRupiahSimple(subtotal), PAGE_WIDTH - 60f, yPos + 10f, normalPaint)
            yPos += LINE_HEIGHT
        }
        
        yPos += 8f
        
        // Separator line
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, linePaint)
        yPos += 16f
        
        // Total
        val totalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("TOTAL:", PAGE_WIDTH - 120f, yPos + 12f, totalPaint)
        canvas.drawText(formatRupiahSimple(transaction.total), PAGE_WIDTH - 60f, yPos + 12f, totalPaint)
        yPos += 28f
        
        // Separator line
        canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, linePaint)
        yPos += 16f
        
        // Footer
        canvas.drawText("Terima kasih atas", centerX, yPos + 10f, subtitlePaint)
        yPos += LINE_HEIGHT
        canvas.drawText("kunjungan Anda!", centerX, yPos + 10f, subtitlePaint)
        
        pdfDocument.finishPage(page)
        
        // Save to file
        val fileName = "struk_${transaction.id}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        
        return try {
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
    
    /**
     * Share PDF via Intent
     */
    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Struk"))
    }
    
    /**
     * Simpan PDF ke folder Downloads
     * Mengembalikan true jika berhasil
     */
    fun saveToDownloads(context: Context, sourceFile: File): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ menggunakan MediaStore
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.name)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                
                val resolver = context.contentResolver
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        sourceFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    true
                } ?: false
            } else {
                // Android 9 dan ke bawah - perlu WRITE_EXTERNAL_STORAGE permission
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, sourceFile.name)
                sourceFile.copyTo(destFile, overwrite = true)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun drawDashedLine(canvas: Canvas, startX: Float, y: Float, endX: Float, paint: Paint) {
        var x = startX
        while (x < endX) {
            canvas.drawLine(x, y, minOf(x + 5f, endX), y, paint)
            x += 10f
        }
    }
    
    private fun formatRupiahSimple(amount: Int): String {
        val formatter = java.text.NumberFormat.getNumberInstance(Locale("in", "ID"))
        return "Rp${formatter.format(amount)}"
    }
}
