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
                    _uiState.update { it.copy(isLoading = false, error = ERROR_BILL_NOT_FOUND) }
                } else {
                    updateBillState(bill)
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

    private fun updateBillState(bill: Bill) {
        _uiState.value = bill.toDetailState().copy(
            shouldReturnHome = _uiState.value.shouldReturnHome,
            message = _uiState.value.message,
        )
    }

    private fun updatePaid() {
        val bill = _uiState.value.bill ?: return
        viewModelScope.launch {
            markBillPaid(bill)
                .onSuccess {
                    reminderScheduler.cancel(bill.id)
                    showSuccessAndReturnHome(MESSAGE_MARKED_PAID)
                }
                .onFailure { error -> showError(error.message ?: ERROR_UPDATE_BILL) }
        }
    }

    private fun updateOpen() {
        val bill = _uiState.value.bill ?: return
        viewModelScope.launch {
            markBillOpen(bill)
                .onSuccess {
                    reminderScheduler.schedule(bill)
                    showSuccessAndReturnHome(MESSAGE_REOPENED)
                }
                .onFailure { error -> showError(error.message ?: ERROR_UPDATE_BILL) }
        }
    }

    private fun delete() {
        val bill = _uiState.value.bill ?: return
        viewModelScope.launch {
            deleteBill(bill)
                .onSuccess {
                    reminderScheduler.cancel(bill.id)
                    showSuccessAndReturnHome(MESSAGE_DELETED)
                }
                .onFailure { error -> showError(error.message ?: ERROR_DELETE_BILL) }
        }
    }

    private fun showSuccessAndReturnHome(message: String) {
        _uiState.update {
            it.copy(
                message = ResultMessage(text = message),
                shouldReturnHome = true,
            )
        }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(message = ResultMessage(text = message)) }
    }

    private fun Bill.toDetailState(): BillDetailUiState = BillDetailUiState(
        isLoading = false,
        bill = this,
        title = title,
        amount = amountCents.toCurrencyText(),
        dueDate = dueDate.toBrazilianDate(),
        category = category.label,
        status = if (status == BillStatus.PAID) STATUS_PAID else STATUS_OPEN,
        recurring = if (isRecurring) RECURRENCE_MONTHLY else RECURRENCE_NONE,
        notes = notes.orEmpty(),
        paidAt = paidAt?.toBrazilianDate(),
        isOverdue = isOverdue(),
    )

    private companion object {
        const val ERROR_BILL_NOT_FOUND = "Conta não encontrada."
        const val ERROR_UPDATE_BILL = "Falha ao atualizar."
        const val ERROR_DELETE_BILL = "Falha ao excluir."
        const val MESSAGE_MARKED_PAID = "Conta marcada como paga."
        const val MESSAGE_REOPENED = "Conta reaberta."
        const val MESSAGE_DELETED = "Conta excluída."
        const val STATUS_PAID = "Pago"
        const val STATUS_OPEN = "Em aberto"
        const val RECURRENCE_MONTHLY = "Mensal"
        const val RECURRENCE_NONE = "Não"
    }
}
