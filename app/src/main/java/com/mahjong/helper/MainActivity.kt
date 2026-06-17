package com.mahjong.helper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mahjong.helper.engine.model.Hand
import com.mahjong.helper.engine.model.Tile
import com.mahjong.helper.ui.home.HomeScreen
import com.mahjong.helper.ui.input.ManualInputScreen
import com.mahjong.helper.ui.advice.AdviceScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onNewGame = { navController.navigate("input") },
                                onReviews = { navController.navigate("reviews") },
                                onStats = { navController.navigate("stats") }
                            )
                        }
                        composable("input") {
                            ManualInputScreen(
                                onAnalyze = { hand ->
                                    val tiles = hand.allTiles.joinToString(",") { it.id }
                                    navController.navigate("advice/$tiles")
                                }
                            )
                        }
                        composable("advice/{tiles}") { backStackEntry ->
                            val tilesStr = backStackEntry.arguments?.getString("tiles") ?: ""
                            val tiles = tilesStr.split(",").mapNotNull { Tile.parse(it) }
                            AdviceScreen(hand = Hand(tiles.sorted()))
                        }
                        composable("reviews") {
                            // Placeholder - will be implemented in Task 12
                            androidx.compose.material3.Text("历史对局 - 即将实现")
                        }
                        composable("stats") {
                            // Placeholder - will be implemented in Task 13
                            androidx.compose.material3.Text("数据统计 - 即将实现")
                        }
                    }
                }
            }
        }
    }
}
