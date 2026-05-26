package br.com.contaemdia.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.contaemdia.core.common.ResultMessage
import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.model.BillSortOption
import br.com.contaemdia.domain.model.BillStatus
import br.com.contaemdia.domain.model.BillStatusFilter
import br.com.contaemdia.domain.model.MonthlySummary
import br.com.contaemdia.domain.usecase.BuildMonthlySummaryUseCase
import br.com.contaemdia.domain.usecase.FilterSortBillsUseCase
import br.com.contaemdia.domain.usecase.MarkBillPaidUseCase
import br.com.contaemdia.domain.usecase.ObserveBillsByMonthUseCase
import br.com.contaemdia.notification.BillReminderScheduler
import br.com.contaemdia.presentation.components.BillUiModel
import br.com.contaemdia.presentation.components.toUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
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

sealed interface DashboardEvent {
    data object PreviousMonth : DashboardEvent
    data object NextMonth : DashboardEvent
    data class ChangeStatusFilter(val filter: BillStatusFilter) : DashboardEvent
    data class ChangeCategoryFilter(val category: BillCategory?) : DashboardEvent
    data class ChangeSortOption(val option: BillSortOption) : DashboardEvent
    data class MarkPaid(val billId: Long) : DashboardEvent
    data object ClearMessage : DashboardEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val observeBillsByMonth: ObserveBillsByMonthUseCase,
    private val buildMonthlySummary: BuildMonthlySummaryUseCase,
    private val filterSortBills: FilterSortBillsUseCase,
    private val markBillPaid: MarkBillPaidUseCase,
    private val reminderScheduler: BillReminderScheduler,
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val statusFilter = MutableStateFlow(BillStatusFilter.ALL)
    private val categoryFilter = MutableStateFlow<BillCategory?>(null)
    private val sortOption = MutableStateFlow(BillSortOption.DUE_DATE_ASC)
    private val currentBills = MutableStateFlow<List<Bill>>(emptyList())

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(month, statusFilter, categoryFilter, sortOption) { selectedMonth, status, category, sort ->
                FilterInput(selectedMonth, status, category, sort)
            }
                .flatMapLatest { input ->
                    observeBillsByMonth(input.month).combine(
                        combine(statusFilter, categoryFilter, sortOption) { status, category, sort ->
                            Triple(status, category, sort)
                        }
                    ) { bills, filters -> input.copy(status = filters.first, category = filters.second, sort = filters.third) to bills }
                }
                .collect { (input, bills) ->
                    currentBills.value = bills
                    val today = LocalDate.now()
                    val filtered = filterSortBills(bills, input.status, input.category, input.sort, today)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            month = input.month,
                            summary = buildMonthlySummary(bills, today),
                            sections = buildSections(filtered, today),
                            statusFilter = input.status,
                            categoryFilter = input.category,
                            sortOption = input.sort,
                        )
                    }
                }
        }
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.PreviousMonth -> month.update { it.minusMonths(1) }
            DashboardEvent.NextMonth -> month.update { it.plusMonths(1) }
            is DashboardEvent.ChangeStatusFilter -> statusFilter.value = event.filter
            is DashboardEvent.ChangeCategoryFilter -> categoryFilter.value = event.category
            is DashboardEvent.ChangeSortOption -> sortOption.value = event.option
            is DashboardEvent.MarkPaid -> markPaid(event.billId)
            DashboardEvent.ClearMessage -> _uiState.update { it.copy(message = null) }
        }
    }

    private fun markPaid(billId: Long) {
        val bill = currentBills.value.firstOrNull { it.id == billId } ?: return
        viewModelScope.launch {
            markBillPaid(bill)
                .onSuccess {
                    reminderScheduler.cancel(bill.id)
                    _uiState.update { it.copy(message = ResultMessage(text = "Conta marcada como paga.")) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(message = ResultMessage(text = error.message ?: "Não foi possível pagar.")) }
                }
        }
    }

    private fun buildSections(bills: List<Bill>, today: LocalDate): List<BillSection> {
        val groups = listOf(
            "Atrasados" to bills.filter { it.isOverdue(today) },
            "Vence hoje" to bills.filter { it.isDueToday(today) },
            "Próximos 7 dias" to bills.filter { it.isDueInNextDays(7, today) },
            "Próximos vencimentos" to bills.filter {
                it.status == BillStatus.OPEN && !it.isOverdue(today) && !it.isDueToday(today) && !it.isDueInNextDays(7, today)
            },
            "Pagos" to bills.filter { it.status == BillStatus.PAID },
        )
        return groups
            .filter { it.second.isNotEmpty() }
            .map { (title, sectionBills) -> BillSection(title, sectionBills.map { it.toUiModel(today) }) }
    }

    private data class FilterInput(
        val month: YearMonth,
        val status: BillStatusFilter,
        val category: BillCategory?,
        val sort: BillSortOption,
    )
}
