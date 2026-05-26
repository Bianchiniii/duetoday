package br.com.contaemdia.domain.usecase

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.repository.BillRepository

class DeleteBillUseCase(
    private val repository: BillRepository,
) {
    suspend operator fun invoke(bill: Bill): Result<Unit> = runCatching {
        repository.deleteBill(bill)
    }
}
