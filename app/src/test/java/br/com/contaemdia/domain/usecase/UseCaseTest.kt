package br.com.contaemdia.domain.usecase

import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.model.BillStatus
import br.com.contaemdia.domain.model.RecurrenceType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class UseCaseTest {
    @Test
    fun saveBillRejectsBlankTitle() = runTest {
        val useCase = SaveBillUseCase(FakeBillRepository())

        val result = useCase(
            Bill(
                title = " ",
                amountCents = 1000,
                dueDate = LocalDate.of(2026, 5, 25),
                category = BillCategory.OTHER,
            )
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun markPaidCreatesNextMonthlyBillWithoutDuplicatingSameMonth() = runTest {
        val bill = Bill(
            id = 1,
            title = "Internet",
            amountCents = 12000,
            dueDate = LocalDate.of(2026, 5, 10),
            category = BillCategory.INTERNET,
            isRecurring = true,
            recurrenceType = RecurrenceType.MONTHLY,
        )
        val repository = FakeBillRepository(listOf(bill))
        val useCase = MarkBillPaidUseCase(repository)

        useCase(bill, paidAt = LocalDate.of(2026, 5, 10))
        useCase(bill, paidAt = LocalDate.of(2026, 5, 10))

        val snapshot = repository.snapshot()
        assertEquals(2, snapshot.size)
        assertEquals(BillStatus.PAID, snapshot.first { it.id == 1L }.status)
        assertTrue(snapshot.any { it.dueDate == LocalDate.of(2026, 6, 10) && it.status == BillStatus.OPEN })
    }

    @Test
    fun monthlySummaryCalculatesPaidOpenAndOverdue() {
        val summary = BuildMonthlySummaryUseCase()(
            bills = listOf(
                Bill(1, "Pago", 1000, LocalDate.of(2026, 5, 1), BillCategory.OTHER, BillStatus.PAID),
                Bill(2, "Atrasado", 2000, LocalDate.of(2026, 5, 1), BillCategory.RENT),
                Bill(3, "Aberto", 3000, LocalDate.of(2026, 5, 30), BillCategory.RENT),
            ),
            today = LocalDate.of(2026, 5, 25),
        )

        assertEquals(6000, summary.totalCents)
        assertEquals(1000, summary.paidCents)
        assertEquals(5000, summary.openCents)
        assertEquals(2000, summary.overdueCents)
    }
}
