package com.almudier.mygarage.core.presentation

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun formatNumber(value: Int): String = NumberFormat.getIntegerInstance().format(value)

private val displayDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

/** Formats a stored ISO date (yyyy-MM-dd) for display; falls back to the raw value if it can't be parsed. */
fun formatDate(isoDate: String): String =
    runCatching { LocalDate.parse(isoDate).format(displayDateFormatter) }.getOrDefault(isoDate)
