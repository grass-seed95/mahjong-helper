package com.mahjong.helper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mahjong.helper.data.dao.GameRecordDao
import com.mahjong.helper.data.entity.GameRecordEntity
import com.mahjong.helper.data.entity.RoundEntity

@Database(
    entities = [GameRecordEntity::class, RoundEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "mahjong-helper.db")
                .build()
    }
}
