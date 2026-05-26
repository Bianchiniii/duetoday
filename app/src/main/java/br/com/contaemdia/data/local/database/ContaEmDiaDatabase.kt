package br.com.contaemdia.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.contaemdia.data.local.converter.DateConverters
import br.com.contaemdia.data.local.dao.BillDao
import br.com.contaemdia.data.local.entity.BillEntity

@Database(
    entities = [BillEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DateConverters::class)
abstract class ContaEmDiaDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao
}
