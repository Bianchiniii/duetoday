package br.com.contaemdia.presentation.bill_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.contaemdia.core.common.ResultMessage
import br.com.contaemdia.core.date.toBrazilianDate
import br.com.contaemdia.core.money.toCurrencyText
import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillStatus
import br.com.contaemdia.domain.usecase.DeleteBillUseCase
import br.com.contaemdia.domain.usecase.MarkBillOpenUseCase
import br.com.contaemdia.domain.usecase.MarkBillPaidUseCase
import br.com.contaemdia.domain.usecase.ObserveBillByIdUseCase
import br.com.contaemdia.notification.BillReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BillDetailUiState(
    val isLoading: Boolean = true,
    val bill: Bill? = null,
    val title: String = "",
    val amount: String = "",
    val dueDate: String = "",
    val category: String = "",
    val status: String = "",
    val recurring: String = "",
    val notes: String = "",
    val paidAt: String? = null,
    val isOverdue: Boolean = false,
    val message: ResultMessage? = null,
    val shouldReturnHome: Boolean = false,
    val error: String? = null,
)

sealed interface BillDetailEvent {
    data object MarkPaid : BillDetailEvent
    data object MarkOpen : BillDetailEvent
    data object Delete : BillDetailEvent
    data object ClearMessage : BillDetailEvent
}

class BillDetailViewModel(
    billId: Long,
    observeBillById: ObserveBillByIdUseCase,
    private val markBillPaid: MarkBillPaidUseCase,
    private val markBillOpen: MarkBillOpenUseCase,
    private val deleteBill: DeleteBillUseCase,
    private val reminderScheduler: BillReminderScheduler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BillDetailUiState())
    val uiState: StateFlow<BillDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeBillById(billId).collect { bill ->
                if (bill == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Conta não encontrada.") }
                } else {
                    _uiState.value = bill.toDetailState().copy(
                        shouldReturnHome = _uiState.value.shouldReturnHome,
                        message = _uiState.value.message,
                    )
                }
            }
        }
    }

    fun onEvent(event: BillDetailEvent) {
        when (event) {
            BillDetailEvent.MarkPaid -> updatePaid()
            BillDetailEvent.MarkOpen -> updateOpen()
            BillDetailEvent.Delete -> delete()
            BillDetailEvent.ClearMessage -> _uiState.update { it.copy(message = null) }
        }
    }

    private fun updatePaid() {
        val bill = _uiState.value.bill ?: return
        viewModelScope.launch {
            markBillPaid(bill)
                .onSuccess {
                    reminderScheduler.cancel(bill.id)
                    _uiState.update {
                        it.copy(
                            message = ResultMessage(text = "Conta marcada como paga."),
                            shouldReturnHome = true,
                        )
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(message = ResultMessage(text = error.message ?: "Falha ao atualizar.")) } }
        }
    }

    private fun updateOpen() {
        val bill = _uiState.value.bill ?: return
        viewModelScope.launch {
            markBillOpen(bill)
                .onSuccess {
                    reminderScheduler.schedule(bill)
                    _uiState.update {
                        it.copy(
                            message = ResultMessage(text = "Conta reaberta."),
                            shouldReturnHome = true,
                        )
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(message = ResultMessage(text = error.message ?: "Falha ao atualizar.")) } }
        }
    }

    private fun delete() {
        val bill = _uiState.value.bill ?: return
        viewModelScope.launch {
            deleteBill(bill)
                .onSuccess {
                    reminderScheduler.cancel(bill.id)
                    _uiState.update {
                        it.copy(
                            message = ResultMessage(text = "Conta excluída."),
                            shouldReturnHome = true,
                        )
                    }
                }
                .onFailure { error -> _uiState.update { it.copy(message = ResultMessage(text = error.message ?: "Falha ao excluir.")) } }
        }
    }

    private fun Bill.toDetailState(): BillDetailUiState = BillDetailUiState(
        isLoading = false,
        bill = this,
        title = title,
        amount = amountCents.toCurrencyText(),
        dueDate = dueDate.toBrazilianDate(),
        category = category.label,
        status = if (status == BillStatus.PAID) "Pago" else "Em aberto",
        recurring = if (isRecurring) "Mensal" else "Não",
        notes = notes.orEmpty(),
        paidAt = paidAt?.toBrazilianDate(),
        isOverdue = isOverdue(),
    )
}
