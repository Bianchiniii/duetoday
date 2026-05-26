package br.com.contaemdia.domain.usecase

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.repository.BillRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

class ObserveBillsByMonthUseCase(
    private val repository: BillRepository,
) {
    operator fun invoke(month: YearMonth): Flow<List<Bill>> = repository.observeBillsByMonth(month)
}
