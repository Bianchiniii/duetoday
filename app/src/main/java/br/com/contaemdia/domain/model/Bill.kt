package br.com.contaemdia.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

data class Bill(
    val id: Long = 0,
    val title: String,
    val amountCents: Long,
    val dueDate: LocalDate,
    val category: BillCategory,
    val status: BillStatus = BillStatus.OPEN,
    val isRecurring: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val notes: String? = null,
    val paidAt: LocalDate? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun isOverdue(today: LocalDate = LocalDate.now()): Boolean =
        status == BillStatus.OPEN && dueDate.isBefore(today)

    fun isDueToday(today: LocalDate = LocalDate.now()): Boolean =
        status == BillStatus.OPEN && dueDate == today

    fun isDueInNextDays(days: Long, today: LocalDate = LocalDate.now()): Boolean =
        status == BillStatus.OPEN && dueDate.isAfter(today) && !dueDate.isAfter(today.plusDays(days))

    fun belongsTo(month: YearMonth): Boolean = YearMonth.from(dueDate) == month
}
