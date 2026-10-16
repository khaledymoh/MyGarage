package com.almudier.mygarage.authentication.presentation

import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.almudier.mygarage.authentication.R as ResR
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.components.GarageButton
import com.almudier.mygarage.core.presentation.components.GarageButtonStyle
import com.almudier.mygarage.core.presentation.components.GarageHeroLayout
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                AuthContract.Effect.NavigateToMain -> onAuthenticated()
            }
        }
    }

    AuthScreenContent(
        isLoading = state.isLoading,
        onContinueWithGoogle = {
            activity?.let { viewModel.onEvent(AuthContract.Intent.ContinueWithGoogleClicked(it)) }
        },
    )
}

@Composable
fun AuthScreenContent(
    isLoading: Boolean,
    onContinueWithGoogle: () -> Unit,
) {
    GarageHeroLayout(
        eyebrow = stringResource(ResR.string.auth_eyebrow),
        title = stringResource(ResR.string.auth_title),
        text = stringResource(ResR.string.auth_subtitle),
        footer = stringResource(ResR.string.auth_footer),
    ) {
        GarageButton(
            text = stringResource(ResR.string.auth_google_button),
            onClick = onContinueWithGoogle,
            style = GarageButtonStyle.Google,
            loading = isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun AuthScreenPreview() {
    AuthScreenContent(
        isLoading = false,
        onContinueWithGoogle = {},
    )
}
