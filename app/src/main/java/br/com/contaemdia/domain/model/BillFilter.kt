package br.com.contaemdia.domain.model

enum class BillStatusFilter(val label: String) {
    ALL("Todos"),
    OPEN("Em aberto"),
    PAID("Pagos"),
    OVERDUE("Atrasados"),
}

enum class BillSortOption(val label: String) {
    DUE_DATE_ASC("Vencimento"),
    AMOUNT_DESC("Maior valor"),
    AMOUNT_ASC("Menor valor"),
}
