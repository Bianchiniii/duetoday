package br.com.contaemdia.domain.usecase

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.RecurrenceType
import br.com.contaemdia.domain.repository.BillRepository
import java.time.LocalDateTime

class SaveBillUseCase(
    private val repository: BillRepository,
) {
    suspend operator fun invoke(bill: Bill): Result<Long> {
        val title = bill.title.trim()
        if (title.isBlank()) return Result.failure(IllegalArgumentException("Informe o nome da conta."))
        if (bill.amountCents <= 0) return Result.failure(IllegalArgumentException("Informe um valor válido."))

        val now = LocalDateTime.now()
        val normalized = bill.copy(
            title = title,
            notes = bill.notes?.trim()?.takeIf { it.isNotBlank() },
            recurrenceType = if (bill.isRecurring) RecurrenceType.MONTHLY else RecurrenceType.NONE,
            updatedAt = now,
            createdAt = if (bill.id == 0L) now else bill.createdAt,
        )
        return runCatching { repository.saveBill(normalized) }
    }
}
