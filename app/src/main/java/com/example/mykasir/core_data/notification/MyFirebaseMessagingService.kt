package com.example.mykasir.core_data.notification

import android.util.Log
import com.example.mykasir.core_ui.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service untuk menerima push notifications dari Firebase Cloud Messaging
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    /**
     * Dipanggil saat FCM token baru digenerate
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM Token baru: $token")
        // TODO: Kirim token ke server backend untuk push notifications
        // sendTokenToServer(token)
    }

    /**
     * Dipanggil saat menerima pesan dari FCM
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Pesan dari: ${remoteMessage.from}")

        // Cek jika ada data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Cek jika ada notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notification: ${notification.title} - ${notification.body}")
            showNotification(notification.title ?: "", notification.body ?: "")
        }
    }

    /**
     * Handle data message (untuk custom processing)
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return
        val title = data["title"] ?: "MyKasir"
        val message = data["message"] ?: ""

        when (type) {
            "transaction" -> {
                val total = data["total"] ?: "0"
                NotificationHelper.showTransactionNotification(this, "Rp$total")
            }
            "low_stock" -> {
                val productName = data["productName"] ?: ""
                val stock = data["stock"]?.toIntOrNull() ?: 0
                NotificationHelper.showLowStockNotification(this, productName, stock)
            }
            "general" -> {
                showNotification(title, message)
            }
        }
    }

    /**
     * Tampilkan notifikasi menggunakan NotificationHelper
     */
    private fun showNotification(title: String, message: String) {
        // Menggunakan welcome notification untuk pesan umum dari server
        NotificationHelper.showWelcomeNotification(this, message)
    }
}
