package br.com.contaemdia.presentation.bill_form

import br.com.contaemdia.domain.model.BillCategory

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
