package br.com.contaemdia.presentation.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.contaemdia.core.date.toBrazilianMonth
import br.com.contaemdia.core.money.toCurrencyText
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.model.BillSortOption
import br.com.contaemdia.domain.model.BillStatusFilter
import br.com.contaemdia.presentation.ads.AdBanner
import br.com.contaemdia.presentation.ads.AdPlacement
import br.com.contaemdia.presentation.components.BillListItem
import br.com.contaemdia.presentation.components.EmptyState
import br.com.contaemdia.presentation.components.SummaryCard
import br.com.contaemdia.presentation.theme.ContaEmDiaTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardRoute(
    onAddBill: () -> Unit,
    onOpenBill: (Long) -> Unit,
    onOpenSummary: () -> Unit,
    adsEnabled: Boolean,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onAddBill = onAddBill,
        onOpenBill = onOpenBill,
        onOpenSummary = onOpenSummary,
        adsEnabled = adsEnabled,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onEvent: (DashboardEvent) -> Unit,
    onAddBill: () -> Unit,
    onOpenBill: (Long) -> Unit,
    onOpenSummary: () -> Unit,
    adsEnabled: Boolean,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Conta em Dia") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = onOpenSummary) {
                        Icon(Icons.Default.Analytics, contentDescription = "Resumo mensal")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBill) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar conta")
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            MonthHeader(
                month = state.month.toBrazilianMonth(),
                onPrevious = { onEvent(DashboardEvent.PreviousMonth) },
                onNext = { onEvent(DashboardEvent.NextMonth) },
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Prioridade de pagamento",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Veja primeiro o que está atrasado, vence hoje ou está perto de vencer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SummaryCard("Total", state.summary.totalCents.toCurrencyText(), Modifier.width(156.dp), MaterialTheme.colorScheme.primary)
                        SummaryCard("Pago", state.summary.paidCents.toCurrencyText(), Modifier.width(156.dp), Color(0xFF21894F))
                        SummaryCard("Aberto", state.summary.openCents.toCurrencyText(), Modifier.width(156.dp), MaterialTheme.colorScheme.secondary)
                        SummaryCard("Atrasado", state.summary.overdueCents.toCurrencyText(), Modifier.width(156.dp), MaterialTheme.colorScheme.error)
                        SummaryCard("7 dias", state.summary.dueNextSevenDaysCents.toCurrencyText(), Modifier.width(156.dp), Color(0xFFB98900))
                    }
                }
                item {
                    AdBanner(
                        placement = AdPlacement.DashboardBanner,
                        adsEnabled = adsEnabled,
                    )
                }
                item {
                    FilterRows(state = state, onEvent = onEvent)
                }
                if (state.sections.isEmpty() && !state.isLoading) {
                    item {
                        EmptyState(
                            title = "Nenhuma conta neste mês",
                            description = "Use o botão de adicionar para cadastrar seu primeiro boleto.",
                            modifier = Modifier.padding(top = 56.dp),
                        )
                    }
                } else {
                    state.sections.forEach { section ->
                        item {
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        items(section.bills, key = { it.id }) { bill ->
                            BillListItem(
                                bill = bill,
                                onClick = { onOpenBill(bill.id) },
                                onMarkPaid = { onEvent(DashboardEvent.MarkPaid(bill.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(month: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Mês anterior")
        }
        Text(text = month, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Próximo mês")
        }
    }
}

@Composable
private fun FilterRows(state: DashboardUiState, onEvent: (DashboardEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BillStatusFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.statusFilter == filter,
                    onClick = { onEvent(DashboardEvent.ChangeStatusFilter(filter)) },
                    label = { Text(filter.label) },
                )
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.categoryFilter == null,
                onClick = { onEvent(DashboardEvent.ChangeCategoryFilter(null)) },
                label = { Text("Todas categorias") },
            )
            BillCategory.entries.forEach { category ->
                FilterChip(
                    selected = state.categoryFilter == category,
                    onClick = { onEvent(DashboardEvent.ChangeCategoryFilter(category)) },
                    label = { Text(category.label) },
                )
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BillSortOption.entries.forEach { option ->
                FilterChip(
                    selected = state.sortOption == option,
                    onClick = { onEvent(DashboardEvent.ChangeSortOption(option)) },
                    label = { Text(option.label) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    ContaEmDiaTheme {
        DashboardScreen(
            state = DashboardUiState(isLoading = false),
            onEvent = {},
            onAddBill = {},
            onOpenBill = {},
            onOpenSummary = {},
            adsEnabled = false,
        )
    }
}
