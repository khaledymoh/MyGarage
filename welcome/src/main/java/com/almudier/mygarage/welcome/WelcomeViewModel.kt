package com.almudier.mygarage.welcome

import com.almudier.mygarage.core.presentation.mvi.BaseMviViewModel
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.vehicle.api.VehicleFormRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(private val navigator: Navigator) :
    BaseMviViewModel<WelcomeContract.Intent, WelcomeContract.State, WelcomeContract.Effect>(WelcomeContract.State) {

    override fun onEvent(intent: WelcomeContract.Intent) {
        navigator.navigate(VehicleFormRoute())
    }
}
