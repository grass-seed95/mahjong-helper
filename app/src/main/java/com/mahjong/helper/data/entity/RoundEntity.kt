package com.mahjong.helper.data.entity

import androidx.room.*

@Entity(
    tableName = "rounds",
    foreignKeys = [ForeignKey(
        entity = GameRecordEntity::class,
        parentColumns = ["id"],
        childColumns = ["game_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "game_id") val gameId: Long,
    @ColumnInfo(name = "round_number") val roundNumber: Int,
    @ColumnInfo(name = "hand_tiles") val handTiles: String,
    @ColumnInfo(name = "drawn_tile") val drawnTile: String?,
    @ColumnInfo(name = "suggested_discard") val suggestedDiscard: String?,
    @ColumnInfo(name = "actual_discard") val actualDiscard: String?,
    @ColumnInfo(name = "was_optimal") val wasOptimal: Boolean?,
    @ColumnInfo(name = "shanten_before") val shantenBefore: Int,
    @ColumnInfo(name = "shanten_after") val shantenAfter: Int?,
    @ColumnInfo(name = "acceptance_count") val acceptanceCount: Int?,
    @ColumnInfo(name = "safety_score") val safetyScore: Int?,
    @ColumnInfo(name = "result") val result: String?
)
