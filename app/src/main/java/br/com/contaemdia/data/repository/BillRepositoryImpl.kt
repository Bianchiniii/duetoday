package br.com.contaemdia.data.repository

import br.com.contaemdia.data.local.dao.BillDao
import br.com.contaemdia.data.mapper.toDomain
import br.com.contaemdia.data.mapper.toEntity
import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.repository.BillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class BillRepositoryImpl(
    private val billDao: BillDao,
) : BillRepository {
    override fun observeBillsByMonth(month: YearMonth): Flow<List<Bill>> =
        billDao.observeBillsByMonth(month.atDay(1), month.atEndOfMonth())
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeOpenBills(): Flow<List<Bill>> =
        billDao.observeOpenBills().map { entities -> entities.map { it.toDomain() } }

    override fun observePaidBills(): Flow<List<Bill>> =
        billDao.observePaidBills().map { entities -> entities.map { it.toDomain() } }

    override fun observeBillsByCategory(category: BillCategory): Flow<List<Bill>> =
        billDao.observeBillsByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override fun observeBillById(id: Long): Flow<Bill?> =
        billDao.observeBillById(id).map { it?.toDomain() }

    override suspend fun getBillById(id: Long): Bill? = billDao.getBillById(id)?.toDomain()

    override suspend fun saveBill(bill: Bill): Long =
        if (bill.id == 0L) {
            billDao.insertBill(bill.toEntity())
        } else {
            billDao.updateBill(bill.toEntity())
            bill.id
        }

    override suspend fun deleteBill(bill: Bill) {
        billDao.deleteBill(bill.toEntity())
    }

    override suspend fun hasRecurringBillInMonth(bill: Bill, month: YearMonth): Boolean =
        billDao.countRecurringInMonth(
            title = bill.title,
            category = bill.category,
            startDate = month.atDay(1),
            endDate = month.atEndOfMonth(),
        ) > 0
}
