package com.almudier.mygarage.core.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.core.presentation.theme.GarageTheme

/**
 * A wrapper for Compose Previews that applies the [GarageTheme] and a background color.
 */
@Composable
fun PreviewWrapper(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    GarageTheme(darkTheme = darkTheme) {
        Box(modifier = Modifier.background(GarageDesignTheme.colors.background)) {
            content()
        }
    }
}

/**
 * Alias for [PreviewWrapper] to maintain backward compatibility with previous naming.
 */
@Composable
fun ThemeWrapper(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) = PreviewWrapper(darkTheme, content)

/**
 * A [PreviewParameterProvider] that provides both Light and Dark mode options.
 */
class DarkThemePreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

/**
 * A [PreviewWrapperProvider] that applies the [GarageTheme] and a background color.
 * Use this with the [PreviewWrapper] annotation.
 *
 * Example:
 * ```
 * @Preview
 * @PreviewWrapper(wrapper = GarageThemeWrapper::class)
 * @Composable
 * fun MyPreview() { ... }
 * ```
 */
class GarageThemeWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        GarageTheme {
            Box(modifier = Modifier.background(GarageDesignTheme.colors.background)) {
                content()
            }
        }
    }
}

/**
 * MultiPreview annotation that generates both Light and Dark mode previews.
 */
@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews
