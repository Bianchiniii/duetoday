package br.com.contaemdia.presentation.bill_form

import br.com.contaemdia.core.common.ResultMessage
import br.com.contaemdia.core.date.toBrazilianDate
import br.com.contaemdia.domain.model.BillCategory
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
