package br.com.contaemdia.presentation.dashboard

import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.model.BillSortOption
import br.com.contaemdia.domain.model.BillStatusFilter

sealed interface DashboardEvent {
    data object PreviousMonth : DashboardEvent
    data object NextMonth : DashboardEvent
    data class ChangeStatusFilter(val filter: BillStatusFilter) : DashboardEvent
    data class ChangeCategoryFilter(val category: BillCategory?) : DashboardEvent
    data class ChangeSortOption(val option: BillSortOption) : DashboardEvent
    data class MarkPaid(val billId: Long) : DashboardEvent
    data object ClearMessage : DashboardEvent
}
