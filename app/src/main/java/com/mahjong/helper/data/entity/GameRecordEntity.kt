package com.mahjong.helper.data.entity

import androidx.room.*

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "started_at") val startedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "ended_at") val endedAt: Long? = null,
    @ColumnInfo(name = "final_rank") val finalRank: Int? = null,
    @ColumnInfo(name = "is_dealer") val isDealer: Boolean = false,
    @ColumnInfo(name = "total_rounds") val totalRounds: Int = 0
)
