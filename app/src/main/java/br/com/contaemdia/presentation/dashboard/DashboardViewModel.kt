package br.com.contaemdia.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.contaemdia.core.common.ResultMessage
import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.model.BillSortOption
import br.com.contaemdia.domain.model.BillStatus
import br.com.contaemdia.domain.model.BillStatusFilter
import br.com.contaemdia.domain.usecase.BuildMonthlySummaryUseCase
import br.com.contaemdia.domain.usecase.FilterSortBillsUseCase
import br.com.contaemdia.domain.usecase.MarkBillPaidUseCase
import br.com.contaemdia.domain.usecase.ObserveBillsByMonthUseCase
import br.com.contaemdia.notification.BillReminderScheduler
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
        observeDashboard()
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

    private fun observeDashboard() {
        viewModelScope.launch {
            combine(month, statusFilter, categoryFilter, sortOption) { selectedMonth, status, category, sort ->
                FilterInput(selectedMonth, status, category, sort)
            }
                .flatMapLatest { input ->
                    observeBillsByMonth(input.month).combine(
                        combine(statusFilter, categoryFilter, sortOption) { status, category, sort ->
                            FilterSelection(status, category, sort)
                        }
                    ) { bills, filters ->
                        input.copy(
                            status = filters.status,
                            category = filters.category,
                            sort = filters.sort,
                        ) to bills
                    }
                }
                .collect { (input, bills) ->
                    updateDashboardState(input, bills)
                }
        }
    }

    private fun updateDashboardState(input: FilterInput, bills: List<Bill>) {
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

    private fun markPaid(billId: Long) {
        val bill = currentBills.value.firstOrNull { it.id == billId } ?: return
        viewModelScope.launch {
            markBillPaid(bill)
                .onSuccess {
                    reminderScheduler.cancel(bill.id)
                    _uiState.update { it.copy(message = ResultMessage(text = MESSAGE_MARKED_PAID)) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(message = ResultMessage(text = error.message ?: ERROR_PAY_BILL)) }
                }
        }
    }

    private fun buildSections(bills: List<Bill>, today: LocalDate): List<BillSection> {
        val groups = listOf(
            SECTION_OVERDUE to bills.filter { it.isOverdue(today) },
            SECTION_DUE_TODAY to bills.filter { it.isDueToday(today) },
            SECTION_NEXT_SEVEN_DAYS to bills.filter { it.isDueInNextDays(NEXT_DAYS_WINDOW, today) },
            SECTION_UPCOMING to bills.filter {
                it.status == BillStatus.OPEN &&
                    !it.isOverdue(today) &&
                    !it.isDueToday(today) &&
                    !it.isDueInNextDays(NEXT_DAYS_WINDOW, today)
            },
            SECTION_PAID to bills.filter { it.status == BillStatus.PAID },
        )

        return groups
            .filter { it.second.isNotEmpty() }
            .map { (title, sectionBills) ->
                BillSection(title, sectionBills.map { it.toUiModel(today) })
            }
    }

    private data class FilterInput(
        val month: YearMonth,
        val status: BillStatusFilter,
        val category: BillCategory?,
        val sort: BillSortOption,
    )

    private data class FilterSelection(
        val status: BillStatusFilter,
        val category: BillCategory?,
        val sort: BillSortOption,
    )

    private companion object {
        const val NEXT_DAYS_WINDOW = 7L
        const val SECTION_OVERDUE = "Atrasados"
        const val SECTION_DUE_TODAY = "Vence hoje"
        const val SECTION_NEXT_SEVEN_DAYS = "Próximos 7 dias"
        const val SECTION_UPCOMING = "Próximos vencimentos"
        const val SECTION_PAID = "Pagos"
        const val MESSAGE_MARKED_PAID = "Conta marcada como paga."
        const val ERROR_PAY_BILL = "Não foi possível pagar."
    }
}
