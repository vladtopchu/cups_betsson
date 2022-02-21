package com.plug.cupgame.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import com.plug.cupgame.di.ApplicationScope
import com.plug.cupgame.data.local.entities.ProgressEntity
import com.plug.cupgame.data.local.daos.ProgressDao

@Database(entities = [ProgressEntity::class], version = 1, exportSchema = false)
abstract class CupsDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao

    class Callback @Inject constructor(
        private val database: Provider<CupsDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val progressDao = database.get().progressDao()

            applicationScope.launch {
                progressDao.insertProgress(ProgressEntity(0))
            }
        }
    }
}