package br.com.contaemdia.domain.usecase

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillStatus
import br.com.contaemdia.domain.repository.BillRepository
import java.time.LocalDateTime

class MarkBillOpenUseCase(
    private val repository: BillRepository,
) {
    suspend operator fun invoke(bill: Bill): Result<Unit> = runCatching {
        repository.saveBill(
            bill.copy(
                status = BillStatus.OPEN,
                paidAt = null,
                updatedAt = LocalDateTime.now(),
            )
        )
    }
}
