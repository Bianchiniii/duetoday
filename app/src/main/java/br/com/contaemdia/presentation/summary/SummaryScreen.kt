package br.com.contaemdia.presentation.summary

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.contaemdia.R
import br.com.contaemdia.core.date.toBrazilianMonth
import br.com.contaemdia.core.money.toCurrencyText
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.model.CategorySummary
import br.com.contaemdia.presentation.ads.AdBanner
import br.com.contaemdia.presentation.ads.AdBannerFormat
import br.com.contaemdia.presentation.ads.AdPlacement
import br.com.contaemdia.presentation.components.SummaryCard
import br.com.contaemdia.presentation.theme.ContaEmDiaTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SummaryRoute(
    onBack: () -> Unit,
    adsEnabled: Boolean,
    viewModel: SummaryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SummaryScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        adsEnabled = adsEnabled,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SummaryScreen(
    state: SummaryUiState,
    onEvent: (SummaryEvent) -> Unit,
    onBack: () -> Unit,
    adsEnabled: Boolean,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.summary_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_back))
                    }
                },
            )
        },
        bottomBar = {
            AdBanner(
                placement = AdPlacement.SummaryBottomBanner,
                adsEnabled = adsEnabled,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                format = AdBannerFormat.BottomAnchored,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.isLoading) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }
            item {
                SummaryMonthHeader(
                    month = state.month.toBrazilianMonth(),
                    onPrevious = { onEvent(SummaryEvent.PreviousMonth) },
                    onNext = { onEvent(SummaryEvent.NextMonth) },
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard(stringResource(R.string.summary_total_general), state.summary.totalCents.toCurrencyText(), Modifier.width(SUMMARY_CARD_WIDTH))
                    SummaryCard(stringResource(R.string.summary_paid), state.summary.paidCents.toCurrencyText(), Modifier.width(SUMMARY_CARD_WIDTH))
                    SummaryCard(stringResource(R.string.summary_open), state.summary.openCents.toCurrencyText(), Modifier.width(SUMMARY_CARD_WIDTH))
                    SummaryCard(stringResource(R.string.summary_overdue), state.summary.overdueCents.toCurrencyText(), Modifier.width(SUMMARY_CARD_WIDTH), MaterialTheme.colorScheme.error)
                }
            }
            item {
                AdBanner(
                    placement = AdPlacement.SummaryInlineBanner,
                    adsEnabled = adsEnabled,
                )
            }
            item {
                Text(stringResource(R.string.summary_by_category), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (state.summary.byCategory.isEmpty()) {
                item { Text(stringResource(R.string.summary_empty_month), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(state.summary.byCategory) { item ->
                    SummaryLine(
                        title = item.category.label,
                        subtitle = stringResource(R.string.summary_bill_count, item.count),
                        value = item.totalCents.toCurrencyText(),
                    )
                }
            }
            item {
                Text(stringResource(R.string.summary_biggest_bills), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(state.summary.biggestBills) { bill ->
                SummaryLine(
                    title = bill.title,
                    subtitle = bill.category.label,
                    value = bill.amountCents.toCurrencyText(),
                )
            }
        }
    }
}

@Composable
private fun SummaryMonthHeader(month: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.content_description_month_previous))
        }
        Text(month, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.content_description_month_next))
        }
    }
}

@Composable
private fun SummaryLine(title: String, subtitle: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryPreview() {
    ContaEmDiaTheme {
        SummaryScreen(
            state = SummaryUiState(
                isLoading = false,
                summary = br.com.contaemdia.domain.model.MonthlySummary(
                    paidCents = 10000,
                    openCents = 5000,
                    overdueCents = 2500,
                    totalCents = 15000,
                    dueNextSevenDaysCents = 5000,
                    byCategory = listOf(CategorySummary(BillCategory.INTERNET, 10000, 1)),
                    biggestBills = emptyList(),
                ),
            ),
            onEvent = {},
            onBack = {},
            adsEnabled = false,
        )
    }
}

private val SUMMARY_CARD_WIDTH = 160.dp
