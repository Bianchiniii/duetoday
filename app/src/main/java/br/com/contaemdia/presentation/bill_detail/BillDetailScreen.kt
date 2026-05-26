package br.com.contaemdia.presentation.bill_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.contaemdia.R
import br.com.contaemdia.domain.model.BillStatus
import br.com.contaemdia.presentation.ads.AdBanner
import br.com.contaemdia.presentation.ads.AdBannerFormat
import br.com.contaemdia.presentation.ads.AdPlacement
import br.com.contaemdia.presentation.theme.ContaEmDiaTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BillDetailRoute(
    billId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onActionCompleted: () -> Unit,
    adsEnabled: Boolean,
    viewModel: BillDetailViewModel = koinViewModel(parameters = { parametersOf(billId) }),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BillDetailScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onEdit = onEdit,
        onActionCompleted = onActionCompleted,
        adsEnabled = adsEnabled,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailScreen(
    state: BillDetailUiState,
    onEvent: (BillDetailEvent) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onActionCompleted: () -> Unit,
    adsEnabled: Boolean,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.shouldReturnHome) {
        if (state.shouldReturnHome) {
            onEvent(BillDetailEvent.ClearMessage)
            onActionCompleted()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bill_detail_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_back))
                    }
                },
                actions = {
                    IconButton(onClick = onEdit, enabled = state.bill != null) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.content_description_edit))
                    }
                },
            )
        },
        bottomBar = {
            AdBanner(
                placement = AdPlacement.DetailBottomBanner,
                adsEnabled = adsEnabled,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                format = AdBannerFormat.BottomAnchored,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
            } else {
                DetailHeader(state)
                BillDetailRows(state)
                BillDetailActions(
                    state = state,
                    onEvent = onEvent,
                    onDeleteClick = { showDeleteDialog = true },
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteBillDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onEvent(BillDetailEvent.Delete)
            },
        )
    }
}

@Composable
private fun BillDetailRows(state: BillDetailUiState) {
    DetailRow(stringResource(R.string.bill_detail_value), state.amount)
    DetailRow(stringResource(R.string.bill_detail_due_date), state.dueDate)
    DetailRow(stringResource(R.string.bill_detail_category), state.category)
    DetailRow(stringResource(R.string.bill_detail_status), state.status)
    DetailRow(stringResource(R.string.bill_detail_recurrence), state.recurring)
    state.paidAt?.let { DetailRow(stringResource(R.string.bill_detail_paid_at), it) }
    if (state.notes.isNotBlank()) DetailRow(stringResource(R.string.bill_detail_notes), state.notes)
}

@Composable
private fun BillDetailActions(
    state: BillDetailUiState,
    onEvent: (BillDetailEvent) -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.bill?.status == BillStatus.PAID) {
            OutlinedButton(onClick = { onEvent(BillDetailEvent.MarkOpen) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
                Text(stringResource(R.string.bill_detail_reopen), modifier = Modifier.padding(start = 8.dp))
            }
        } else {
            Button(onClick = { onEvent(BillDetailEvent.MarkPaid) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Payments, contentDescription = null)
                Text(stringResource(R.string.bill_detail_pay), modifier = Modifier.padding(start = 8.dp))
            }
        }
        OutlinedButton(onClick = onDeleteClick, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Text(stringResource(R.string.bill_detail_delete), modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun DeleteBillDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.bill_detail_delete_title)) },
        text = { Text(stringResource(R.string.bill_detail_delete_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.bill_detail_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
}

@Composable
private fun DetailHeader(state: BillDetailUiState) {
    val containerColor = if (state.isOverdue) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    Card(colors = CardDefaults.cardColors(containerColor = containerColor), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(state.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(state.amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            if (state.isOverdue) {
                Text(stringResource(R.string.bill_detail_overdue), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun BillDetailPreview() {
    ContaEmDiaTheme {
        BillDetailScreen(
            state = BillDetailUiState(title = "Luz", amount = "R$ 210,00", dueDate = "25/05/2026"),
            onEvent = {},
            onBack = {},
            onEdit = {},
            onActionCompleted = {},
            adsEnabled = false,
        )
    }
}
