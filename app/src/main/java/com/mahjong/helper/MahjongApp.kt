package com.mahjong.helper

import android.app.Application

class MahjongApp : Application() {
    lateinit var database: com.mahjong.helper.data.AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = com.mahjong.helper.data.AppDatabase.create(this)
    }
}
