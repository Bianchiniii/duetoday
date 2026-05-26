package br.com.contaemdia.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.contaemdia.R
import br.com.contaemdia.core.date.toBrazilianDate
import br.com.contaemdia.core.money.toCurrencyText
import java.time.LocalDate

class BillReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        if (!canNotify()) return Result.success()
        createChannel()

        val title = inputData.getString(KEY_TITLE).orEmpty()
        val amount = inputData.getLong(KEY_AMOUNT_CENTS, 0L).toCurrencyText()
        val dueDate = inputData.getString(KEY_DUE_DATE)
            ?.let(LocalDate::parse)
            ?.toBrazilianDate()
            .orEmpty()
        val daysBefore = inputData.getInt(KEY_DAYS_BEFORE, 0)
        val reminderTitle =
            if (daysBefore == 0) "Conta vence hoje" else "Conta vence em $daysBefore dias"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(reminderTitle)
            .setContentText("$title - $amount - $dueDate")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify((title + dueDate + daysBefore).hashCode(), notification)
        return Result.success()
    }

    private fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Vencimentos",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        applicationContext.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_AMOUNT_CENTS = "amount_cents"
        const val KEY_DUE_DATE = "due_date"
        const val KEY_DAYS_BEFORE = "days_before"
        private const val CHANNEL_ID = "bill_due_dates"
    }
}
