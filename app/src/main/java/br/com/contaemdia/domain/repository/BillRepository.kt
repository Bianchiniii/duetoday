package br.com.contaemdia.domain.repository

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillCategory
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BillRepository {
    fun observeBillsByMonth(month: YearMonth): Flow<List<Bill>>
    fun observeOpenBills(): Flow<List<Bill>>
    fun observePaidBills(): Flow<List<Bill>>
    fun observeBillsByCategory(category: BillCategory): Flow<List<Bill>>
    fun observeBillById(id: Long): Flow<Bill?>
    suspend fun getBillById(id: Long): Bill?
    suspend fun saveBill(bill: Bill): Long
    suspend fun deleteBill(bill: Bill)
    suspend fun hasRecurringBillInMonth(bill: Bill, month: YearMonth): Boolean
}
