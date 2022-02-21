package com.plug.cupgame.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cupgame_progress")
data class ProgressEntity(
    val score: Int,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
