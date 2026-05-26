package br.com.contaemdia.core.date

import java.time.LocalDate
import java.time.YearMonth
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val brazilianLocale: Locale = Locale.forLanguageTag("pt-BR")
private val brazilianDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", brazilianLocale)
private val monthFormatter = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", brazilianLocale)

fun LocalDate.toBrazilianDate(): String = format(brazilianDateFormatter)

fun YearMonth.toBrazilianMonth(): String =
    atDay(1).format(monthFormatter).replaceFirstChar { it.titlecase(brazilianLocale) }

fun LocalDate.toIsoDateInput(): String = toString()

fun String.parseIsoDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

fun String.parseBrazilianDateOrNull(): LocalDate? =
    runCatching { LocalDate.parse(this, brazilianDateFormatter) }.getOrNull()

fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.toLocalDateFromUtcMillis(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
