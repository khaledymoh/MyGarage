package com.almudier.mygarage.main

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.almudier.mygarage.R
import com.almudier.mygarage.core.presentation.Motion
import com.almudier.mygarage.core.presentation.components.GarageBottomNavigation
import com.almudier.mygarage.core.presentation.components.GarageIconButton
import com.almudier.mygarage.core.presentation.components.GarageIconButtonSize
import com.almudier.mygarage.core.presentation.components.GarageIconButtonStyle
import com.almudier.mygarage.core.presentation.components.GarageNavItem
import com.almudier.mygarage.core.presentation.components.LocalMainBottomBarInset
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.maintenance.api.MaintenanceFormRoute
import com.almudier.mygarage.settings.api.SettingsRoute
import com.almudier.mygarage.settings.settingsNavGraph
import com.almudier.mygarage.vehicle.api.DashRoute
import com.almudier.mygarage.vehicle.api.InsightsRoute
import com.almudier.mygarage.vehicle.api.LogRoute
import com.almudier.mygarage.vehicle.vehicleNavGraph
import com.almudier.mygarage.core.presentation.R as CoreR

private data class Tab(val route: NavKey, @param:StringRes val label: Int, val icon: ImageVector)

/**
 * The picked car's home. Each tab keeps its own back stack; detail screens are pushed on the
 * app-level stack through [Navigator], above this shell.
 */
@Composable
fun MainScreen(vehicleId: String, navigator: Navigator) {
    val context = LocalContext.current
    val tabs = remember(vehicleId) {
        listOf(
            Tab(DashRoute(vehicleId), R.string.tab_dash, Icons.Rounded.Speed),
            Tab(LogRoute(vehicleId), R.string.tab_log, Icons.AutoMirrored.Rounded.ReceiptLong),
            Tab(InsightsRoute(vehicleId), R.string.tab_insights, Icons.Rounded.Insights),
            Tab(SettingsRoute, CoreR.string.common_settings, Icons.Rounded.Settings),
        )
    }
    val tabBackStacks: List<NavBackStack<NavKey>> = tabs.map { rememberNavBackStack(it.route) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // One NavDisplay over every tab's stack, selected tab last, so inactive tabs keep their state.
    val displayedBackStack by remember {
        derivedStateOf {
            buildList {
                tabBackStacks.forEachIndexed { index, stack -> if (index != selectedTab) addAll(stack) }
                addAll(tabBackStacks[selectedTab])
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = GarageDesignTheme.colors.background,
        bottomBar = {
            GarageBottomNavigation(
                centerAction = {
                    GarageIconButton(
                        icon = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.tab_add_record),
                        onClick = { navigator.navigate(MaintenanceFormRoute(vehicleId)) },
                        style = GarageIconButtonStyle.Filled,
                        size = GarageIconButtonSize.Large,
                    )
                },
            ) {
                tabs.forEachIndexed { index, tab ->
                    GarageNavItem(
                        selected = index == selectedTab,
                        onClick = { selectedTab = index },
                        icon = tab.icon,
                        label = stringResource(tab.label),
                    )
                }
            }
        },
    ) { innerPadding ->
        val activeStack = tabBackStacks[selectedTab]
        CompositionLocalProvider(LocalMainBottomBarInset provides innerPadding.calculateBottomPadding()) {
            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = displayedBackStack,
                transitionSpec = {
                    Motion.scaleIntoContainer(Motion.ScaleTransitionDirection.INWARDS) togetherWith
                        Motion.scaleOutOfContainer(Motion.ScaleTransitionDirection.INWARDS)
                },
                popTransitionSpec = {
                    Motion.scaleIntoContainer(Motion.ScaleTransitionDirection.OUTWARDS) togetherWith
                        Motion.scaleOutOfContainer(Motion.ScaleTransitionDirection.OUTWARDS)
                },
                predictivePopTransitionSpec = {
                    Motion.scaleIntoContainer(Motion.ScaleTransitionDirection.OUTWARDS) togetherWith
                        Motion.scaleOutOfContainer(Motion.ScaleTransitionDirection.OUTWARDS)
                },
                onBack = {
                    when {
                        activeStack.size > 1 -> activeStack.removeAt(activeStack.lastIndex)
                        selectedTab != 0 -> selectedTab = 0
                        else -> (context as? Activity)?.finish()
                    }
                },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    vehicleNavGraph()
                    settingsNavGraph()
                },
            )
        }
    }
}
