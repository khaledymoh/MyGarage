package com.almudier.mygarage.vehicle.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.almudier.mygarage.core.domain.model.EmptyVehicle
import com.almudier.mygarage.core.domain.model.MaintenanceItem
import com.almudier.mygarage.core.domain.model.MaintenanceRecord
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.domain.model.Vehicle
import com.almudier.mygarage.core.domain.model.cost
import com.almudier.mygarage.core.domain.model.firstRecordDate
import com.almudier.mygarage.core.domain.model.totalsByType
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.BarSegment
import com.almudier.mygarage.core.presentation.components.GarageCard
import com.almudier.mygarage.core.presentation.components.GarageDot
import com.almudier.mygarage.core.presentation.components.GarageEmptyState
import com.almudier.mygarage.core.presentation.components.GarageProgressBar
import com.almudier.mygarage.core.presentation.components.GarageSectionHeader
import com.almudier.mygarage.core.presentation.components.GarageStackedBar
import com.almudier.mygarage.core.presentation.components.GarageTab
import com.almudier.mygarage.core.presentation.components.GarageTabs
import com.almudier.mygarage.core.presentation.components.LocalMainBottomBarInset
import com.almudier.mygarage.core.presentation.components.SnackbarManager
import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.mvi.UiEffect
import com.almudier.mygarage.core.presentation.mvi.UiIntent
import com.almudier.mygarage.core.presentation.mvi.UiState
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.core.presentation.shared.Amount
import com.almudier.mygarage.core.presentation.shared.AmountSize
import com.almudier.mygarage.core.presentation.shared.LocalCurrency
import com.almudier.mygarage.core.presentation.shared.color
import com.almudier.mygarage.core.presentation.shared.formatMoney
import com.almudier.mygarage.core.presentation.shared.pluralLabel
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.core.presentation.toErrorMessageRes
import com.almudier.mygarage.vehicle.R
import com.almudier.mygarage.vehicle.api.InsightsRoute
import com.almudier.mygarage.vehicle.api.VehicleProfileRoute
import com.almudier.mygarage.vehicle.common.CarHeader
import com.almudier.mygarage.vehicle.common.CarRecordsLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class InsightsPeriod { ThisYear, AllTime }

object InsightsContract {
    data class State(
        val vehicle: Vehicle = EmptyVehicle,
        val records: List<MaintenanceRecord> = emptyList(),
        val period: InsightsPeriod = InsightsPeriod.ThisYear,
        val isLoading: Boolean = true,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class Load(val vehicleId: String) : Intent
        data class SetPeriod(val period: InsightsPeriod) : Intent
        data object OpenProfile : Intent
        data object SwitchCar : Intent
    }

    sealed interface Effect : UiEffect
}

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val navigator: Navigator,
    private val loader: CarRecordsLoader,
    private val snackbarManager: SnackbarManager,
) : BaseMviViewModel<InsightsContract.Intent, InsightsContract.State, InsightsContract.Effect>(InsightsContract.State()) {

    private var vehicleId: String? = null
    private var loadJob: Job? = null

    override fun onEvent(intent: InsightsContract.Intent) {
        when (intent) {
            is InsightsContract.Intent.Load -> load(intent.vehicleId)
            is InsightsContract.Intent.SetPeriod -> setState { copy(period = intent.period) }
            InsightsContract.Intent.OpenProfile -> vehicleId?.let { navigator.navigate(VehicleProfileRoute(it)) }
            InsightsContract.Intent.SwitchCar -> loader.switchCar()
        }
    }

    private fun load(id: String) {
        if (vehicleId == id) return
        vehicleId = id
        loadJob?.cancel()
        loadJob = loader.observe(id)
            .onEach { data ->
                if (data == null) {
                    viewModelScope.launch { loader.onCarMissing() }
                    return@onEach
                }
                setState { copy(vehicle = data.vehicle, records = data.records, isLoading = false) }
            }
            .catch { snackbarManager.showError(it.toErrorMessageRes()) }
            .launchIn(viewModelScope)
    }
}

@Composable
fun InsightsScreen(route: InsightsRoute, viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(route.vehicleId) { viewModel.onEvent(InsightsContract.Intent.Load(route.vehicleId)) }
    InsightsContent(state.vehicle, state.records, state.period, state.isLoading, viewModel::onEvent)
}

@Composable
fun InsightsContent(
    vehicle: Vehicle,
    records: List<MaintenanceRecord>,
    period: InsightsPeriod,
    isLoading: Boolean,
    onEvent: (InsightsContract.Intent) -> Unit,
    today: LocalDate = remember { LocalDate.now() },
) {
    val spacing = GarageDesignTheme.spacing
    val colors = GarageDesignTheme.colors
    val currency = LocalCurrency.current
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
        return
    }
    val inPeriod = remember(records, period, today) {
        if (period == InsightsPeriod.AllTime) records
        else records.filter { runCatching { LocalDate.parse(it.date).year == today.year }.getOrDefault(false) }
    }
    val totals = totalsByType(inPeriod)
    val total = totals.values.sum()

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.nr)
            .padding(bottom = LocalMainBottomBarInset.current + spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        CarHeader(vehicle, onOpenProfile = { onEvent(InsightsContract.Intent.OpenProfile) }, onSwitchCar = { onEvent(InsightsContract.Intent.SwitchCar) })
        GarageTabs {
            GarageTab(stringResource(R.string.insights_this_year), period == InsightsPeriod.ThisYear, { onEvent(InsightsContract.Intent.SetPeriod(InsightsPeriod.ThisYear)) })
            GarageTab(stringResource(R.string.insights_all_time), period == InsightsPeriod.AllTime, { onEvent(InsightsContract.Intent.SetPeriod(InsightsPeriod.AllTime)) })
        }
        if (inPeriod.isEmpty()) {
            GarageCard(Modifier.fillMaxWidth()) {
                GarageEmptyState(Icons.Rounded.Insights, stringResource(R.string.insights_empty_title), text = stringResource(R.string.insights_empty_text))
            }
            return@Column
        }

        GarageCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
            GarageSectionHeader(stringResource(R.string.insights_breakdown))
            Amount(total, size = AmountSize.Hero, modifier = Modifier.padding(top = 2.dp))
            GarageStackedBar(RecordType.entries.map { BarSegment(totals.getValue(it).toFloat(), it.color) }, Modifier.padding(top = 12.dp, bottom = 8.dp))
            RecordType.entries.forEach { type ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    GarageDot(type.color, size = 8.dp)
                    Text(stringResource(type.pluralLabel), style = GarageDesignTheme.typography.body.copy(color = colors.dim), modifier = Modifier.weight(1f).padding(start = 8.dp))
                    Amount(totals.getValue(type), size = AmountSize.Small)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            val first = firstRecordDate(inPeriod) ?: today
            val months = (ChronoUnit.MONTHS.between(first.withDayOfMonth(1), today.withDayOfMonth(1)) + 1).coerceAtLeast(1)
            Tile(stringResource(R.string.insights_per_month), stringResource(R.string.insights_per_month_caption), Modifier.weight(1f)) {
                Amount(total / months, size = AmountSize.Large)
            }
            val washes = inPeriod.filter { it.type == RecordType.Detailing }
            Tile(stringResource(R.string.insights_washes), stringResource(R.string.insights_washes_caption, formatMoney(washes.sumOf { it.cost }, currency)), Modifier.weight(1f)) {
                Text(washes.size.toString(), style = GarageDesignTheme.typography.number)
            }
        }

        val buildItems = remember(inPeriod) {
            inPeriod.filter { it.type == RecordType.Upgrade }.flatMap { it.items }.sortedByDescending { it.cost + it.laborCost }
        }
        if (buildItems.isNotEmpty()) {
            val max = buildItems.maxOf { it.cost + it.laborCost }.coerceAtLeast(1.0)
            GarageCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                GarageSectionHeader(
                    stringResource(R.string.insights_build_list),
                    trailing = { Text(pluralStringResource(R.plurals.insights_build_parts, buildItems.size, buildItems.size)) },
                )
                buildItems.forEach { item -> BuildRow(item, max) }
            }
        }

        val places = remember(inPeriod) {
            inPeriod.filter { it.workshop.isNotBlank() }.groupBy { it.workshop.trim() }
                .mapValues { (_, list) -> list.sumOf { it.cost } }
                .entries.sortedByDescending { it.value }.take(4)
        }
        if (places.isNotEmpty()) {
            val max = places.first().value.coerceAtLeast(1.0)
            GarageCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                GarageSectionHeader(stringResource(R.string.insights_top_places))
                places.forEach { (place, amount) ->
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(place, style = GarageDesignTheme.typography.label, modifier = Modifier.weight(1f))
                        Amount(amount, size = AmountSize.Small)
                    }
                    GarageProgressBar((amount / max).toFloat(), colors.primary, Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun BuildRow(item: MaintenanceItem, max: Double) {
    val amount = item.cost + item.laborCost
    Column(Modifier.padding(top = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.service, style = GarageDesignTheme.typography.label, modifier = Modifier.weight(1f))
            Amount(amount, size = AmountSize.Small)
        }
        GarageProgressBar((amount / max).toFloat(), RecordType.Upgrade.color, Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun Tile(label: String, caption: String, modifier: Modifier, value: @Composable () -> Unit) {
    GarageCard(modifier) {
        GarageSectionHeader(label)
        Spacer(Modifier.height(4.dp))
        value()
        Text(caption, style = GarageDesignTheme.typography.caption)
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun InsightsPreview() {
    InsightsContent(
        vehicle = Vehicle("v", "BMW", "330e", 2020, "M Sport", 48600, "", color = ""),
        records = listOf(
            MaintenanceRecord("1", "v", "2026-10-12", 0, "Tune Lab", 650.0, items = listOf(MaintenanceItem("Valvetronic catback exhaust", 560.0, 90.0)), type = RecordType.Upgrade),
            MaintenanceRecord("2", "v", "2026-10-12", 48600, "Bavaria Motors", 35.0, items = listOf(MaintenanceItem("Brake fluid", 20.0, 15.0))),
            MaintenanceRecord("3", "v", "2026-09-27", 0, "Sparkle Wash", 5.0, items = listOf(MaintenanceItem("Car wash", 5.0)), type = RecordType.Detailing),
        ),
        period = InsightsPeriod.ThisYear,
        isLoading = false,
        onEvent = {},
        today = LocalDate.of(2026, 10, 16),
    )
}
