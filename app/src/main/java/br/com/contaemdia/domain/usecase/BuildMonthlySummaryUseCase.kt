package br.com.contaemdia.domain.usecase

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillStatus
import br.com.contaemdia.domain.model.CategorySummary
import br.com.contaemdia.domain.model.MonthlySummary
import java.time.LocalDate

class BuildMonthlySummaryUseCase {
    operator fun invoke(bills: List<Bill>, today: LocalDate = LocalDate.now()): MonthlySummary {
        val paid = bills.filter { it.status == BillStatus.PAID }.sumOf { it.amountCents }
        val overdue = bills.filter { it.isOverdue(today) }.sumOf { it.amountCents }
        val open = bills.filter { it.status == BillStatus.OPEN }.sumOf { it.amountCents }
        val nextSevenDays = bills.filter { it.isDueInNextDays(7, today) || it.isDueToday(today) }
            .sumOf { it.amountCents }

        return MonthlySummary(
            paidCents = paid,
            openCents = open,
            overdueCents = overdue,
            totalCents = bills.sumOf { it.amountCents },
            dueNextSevenDaysCents = nextSevenDays,
            byCategory = bills
                .groupBy { it.category }
                .map { (category, categoryBills) ->
                    CategorySummary(
                        category = category,
                        totalCents = categoryBills.sumOf { it.amountCents },
                        count = categoryBills.size,
                    )
                }
                .sortedByDescending { it.totalCents },
            biggestBills = bills.sortedByDescending { it.amountCents }.take(5),
        )
    }
}
