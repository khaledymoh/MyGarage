package com.almudier.mygarage.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.almudier.mygarage.core.presentation.GarageThemeWrapper
import com.almudier.mygarage.core.presentation.R
import com.almudier.mygarage.core.presentation.ThemePreviews
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme

/**
 * Full-screen intro layout: the app logo on a soft glow, eyebrow, title, supporting text, one action pinned
 * at the bottom and a footer line. Used by sign-in and welcome.
 */
@Composable
fun GarageHeroLayout(
    eyebrow: String,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    footer: String? = null,
    action: @Composable () -> Unit,
) {
    val colors = GarageDesignTheme.colors
    val glow = colors.primary.copy(alpha = 0.28f)
    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = GarageDesignTheme.spacing.nr, vertical = GarageDesignTheme.spacing.lr),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(glow, Color.Transparent), center = Offset(size.width / 2, size.height / 2), radius = size.minDimension * 0.75f),
                        radius = size.minDimension * 0.75f,
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(R.drawable.garage_logo), contentDescription = null, modifier = Modifier.size(width = 204.dp, height = 170.dp))
        }
        Spacer(Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(eyebrow.uppercase(), style = GarageDesignTheme.typography.eyebrow.copy(color = colors.primary))
            Text(title, style = GarageDesignTheme.typography.display)
            Text(text, style = GarageDesignTheme.typography.bodyLarge.copy(color = colors.dim))
        }
        Spacer(Modifier.height(GarageDesignTheme.spacing.xl))
        action()
        if (footer != null) {
            Text(
                footer,
                style = GarageDesignTheme.typography.caption.copy(color = colors.mute),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = GarageDesignTheme.spacing.sm),
            )
        }
    }
}

@ThemePreviews
@PreviewWrapper(wrapper = GarageThemeWrapper::class)
@Composable
private fun GarageHeroLayoutPreview() {
    GarageHeroLayout(
        eyebrow = "Sign in to continue",
        title = "Your garage, on every device.",
        text = "Sign in with Google to keep your vehicles, service history and receipts backed up and in sync.",
        footer = "We only use your Google account to sync your garage.",
    ) {
        GarageButton("Continue with Google", onClick = {}, style = GarageButtonStyle.Google, modifier = Modifier.fillMaxWidth())
    }
}
