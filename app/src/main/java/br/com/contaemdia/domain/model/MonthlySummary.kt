package br.com.contaemdia.domain.model

data class CategorySummary(
    val category: BillCategory,
    val totalCents: Long,
    val count: Int,
)

data class MonthlySummary(
    val paidCents: Long,
    val openCents: Long,
    val overdueCents: Long,
    val totalCents: Long,
    val dueNextSevenDaysCents: Long,
    val byCategory: List<CategorySummary>,
    val biggestBills: List<Bill>,
)
