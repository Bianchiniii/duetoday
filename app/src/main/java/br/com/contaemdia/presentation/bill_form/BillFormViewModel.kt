package br.com.contaemdia.presentation.bill_form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.contaemdia.core.common.ResultMessage
import br.com.contaemdia.core.date.parseBrazilianDateOrNull
import br.com.contaemdia.core.date.toBrazilianDate
import br.com.contaemdia.core.money.toCentsOrNull
import br.com.contaemdia.core.money.toDecimalInput
import br.com.contaemdia.core.money.toMoneyInput
import br.com.contaemdia.domain.model.Bill
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.model.RecurrenceType
import br.com.contaemdia.domain.usecase.ObserveBillByIdUseCase
import br.com.contaemdia.domain.usecase.SaveBillUseCase
import br.com.contaemdia.notification.BillReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class BillFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val title: String = "",
    val amount: String = "",
    val dueDate: String = LocalDate.now().toBrazilianDate(),
    val category: BillCategory = BillCategory.OTHER,
    val isRecurring: Boolean = false,
    val notes: String = "",
    val error: String? = null,
    val message: ResultMessage? = null,
    val savedBillId: Long? = null,
    val isEditing: Boolean = false,
)

sealed interface BillFormEvent {
    data class TitleChanged(val value: String) : BillFormEvent
    data class AmountChanged(val value: String) : BillFormEvent
    data class DueDateChanged(val value: String) : BillFormEvent
    data class CategoryChanged(val value: BillCategory) : BillFormEvent
    data class RecurringChanged(val value: Boolean) : BillFormEvent
    data class NotesChanged(val value: String) : BillFormEvent
    data object Save : BillFormEvent
    data object ClearMessage : BillFormEvent
}

class BillFormViewModel(
    private val billId: Long,
    private val observeBillById: ObserveBillByIdUseCase,
    private val saveBill: SaveBillUseCase,
    private val reminderScheduler: BillReminderScheduler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BillFormUiState(isLoading = billId > 0, isEditing = billId > 0))
    val uiState: StateFlow<BillFormUiState> = _uiState.asStateFlow()
    private var originalBill: Bill? = null

    init {
        if (billId > 0) {
            viewModelScope.launch {
                observeBillById(billId).collect { bill ->
                    originalBill = bill
                    if (bill == null) {
                        _uiState.update { it.copy(isLoading = false, error = "Conta não encontrada.") }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                title = bill.title,
                                amount = bill.amountCents.toDecimalInput(),
                                dueDate = bill.dueDate.toBrazilianDate(),
                                category = bill.category,
                                isRecurring = bill.isRecurring,
                                notes = bill.notes.orEmpty(),
                                error = null,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onEvent(event: BillFormEvent) {
        when (event) {
            is BillFormEvent.TitleChanged -> _uiState.update { it.copy(title = event.value, error = null) }
            is BillFormEvent.AmountChanged -> _uiState.update { it.copy(amount = event.value.toMoneyInput(), error = null) }
            is BillFormEvent.DueDateChanged -> _uiState.update { it.copy(dueDate = event.value, error = null) }
            is BillFormEvent.CategoryChanged -> _uiState.update { it.copy(category = event.value) }
            is BillFormEvent.RecurringChanged -> _uiState.update { it.copy(isRecurring = event.value) }
            is BillFormEvent.NotesChanged -> _uiState.update { it.copy(notes = event.value) }
            BillFormEvent.Save -> save()
            BillFormEvent.ClearMessage -> _uiState.update { it.copy(message = null, savedBillId = null) }
        }
    }

    private fun save() {
        val state = _uiState.value
        val amountCents = state.amount.toCentsOrNull()
        val dueDate = state.dueDate.parseBrazilianDateOrNull()
        when {
            state.title.isBlank() -> {
                _uiState.update { it.copy(error = "Informe o nome da conta.") }
                return
            }
            amountCents == null -> {
                _uiState.update { it.copy(error = "Informe um valor válido.") }
                return
            }
            dueDate == null -> {
                _uiState.update { it.copy(error = "Informe uma data no formato dd/MM/aaaa.") }
                return
            }
        }

        val bill = (originalBill ?: Bill(
            title = state.title,
            amountCents = amountCents,
            dueDate = dueDate,
            category = state.category,
        )).copy(
            title = state.title,
            amountCents = amountCents,
            dueDate = dueDate,
            category = state.category,
            isRecurring = state.isRecurring,
            recurrenceType = if (state.isRecurring) RecurrenceType.MONTHLY else RecurrenceType.NONE,
            notes = state.notes,
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            saveBill(bill)
                .onSuccess { id ->
                    reminderScheduler.schedule(bill.copy(id = id))
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = ResultMessage(text = if (bill.id == 0L) "Conta salva." else "Conta atualizada."),
                            savedBillId = id,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, error = error.message ?: "Não foi possível salvar.") }
                }
        }
    }
}
