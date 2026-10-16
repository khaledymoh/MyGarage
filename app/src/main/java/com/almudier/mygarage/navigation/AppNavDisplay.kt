package com.almudier.mygarage.navigation

import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.almudier.mygarage.authentication.presentation.authNavGraph
import com.almudier.mygarage.core.presentation.Motion
import com.almudier.mygarage.core.presentation.navigation.NavigationCommand
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.garage.garageNavGraph
import com.almudier.mygarage.garage.api.GarageRoute
import com.almudier.mygarage.main.mainNavGraph
import com.almudier.mygarage.maintenance.maintenanceNavGraph
import com.almudier.mygarage.settings.settingsNavGraph
import com.almudier.mygarage.vehicle.vehicleNavGraph
import com.almudier.mygarage.welcome.welcomeNavGraph

@Composable
fun AppNavDisplay(navigator: Navigator, startRoute: NavKey) {
    val backStack = rememberNavBackStack(startRoute)

    LaunchedEffect(navigator) {
        navigator.commands.collect { command ->
            when (command) {
                is NavigationCommand.Navigate -> {
                    command.popUpToInclusive?.let { target ->
                        val index = backStack.indexOfLast { it == target }
                        if (index >= 0) while (backStack.size > index) backStack.removeAt(backStack.lastIndex)
                    }
                    if (!command.singleTop || backStack.lastOrNull() != command.key) backStack.add(command.key)
                }
                NavigationCommand.Back -> if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                is NavigationCommand.ReplaceAll -> { backStack.clear(); backStack.add(command.key) }
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
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
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            mainNavGraph(navigator)
            authNavGraph { navigator.replaceAll(GarageRoute) }
            welcomeNavGraph()
            garageNavGraph()
            vehicleNavGraph()
            maintenanceNavGraph()
            settingsNavGraph()
        },
    )
}
