package br.com.contaemdia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.contaemdia.domain.model.BillCategory
import br.com.contaemdia.domain.model.BillStatus
import br.com.contaemdia.domain.model.RecurrenceType
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amountCents: Long,
    val dueDate: LocalDate,
    val category: BillCategory,
    val status: BillStatus,
    val isRecurring: Boolean,
    val recurrenceType: RecurrenceType,
    val notes: String?,
    val paidAt: LocalDate?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
