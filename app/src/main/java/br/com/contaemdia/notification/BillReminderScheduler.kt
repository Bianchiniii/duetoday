package br.com.contaemdia.notification

import br.com.contaemdia.domain.model.Bill

interface BillReminderScheduler {
    fun schedule(bill: Bill)
    fun cancel(billId: Long)
}
