package br.com.contaemdia.core.common

data class ResultMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
)
