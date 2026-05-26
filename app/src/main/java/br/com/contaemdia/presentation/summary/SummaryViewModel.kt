package br.com.contaemdia.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.contaemdia.domain.usecase.BuildMonthlySummaryUseCase
import br.com.contaemdia.domain.usecase.ObserveBillsByMonthUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModel(
    private val observeBillsByMonth: ObserveBillsByMonthUseCase,
    private val buildMonthlySummary: BuildMonthlySummaryUseCase,
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        observeSummary()
    }

    fun onEvent(event: SummaryEvent) {
        when (event) {
            SummaryEvent.PreviousMonth -> month.update { it.minusMonths(1) }
            SummaryEvent.NextMonth -> month.update { it.plusMonths(1) }
        }
    }

    private fun observeSummary() {
        viewModelScope.launch {
            month.flatMapLatest { selectedMonth ->
                observeBillsByMonth(selectedMonth)
            }.collect { bills ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        month = month.value,
                        summary = buildMonthlySummary(bills),
                    )
                }
            }
        }
    }
}
