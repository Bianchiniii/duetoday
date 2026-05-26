package br.com.contaemdia.presentation.components

import br.com.contaemdia.core.date.toBrazilianDate
import br.com.contaemdia.core.money.toCurrencyText
import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillStatus
import java.time.LocalDate

enum class BillUrgency {
    OVERDUE,
    TODAY,
    NEXT_SEVEN_DAYS,
    FUTURE,
    PAID,
}

data class BillUiModel(
    val id: Long,
    val title: String,
    val amount: String,
    val dueDate: String,
    val category: String,
    val status: String,
    val urgency: BillUrgency,
    val canMarkPaid: Boolean,
)

fun Bill.toUiModel(today: LocalDate = LocalDate.now()): BillUiModel {
    val urgency = when {
        status == BillStatus.PAID -> BillUrgency.PAID
        isOverdue(today) -> BillUrgency.OVERDUE
        isDueToday(today) -> BillUrgency.TODAY
        isDueInNextDays(7, today) -> BillUrgency.NEXT_SEVEN_DAYS
        else -> BillUrgency.FUTURE
    }

    return BillUiModel(
        id = id,
        title = title,
        amount = amountCents.toCurrencyText(),
        dueDate = dueDate.toBrazilianDate(),
        category = category.label,
        status = if (status == BillStatus.PAID) "Pago" else "Em aberto",
        urgency = urgency,
        canMarkPaid = status == BillStatus.OPEN,
    )
}
