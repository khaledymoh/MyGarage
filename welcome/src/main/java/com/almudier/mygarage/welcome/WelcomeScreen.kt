package com.almudier.mygarage.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.almudier.mygarage.welcome.R as ResR
import com.almudier.mygarage.core.presentation.components.GarageButton
import com.almudier.mygarage.core.presentation.components.GarageHeroLayout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel = hiltViewModel()) {
    GarageHeroLayout(
        eyebrow = stringResource(ResR.string.welcome_eyebrow),
        title = stringResource(ResR.string.welcome_title),
        text = stringResource(ResR.string.welcome_subtitle),
        footer = stringResource(ResR.string.welcome_footer),
    ) {
        GarageButton(
            text = stringResource(ResR.string.welcome_add_first_car).trim().removePrefix("＋").trim(),
            onClick = { viewModel.onEvent(WelcomeContract.Intent.AddFirstVehicle) },
            leadingIcon = Icons.Rounded.Add,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
