package com.framextv.v2.ui.screens.detail

import android.text.format.DateFormat
import com.framextv.v2.core.util.parseEpisodeReleaseLocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatReleaseDate(isoDate: String): String {
    val locale = Locale.getDefault()
    val releaseDate = parseEpisodeReleaseLocalDate(isoDate) ?: return ""
    val pattern = DateFormat.getBestDateTimePattern(locale, "dMMMMy")
    return DateTimeFormatter.ofPattern(pattern, locale).format(releaseDate)
}
