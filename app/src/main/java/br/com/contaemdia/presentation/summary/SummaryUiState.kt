package br.com.contaemdia.presentation.summary

import br.com.contaemdia.domain.model.MonthlySummary
import java.time.YearMonth

data class SummaryUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val summary: MonthlySummary = MonthlySummary(0, 0, 0, 0, 0, emptyList(), emptyList()),
)
