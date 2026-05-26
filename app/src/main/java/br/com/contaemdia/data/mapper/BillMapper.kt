package br.com.contaemdia.data.mapper

import br.com.contaemdia.data.local.entity.BillEntity
import br.com.contaemdia.domain.model.Bill

fun BillEntity.toDomain(): Bill = Bill(
    id = id,
    title = title,
    amountCents = amountCents,
    dueDate = dueDate,
    category = category,
    status = status,
    isRecurring = isRecurring,
    recurrenceType = recurrenceType,
    notes = notes,
    paidAt = paidAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Bill.toEntity(): BillEntity = BillEntity(
    id = id,
    title = title,
    amountCents = amountCents,
    dueDate = dueDate,
    category = category,
    status = status,
    isRecurring = isRecurring,
    recurrenceType = recurrenceType,
    notes = notes,
    paidAt = paidAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
