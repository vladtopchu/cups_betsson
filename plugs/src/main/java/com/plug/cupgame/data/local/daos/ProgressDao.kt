package com.plug.cupgame.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.plug.cupgame.data.local.entities.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Query("SELECT * FROM cupgame_progress WHERE id = 1")
    fun getProgress(): Flow<ProgressEntity>

    @Query("UPDATE cupgame_progress SET score = :newScore WHERE id = 1")
    suspend fun updateProgress(newScore: Int)

    @Insert
    suspend fun insertProgress(progressEntity: ProgressEntity)
}