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
import br.com.contaemdia.domain.model.RecurrenceType
import br.com.contaemdia.domain.usecase.ObserveBillByIdUseCase
import br.com.contaemdia.domain.usecase.SaveBillUseCase
import br.com.contaemdia.notification.BillReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            observeExistingBill()
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

    private fun observeExistingBill() {
        viewModelScope.launch {
            observeBillById(billId).collect { bill ->
                originalBill = bill
                if (bill == null) {
                    _uiState.update { it.copy(isLoading = false, error = ERROR_BILL_NOT_FOUND) }
                } else {
                    _uiState.update { bill.toFormState() }
                }
            }
        }
    }

    private fun save() {
        val state = _uiState.value
        val validation = validate(state)
        if (validation != null) {
            _uiState.update { it.copy(error = validation) }
            return
        }

        val amountCents = requireNotNull(state.amount.toCentsOrNull())
        val dueDate = requireNotNull(state.dueDate.parseBrazilianDateOrNull())
        val bill = state.toBill(amountCents, dueDate)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            saveBill(bill)
                .onSuccess { id -> handleSaveSuccess(bill, id) }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, error = error.message ?: ERROR_SAVE_BILL) }
                }
        }
    }

    private fun validate(state: BillFormUiState): String? = when {
        state.title.isBlank() -> ERROR_EMPTY_TITLE
        state.amount.toCentsOrNull() == null -> ERROR_INVALID_AMOUNT
        state.dueDate.parseBrazilianDateOrNull() == null -> ERROR_INVALID_DATE
        else -> null
    }

    private fun handleSaveSuccess(bill: Bill, id: Long) {
        reminderScheduler.schedule(bill.copy(id = id))
        _uiState.update {
            it.copy(
                isSaving = false,
                message = ResultMessage(text = if (bill.id == 0L) MESSAGE_BILL_SAVED else MESSAGE_BILL_UPDATED),
                savedBillId = id,
            )
        }
    }

    private fun Bill.toFormState(): BillFormUiState =
        _uiState.value.copy(
            isLoading = false,
            title = title,
            amount = amountCents.toDecimalInput(),
            dueDate = dueDate.toBrazilianDate(),
            category = category,
            isRecurring = isRecurring,
            notes = notes.orEmpty(),
            error = null,
        )

    private fun BillFormUiState.toBill(amountCents: Long, dueDate: java.time.LocalDate): Bill =
        (originalBill ?: Bill(
            title = title,
            amountCents = amountCents,
            dueDate = dueDate,
            category = category,
        )).copy(
            title = title,
            amountCents = amountCents,
            dueDate = dueDate,
            category = category,
            isRecurring = isRecurring,
            recurrenceType = if (isRecurring) RecurrenceType.MONTHLY else RecurrenceType.NONE,
            notes = notes,
        )

    private companion object {
        const val ERROR_BILL_NOT_FOUND = "Conta não encontrada."
        const val ERROR_EMPTY_TITLE = "Informe o nome da conta."
        const val ERROR_INVALID_AMOUNT = "Informe um valor válido."
        const val ERROR_INVALID_DATE = "Informe uma data no formato dd/MM/aaaa."
        const val ERROR_SAVE_BILL = "Não foi possível salvar."
        const val MESSAGE_BILL_SAVED = "Conta salva."
        const val MESSAGE_BILL_UPDATED = "Conta atualizada."
    }
}
