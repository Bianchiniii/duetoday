package br.com.contaemdia.presentation.bill_form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.contaemdia.core.date.parseBrazilianDateOrNull
import br.com.contaemdia.core.date.toBrazilianDate
import br.com.contaemdia.core.date.toLocalDateFromUtcMillis
import br.com.contaemdia.core.date.toUtcMillis
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.presentation.theme.ContaEmDiaTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BillFormRoute(
    billId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: BillFormViewModel = koinViewModel(parameters = { parametersOf(billId) }),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BillFormScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onSaved = onSaved,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillFormScreen(
    state: BillFormUiState,
    onEvent: (BillFormEvent) -> Unit,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    LaunchedEffect(state.savedBillId) {
        if (state.savedBillId != null) {
            onEvent(BillFormEvent.ClearMessage)
            onSaved()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Editar conta" else "Nova conta") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state.isLoading || state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
            OutlinedTextField(
                value = state.title,
                onValueChange = { onEvent(BillFormEvent.TitleChanged(it)) },
                label = { Text("Nome da conta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.error?.contains("nome", ignoreCase = true) == true,
            )
            OutlinedTextField(
                value = state.amount,
                onValueChange = { onEvent(BillFormEvent.AmountChanged(it)) },
                label = { Text("Valor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("Use apenas números e vírgula. Exemplo: 120,50") },
                isError = state.error?.contains("valor", ignoreCase = true) == true,
            )
            DateInputField(
                value = state.dueDate,
                onValueChange = { onEvent(BillFormEvent.DueDateChanged(it)) },
                isError = state.error?.contains("data", ignoreCase = true) == true,
            )
            Text(text = "Categoria", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BillCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onEvent(BillFormEvent.CategoryChanged(category)) },
                        label = { Text(category.label) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Recorrente mensal", style = MaterialTheme.typography.titleSmall)
                    Text("Gera a próxima ocorrência ao pagar", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = state.isRecurring,
                    onCheckedChange = { onEvent(BillFormEvent.RecurringChanged(it)) },
                )
            }
            OutlinedTextField(
                value = state.notes,
                onValueChange = { onEvent(BillFormEvent.NotesChanged(it)) },
                label = { Text("Observação opcional") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { onEvent(BillFormEvent.Save) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text(if (state.isEditing) "Salvar alterações" else "Salvar conta", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BillFormPreview() {
    ContaEmDiaTheme {
        BillFormScreen(
            state = BillFormUiState(),
            onEvent = {},
            onBack = {},
            onSaved = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text("Data de vencimento") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = true,
            supportingText = { Text("Formato: dd/MM/aaaa") },
            isError = isError,
            trailingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar data")
                }
            },
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true },
        )
    }

    if (showPicker) {
        val selectedDateMillis = value.parseBrazilianDateOrNull()?.toUtcMillis()
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.toLocalDateFromUtcMillis()
                            ?.toBrazilianDate()
                            ?.let(onValueChange)
                        showPicker = false
                    },
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
