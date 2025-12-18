package com.example.mykasir.core_ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mykasir.MainActivity
import com.example.mykasir.R

/**
 * Helper class untuk menampilkan push notification
 */
object NotificationHelper {
    
    private const val CHANNEL_ID_TRANSACTION = "transaction_channel"
    private const val CHANNEL_ID_STOCK = "stock_channel"
    private const val CHANNEL_ID_PDF = "pdf_channel"
    
    private var notificationId = 0
    
    /**
     * Inisialisasi notification channels (panggil di Application atau MainActivity)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Channel untuk transaksi
            val transactionChannel = NotificationChannel(
                CHANNEL_ID_TRANSACTION,
                "Transaksi",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi transaksi berhasil"
            }
            
            // Channel untuk stok
            val stockChannel = NotificationChannel(
                CHANNEL_ID_STOCK,
                "Peringatan Stok",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi stok menipis"
            }
            
            // Channel untuk PDF
            val pdfChannel = NotificationChannel(
                CHANNEL_ID_PDF,
                "Struk PDF",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi pembuatan PDF"
            }
            
            notificationManager.createNotificationChannels(
                listOf(transactionChannel, stockChannel, pdfChannel)
            )
        }
    }
    
    /**
     * Notifikasi transaksi berhasil
     */
    fun showTransactionNotification(context: Context, total: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRANSACTION)
            .setSmallIcon(R.drawable.mykasir_logo)
            .setContentTitle("Transaksi Berhasil")
            .setContentText("✅ Transaksi $total berhasil disimpan")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId++, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    /**
     * Notifikasi stok menipis
     */
    fun showLowStockNotification(context: Context, productName: String, stock: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_STOCK)
            .setSmallIcon(R.drawable.mykasir_logo)
            .setContentTitle("Stok Menipis!")
            .setContentText("⚠️ Stok $productName menipis! Tersisa $stock unit")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId++, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    /**
     * Notifikasi PDF berhasil dibuat
     */
    fun showPdfNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PDF)
            .setSmallIcon(R.drawable.mykasir_logo)
            .setContentTitle("Struk PDF Dibuat")
            .setContentText("📄 Struk PDF berhasil dibuat")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId++, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
