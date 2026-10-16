package com.almudier.mygarage.core.presentation.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.almudier.mygarage.core.presentation.theme.GarageDesignTheme
import java.text.NumberFormat

/** The user's currency code, provided at the app root from settings. */
val LocalCurrency = staticCompositionLocalOf { "JOD" }

/** Grouped number, decimals only when the value isn't whole (max 2): 1,480 · 12.5. */
fun formatAmount(value: Double): String {
    val format = NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = if (value % 1.0 == 0.0) 0 else 2
        minimumFractionDigits = 0
    }
    return format.format(value)
}

/** "35 JOD" as plain text, for captions and meta lines. */
fun formatMoney(value: Double, currency: String): String = "${formatAmount(value)} $currency"

enum class AmountSize { Hero, Large, Medium, Small }

/**
 * Money with its currency: number + space + code. The number is mono and bold; the code is
 * mono, regular and dim, at about 42% of the number size (never under 10sp).
 */
@Composable
fun Amount(
    value: Double,
    modifier: Modifier = Modifier,
    size: AmountSize = AmountSize.Medium,
    currency: String = LocalCurrency.current,
    color: Color = Color.Unspecified,
) {
    val typography = GarageDesignTheme.typography
    val colors = GarageDesignTheme.colors
    val numberStyle: TextStyle = when (size) {
        AmountSize.Hero -> typography.numberHero
        AmountSize.Large -> typography.number.copy(fontSize = 22.sp)
        AmountSize.Medium -> typography.mono.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text)
        AmountSize.Small -> typography.mono.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.text)
    }
    val currencySize = (numberStyle.fontSize.value * 0.42f).coerceAtLeast(10f).sp
    val numberColor = if (color != Color.Unspecified) color else if (value == 0.0) colors.mute else numberStyle.color
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Text(formatAmount(value), style = numberStyle.copy(color = numberColor), modifier = Modifier.alignBy(FirstBaseline))
        Text(
            " $currency",
            style = typography.mono.copy(fontSize = currencySize, fontWeight = FontWeight.Medium, color = colors.dim),
            modifier = Modifier.alignBy(FirstBaseline),
        )
    }
}
