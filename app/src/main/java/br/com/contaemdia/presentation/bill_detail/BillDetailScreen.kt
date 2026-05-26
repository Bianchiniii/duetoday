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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                title = { Text("Detalhe da conta") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit, enabled = state.bill != null) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
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
                DetailRow("Valor", state.amount)
                DetailRow("Vencimento", state.dueDate)
                DetailRow("Categoria", state.category)
                DetailRow("Status", state.status)
                DetailRow("Recorrência", state.recurring)
                state.paidAt?.let { DetailRow("Pago em", it) }
                if (state.notes.isNotBlank()) DetailRow("Observação", state.notes)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.bill?.status == BillStatus.PAID) {
                        OutlinedButton(onClick = { onEvent(BillDetailEvent.MarkOpen) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
                            Text("Reabrir", modifier = Modifier.padding(start = 8.dp))
                        }
                    } else {
                        Button(onClick = { onEvent(BillDetailEvent.MarkPaid) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Payments, contentDescription = null)
                            Text("Pagar conta", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Text("Excluir", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir conta?") },
            text = { Text("Essa ação remove a conta do aparelho.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onEvent(BillDetailEvent.Delete)
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
        )
    }
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
                Text("Conta atrasada", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
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
