package br.com.contaemdia.core.money

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

fun Long.toCurrencyText(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    return formatter.format(this / 100.0)
}

fun String.toCentsOrNull(): Long? {
    val value = trim()
        .replace("R$", "", ignoreCase = true)
        .replace(" ", "")
    if (value.isBlank()) return null

    val cents = if (value.contains(",")) {
        val parts = value.split(",", limit = 2)
        val reais = parts.getOrNull(0).orEmpty().filter(Char::isDigit).ifBlank { "0" }
        val centavos = parts.getOrNull(1).orEmpty().filter(Char::isDigit).padEnd(2, '0').take(2)
        "$reais$centavos".toLongOrNull()
    } else {
        value.filter(Char::isDigit).ifBlank { return null }.toLongOrNull()?.let { it * 100 }
    }

    return cents?.takeIf { it > 0 }
}

fun Long.toDecimalInput(): String = "%.2f".format(Locale.forLanguageTag("pt-BR"), this / 100.0)

fun String.toMoneyInput(): String {
    val filtered = filter { it.isDigit() || it == ',' }
    val commaIndex = filtered.indexOf(',')
    if (commaIndex == -1) return filtered

    val reais = filtered.take(commaIndex).filter(Char::isDigit)
    val centavos = filtered.drop(commaIndex + 1).filter(Char::isDigit).take(2)
    return "$reais,$centavos"
}
