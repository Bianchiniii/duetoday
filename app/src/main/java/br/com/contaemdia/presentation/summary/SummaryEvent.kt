package br.com.contaemdia.presentation.summary

sealed interface SummaryEvent {
    data object PreviousMonth : SummaryEvent
    data object NextMonth : SummaryEvent
}
