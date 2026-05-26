package br.com.contaemdia.presentation.bill_detail

import br.com.contaemdia.core.common.ResultMessage
import br.com.contaemdia.domain.model.Bill

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
