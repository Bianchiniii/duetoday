package br.com.contaemdia.domain.usecase

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.model.BillSortOption
import br.com.contaemdia.domain.model.BillStatus
import br.com.contaemdia.domain.model.BillStatusFilter
import java.time.LocalDate

class FilterSortBillsUseCase {
    operator fun invoke(
        bills: List<Bill>,
        statusFilter: BillStatusFilter,
        category: BillCategory?,
        sortOption: BillSortOption,
        today: LocalDate = LocalDate.now(),
    ): List<Bill> {
        val filtered = bills
            .filter { bill ->
                when (statusFilter) {
                    BillStatusFilter.ALL -> true
                    BillStatusFilter.OPEN -> bill.status == BillStatus.OPEN
                    BillStatusFilter.PAID -> bill.status == BillStatus.PAID
                    BillStatusFilter.OVERDUE -> bill.isOverdue(today)
                }
            }
            .filter { bill -> category == null || bill.category == category }

        return when (sortOption) {
            BillSortOption.DUE_DATE_ASC -> filtered.sortedWith(compareBy<Bill> { it.dueDate }.thenBy { it.title })
            BillSortOption.AMOUNT_DESC -> filtered.sortedByDescending { it.amountCents }
            BillSortOption.AMOUNT_ASC -> filtered.sortedBy { it.amountCents }
        }
    }
}
