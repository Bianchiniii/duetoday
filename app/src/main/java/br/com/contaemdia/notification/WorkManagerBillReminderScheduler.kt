package br.com.contaemdia.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillStatus
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class WorkManagerBillReminderScheduler(
    private val context: Context,
) : BillReminderScheduler {
    override fun schedule(bill: Bill) {
        cancel(bill.id)
        if (bill.id == 0L || bill.status == BillStatus.PAID) return

        enqueueReminder(bill, daysBefore = 3)
        enqueueReminder(bill, daysBefore = 0)
    }

    override fun cancel(billId: Long) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(workName(billId, 3))
        workManager.cancelUniqueWork(workName(billId, 0))
    }

    private fun enqueueReminder(bill: Bill, daysBefore: Long) {
        val reminderDateTime = bill.dueDate.minusDays(daysBefore).atTime(LocalTime.of(9, 0))
        val delayMillis = Duration.between(LocalDateTime.now(), reminderDateTime).toMillis()
        if (delayMillis <= 0) return

        val request = OneTimeWorkRequestBuilder<BillReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    BillReminderWorker.KEY_TITLE to bill.title,
                    BillReminderWorker.KEY_AMOUNT_CENTS to bill.amountCents,
                    BillReminderWorker.KEY_DUE_DATE to bill.dueDate.toString(),
                    BillReminderWorker.KEY_DAYS_BEFORE to daysBefore.toInt(),
                )
            )
            .addTag("bill-${bill.id}")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(bill.id, daysBefore),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun workName(billId: Long, daysBefore: Long): String =
        "bill-$billId-reminder-$daysBefore"
}
