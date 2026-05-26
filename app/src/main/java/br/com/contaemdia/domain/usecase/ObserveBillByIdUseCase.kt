package br.com.contaemdia.domain.usecase

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.repository.BillRepository
import kotlinx.coroutines.flow.Flow

class ObserveBillByIdUseCase(
    private val repository: BillRepository,
) {
    operator fun invoke(id: Long): Flow<Bill?> = repository.observeBillById(id)
}
