package com.almudier.mygarage.core.presentation

import androidx.annotation.StringRes
import com.almudier.mygarage.core.domain.model.RecordType

/** Picks the string variant matching this record type. */
@StringRes
fun RecordType.pick(@StringRes maintenance: Int, @StringRes upgrade: Int, @StringRes detailing: Int): Int = when (this) {
    RecordType.Maintenance -> maintenance
    RecordType.Upgrade -> upgrade
    RecordType.Detailing -> detailing
}
