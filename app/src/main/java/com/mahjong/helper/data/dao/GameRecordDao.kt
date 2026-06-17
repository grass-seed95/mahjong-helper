package com.mahjong.helper.data.dao

import androidx.room.*
import com.mahjong.helper.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Insert
    suspend fun insertGame(game: GameRecordEntity): Long

    @Update
    suspend fun updateGame(game: GameRecordEntity)

    @Insert
    suspend fun insertRound(round: RoundEntity)

    @Query("SELECT * FROM game_records ORDER BY started_at DESC")
    fun allGames(): Flow<List<GameRecordEntity>>

    @Query("SELECT * FROM rounds WHERE game_id = :gameId ORDER BY round_number ASC")
    suspend fun roundsForGame(gameId: Long): List<RoundEntity>

    @Query("SELECT * FROM game_records WHERE id = :id")
    suspend fun gameById(id: Long): GameRecordEntity?

    @Query("SELECT COUNT(*) FROM game_records WHERE final_rank IS NOT NULL")
    suspend fun completedGamesCount(): Int

    @Query("SELECT final_rank FROM game_records WHERE final_rank IS NOT NULL")
    suspend fun allRanks(): List<Int>

    @Query("SELECT COUNT(*) FROM rounds WHERE was_optimal = 1")
    suspend fun optimalDecisionsCount(): Int

    @Query("SELECT COUNT(*) FROM rounds WHERE was_optimal = 0")
    suspend fun suboptimalDecisionsCount(): Int
}
