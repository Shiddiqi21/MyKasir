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
    
    private const val CHANNEL_ID_GENERAL = "general_channel"
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
            
            // Channel untuk notifikasi umum - HIGH untuk melayang di layar
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "Notifikasi Umum",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi umum aplikasi"
                enableVibration(true)
            }
            
            // Channel untuk transaksi - HIGH untuk melayang di layar
            val transactionChannel = NotificationChannel(
                CHANNEL_ID_TRANSACTION,
                "Transaksi",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi transaksi berhasil"
                enableVibration(true)
            }
            
            // Channel untuk stok - HIGH untuk melayang di layar
            val stockChannel = NotificationChannel(
                CHANNEL_ID_STOCK,
                "Peringatan Stok",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi stok menipis"
                enableVibration(true)
            }
            
            // Channel untuk PDF - HIGH untuk melayang di layar
            val pdfChannel = NotificationChannel(
                CHANNEL_ID_PDF,
                "Dokumen PDF",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pembuatan PDF"
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannels(
                listOf(generalChannel, transactionChannel, stockChannel, pdfChannel)
            )
        }
    }
    
    /**
     * Notifikasi selamat datang setelah login
     */
    fun showWelcomeNotification(context: Context, userName: String) {
        showNotification(
            context = context,
            channelId = CHANNEL_ID_GENERAL,
            title = "Selamat Datang! 👋",
            message = "Halo $userName, selamat bekerja di MyKasir!"
        )
    }
    
    /**
     * Notifikasi transaksi berhasil
     */
    fun showTransactionNotification(context: Context, total: String) {
        showNotification(
            context = context,
            channelId = CHANNEL_ID_TRANSACTION,
            title = "Transaksi Berhasil ✅",
            message = "Transaksi $total berhasil disimpan"
        )
    }
    
    /**
     * Notifikasi stok menipis
     */
    fun showLowStockNotification(context: Context, productName: String, stock: Int) {
        showNotification(
            context = context,
            channelId = CHANNEL_ID_STOCK,
            title = "Stok Menipis! ⚠️",
            message = "Stok $productName tinggal $stock unit. Segera restok!"
        )
    }
    
    /**
     * Notifikasi kasir baru ditambahkan
     */
    fun showNewCashierNotification(context: Context, cashierName: String) {
        showNotification(
            context = context,
            channelId = CHANNEL_ID_GENERAL,
            title = "Kasir Baru Ditambahkan 👤",
            message = "$cashierName berhasil ditambahkan sebagai kasir"
        )
    }
    
    /**
     * Notifikasi profil diperbarui
     */
    fun showProfileUpdatedNotification(context: Context) {
        showNotification(
            context = context,
            channelId = CHANNEL_ID_GENERAL,
            title = "Profil Diperbarui ✏️",
            message = "Data profil Anda berhasil diperbarui"
        )
    }
    
    /**
     * Notifikasi produk baru ditambahkan
     */
    fun showProductAddedNotification(context: Context, productName: String) {
        showNotification(
            context = context,
            channelId = CHANNEL_ID_GENERAL,
            title = "Produk Baru Ditambahkan 📦",
            message = "$productName berhasil ditambahkan ke daftar produk"
        )
    }
    
    /**
     * Notifikasi Struk PDF berhasil dibuat
     */
    fun showPdfNotification(context: Context) {
        showNotification(
            context = context,
            channelId = CHANNEL_ID_PDF,
            title = "Struk PDF Dibuat 📄",
            message = "Struk transaksi PDF berhasil dibuat dan disimpan"
        )
    }
    
    /**
     * Notifikasi Laporan PDF berhasil dibuat
     */
    fun showReportNotification(context: Context) {
        showNotification(
            context = context,
            channelId = CHANNEL_ID_PDF,
            title = "Laporan PDF Dibuat 📊",
            message = "Laporan penjualan PDF berhasil dibuat dan disimpan"
        )
    }
    
    /**
     * Fungsi utama untuk menampilkan notifikasi
     */
    private fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.mykasir_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
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
}
