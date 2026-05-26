package br.com.contaemdia.presentation.dashboard

import br.com.contaemdia.core.common.ResultMessage
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.model.BillSortOption
import br.com.contaemdia.domain.model.BillStatusFilter
import br.com.contaemdia.domain.model.MonthlySummary
import br.com.contaemdia.presentation.components.BillUiModel
import java.time.YearMonth

data class BillSection(
    val title: String,
    val bills: List<BillUiModel>,
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val month: YearMonth = YearMonth.now(),
    val summary: MonthlySummary = MonthlySummary(0, 0, 0, 0, 0, emptyList(), emptyList()),
    val sections: List<BillSection> = emptyList(),
    val statusFilter: BillStatusFilter = BillStatusFilter.ALL,
    val categoryFilter: BillCategory? = null,
    val sortOption: BillSortOption = BillSortOption.DUE_DATE_ASC,
    val message: ResultMessage? = null,
)
