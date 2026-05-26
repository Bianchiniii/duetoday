package br.com.contaemdia.domain.model

enum class BillCategory(val label: String) {
    HOME("Casa"),
    WATER("Água"),
    ELECTRICITY("Luz"),
    INTERNET("Internet"),
    CREDIT_CARD("Cartão"),
    RENT("Aluguel"),
    SUBSCRIPTION("Assinatura"),
    GROCERIES("Mercado"),
    HEALTH("Saúde"),
    EDUCATION("Educação"),
    OTHER("Outros");

    companion object {
        fun fromName(name: String): BillCategory =
            entries.firstOrNull { it.name == name || it.label.equals(name, ignoreCase = true) } ?: OTHER
    }
}
