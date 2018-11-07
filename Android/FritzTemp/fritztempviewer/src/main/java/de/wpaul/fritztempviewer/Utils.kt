package de.wpaul.fritztempviewer

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun LocalDate.toOldDate(): Date = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())

fun LocalDateTime.toOldDate(): Date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())