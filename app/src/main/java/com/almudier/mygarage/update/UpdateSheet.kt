package com.almudier.mygarage.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.almudier.mygarage.R
import com.almudier.mygarage.core.presentation.components.GarageBottomSheet
import com.almudier.mygarage.core.presentation.components.GarageButton
import com.almudier.mygarage.core.presentation.components.GarageButtonStyle
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import com.almudier.mygarage.core.presentation.R as PresentationR

/**
 * Offers a newer release. A required update can't be dismissed (no swipe, back or tap outside);
 * an optional one has "Later".
 */
@Composable
fun UpdateSheet(update: AppUpdate, onLater: () -> Unit) {
    val colors = GarageDesignTheme.colors
    val context = LocalContext.current
    GarageBottomSheet(onDismissRequest = onLater, dismissible = !update.required) {
        Column(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painterResource(PresentationR.drawable.garage_logo),
                contentDescription = null,
                modifier = Modifier.size(width = 96.dp, height = 80.dp),
            )
            Text(
                stringResource(if (update.required) R.string.update_required_title else R.string.update_available_title),
                style = GarageDesignTheme.typography.h2,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(
                    if (update.required) R.string.update_required_text else R.string.update_available_text,
                    update.versionName,
                ),
                style = GarageDesignTheme.typography.body.copy(color = colors.dim),
                textAlign = TextAlign.Center,
            )
        }
        Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GarageButton(
                stringResource(R.string.update_action),
                onClick = { openInChrome(context, update.url) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (!update.required) {
                GarageButton(
                    stringResource(R.string.update_later),
                    onClick = onLater,
                    style = GarageButtonStyle.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Opens [url] in Chrome itself (not a tab inside another app or a browser chooser), so the APK
 * download and its install prompt happen in a full browser. Falls back to any browser.
 */
private fun openInChrome(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(Intent(intent).setPackage(CHROME_PACKAGE))
    } catch (_: ActivityNotFoundException) {
        runCatching { context.startActivity(intent) }
    }
}

private const val CHROME_PACKAGE = "com.android.chrome"
