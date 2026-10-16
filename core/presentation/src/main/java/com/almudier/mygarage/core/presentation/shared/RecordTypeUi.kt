package com.almudier.mygarage.core.presentation.shared

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CarRepair
import androidx.compose.material.icons.rounded.LocalCarWash
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.almudier.mygarage.core.domain.model.RecordType
import com.almudier.mygarage.core.presentation.R

// The only place a record type maps to its color, icon and labels.

val RecordType.color: Color
    get() = when (this) {
        RecordType.Maintenance -> Color(0xFF4FD8C4)
        RecordType.Upgrade -> Color(0xFFFFB547)
        RecordType.Detailing -> Color(0xFFB39DFF)
    }

val RecordType.icon: ImageVector
    get() = when (this) {
        RecordType.Maintenance -> Icons.Rounded.CarRepair
        RecordType.Upgrade -> Icons.Rounded.Build
        RecordType.Detailing -> Icons.Rounded.LocalCarWash
    }

/** Singular label: "Maintenance", "Upgrade", "Detailing". */
@get:StringRes
val RecordType.label: Int
    get() = when (this) {
        RecordType.Maintenance -> R.string.common_maintenance
        RecordType.Upgrade -> R.string.common_upgrade
        RecordType.Detailing -> R.string.common_detailing
    }

/** Short label for tabs: "Maint.", "Upgrade", "Detailing". */
@get:StringRes
val RecordType.tabLabel: Int
    get() = when (this) {
        RecordType.Maintenance -> R.string.type_tab_maintenance
        RecordType.Upgrade -> R.string.type_tab_upgrade
        RecordType.Detailing -> R.string.type_tab_detailing
    }

/** Plural label for filters and totals: "Maintenance", "Upgrades", "Detailing". */
@get:StringRes
val RecordType.pluralLabel: Int
    get() = when (this) {
        RecordType.Maintenance -> R.string.type_plural_maintenance
        RecordType.Upgrade -> R.string.type_plural_upgrade
        RecordType.Detailing -> R.string.type_plural_detailing
    }
