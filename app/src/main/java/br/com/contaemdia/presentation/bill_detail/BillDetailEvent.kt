package br.com.contaemdia.presentation.bill_detail

sealed interface BillDetailEvent {
    data object MarkPaid : BillDetailEvent
    data object MarkOpen : BillDetailEvent
    data object Delete : BillDetailEvent
    data object ClearMessage : BillDetailEvent
}
