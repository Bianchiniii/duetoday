package br.com.contaemdia.domain.usecase

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillStatus
import br.com.contaemdia.domain.model.RecurrenceType
import br.com.contaemdia.domain.repository.BillRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class MarkBillPaidUseCase(
    private val repository: BillRepository,
) {
    suspend operator fun invoke(bill: Bill, paidAt: LocalDate = LocalDate.now()): Result<Unit> = runCatching {
        val now = LocalDateTime.now()
        repository.saveBill(
            bill.copy(
                status = BillStatus.PAID,
                paidAt = paidAt,
                updatedAt = now,
            )
        )
        createNextRecurringBillIfNeeded(bill, now)
    }

    private suspend fun createNextRecurringBillIfNeeded(bill: Bill, now: LocalDateTime) {
        if (!bill.isRecurring || bill.recurrenceType != RecurrenceType.MONTHLY) return

        val nextDueDate = bill.dueDate.plusMonths(1)
        val nextMonth = YearMonth.from(nextDueDate)
        if (repository.hasRecurringBillInMonth(bill, nextMonth)) return

        repository.saveBill(
            bill.copy(
                id = 0,
                dueDate = nextDueDate,
                status = BillStatus.OPEN,
                paidAt = null,
                createdAt = now,
                updatedAt = now,
            )
        )
    }
}
