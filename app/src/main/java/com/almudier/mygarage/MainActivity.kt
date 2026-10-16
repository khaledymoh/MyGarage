package com.almudier.mygarage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import com.almudier.mygarage.core.presentation.shared.LocalCurrency
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.almudier.mygarage.core.presentation.components.GarageSnackbarHost
import com.almudier.mygarage.core.presentation.components.SnackbarManager
import com.almudier.mygarage.core.presentation.navigation.Navigator
import com.almudier.mygarage.core.presentation.theme.GarageTheme
import com.almudier.mygarage.navigation.AppNavDisplay
import com.almudier.mygarage.update.UpdateSheet
import com.almudier.mygarage.widget.GarageWidget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var snackbarManager: SnackbarManager

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is StartDestinationUiState.Loading
        }
        // The garage zooms towards you as the splash fades into the app.
        splashScreen.setOnExitAnimationListener { provider ->
            provider.iconView.animate().scaleX(1.25f).scaleY(1.25f).setDuration(320L).start()
            provider.view.animate().alpha(0f).setDuration(320L).withEndAction { provider.remove() }.start()
        }
        enableEdgeToEdge()
        setContent {
            GarageTheme(darkTheme = true) {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val currency by viewModel.currency.collectAsStateWithLifecycle()
                val update by viewModel.update.collectAsStateWithLifecycle()
                when (val current = state) {
                    StartDestinationUiState.Loading -> Unit
                    is StartDestinationUiState.Ready -> CompositionLocalProvider(LocalCurrency provides currency) {
                        Box(Modifier.fillMaxSize()) {
                        AppNavDisplay(navigator, current.startRoute)
                        GarageSnackbarHost(
                            manager = snackbarManager,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .imePadding(),
                        )
                        update?.let { UpdateSheet(it, onLater = viewModel::postponeUpdate) }
                        }
                    }
                }
            }
        }
    }

    /** Leaving the app is when records or settings may have changed: refresh the home-screen widget. */
    override fun onStop() {
        super.onStop()
        val context = applicationContext
        lifecycleScope.launch { GarageWidget().updateAll(context) }
    }
}
