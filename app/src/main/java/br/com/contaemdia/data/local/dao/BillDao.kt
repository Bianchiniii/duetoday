package br.com.contaemdia.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.contaemdia.data.local.entity.BillEntity
import br.com.contaemdia.domain.model.BillCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC, status ASC")
    fun observeBillsByMonth(startDate: LocalDate, endDate: LocalDate): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE status = 'OPEN' ORDER BY dueDate ASC")
    fun observeOpenBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE status = 'PAID' ORDER BY paidAt DESC")
    fun observePaidBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE category = :category ORDER BY dueDate ASC")
    fun observeBillsByCategory(category: BillCategory): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :id")
    fun observeBillById(id: Long): Flow<BillEntity?>

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Long): BillEntity?

    @Query(
        """
        SELECT COUNT(*) FROM bills
        WHERE title = :title
        AND category = :category
        AND isRecurring = 1
        AND dueDate BETWEEN :startDate AND :endDate
        """
    )
    suspend fun countRecurringInMonth(
        title: String,
        category: BillCategory,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Delete
    suspend fun deleteBill(bill: BillEntity)
}
