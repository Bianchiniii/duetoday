package br.com.contaemdia.domain.usecase

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.repository.BillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class FakeBillRepository(initialBills: List<Bill> = emptyList()) : BillRepository {
    private val bills = MutableStateFlow(initialBills)
    private var nextId = (initialBills.maxOfOrNull { it.id } ?: 0L) + 1

    override fun observeBillsByMonth(month: YearMonth): Flow<List<Bill>> =
        bills.map { list -> list.filter { it.belongsTo(month) } }

    override fun observeOpenBills(): Flow<List<Bill>> = bills

    override fun observePaidBills(): Flow<List<Bill>> = bills

    override fun observeBillsByCategory(category: BillCategory): Flow<List<Bill>> =
        bills.map { list -> list.filter { it.category == category } }

    override fun observeBillById(id: Long): Flow<Bill?> = bills.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getBillById(id: Long): Bill? = bills.value.firstOrNull { it.id == id }

    override suspend fun saveBill(bill: Bill): Long {
        val id = if (bill.id == 0L) nextId++ else bill.id
        bills.value = bills.value.filterNot { it.id == id } + bill.copy(id = id)
        return id
    }

    override suspend fun deleteBill(bill: Bill) {
        bills.value = bills.value.filterNot { it.id == bill.id }
    }

    override suspend fun hasRecurringBillInMonth(bill: Bill, month: YearMonth): Boolean =
        bills.value.any { it.title == bill.title && it.category == bill.category && it.belongsTo(month) && it.isRecurring }

    fun snapshot(): List<Bill> = bills.value
}
