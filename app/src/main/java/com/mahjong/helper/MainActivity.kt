package com.mahjong.helper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mahjong.helper.data.dao.GameRecordDao
import com.mahjong.helper.engine.model.Hand
import com.mahjong.helper.engine.model.Tile
import com.mahjong.helper.ui.capture.CaptureScreen
import com.mahjong.helper.ui.home.HomeScreen
import com.mahjong.helper.ui.input.ManualInputScreen
import com.mahjong.helper.ui.advice.AdviceScreen
import com.mahjong.helper.ui.review.ReviewListScreen
import com.mahjong.helper.ui.review.ReviewDetailScreen
import com.mahjong.helper.ui.stats.StatsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val app = LocalContext.current.applicationContext as MahjongApp
                    val dao: GameRecordDao = remember { app.database.gameRecordDao() }

                    NavHost(navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onNewGame = { navController.navigate("input") },
                                onLiveMonitor = { navController.navigate("capture") },
                                onReviews = { navController.navigate("reviews") },
                                onStats = { navController.navigate("stats") }
                            )
                        }
                        composable("capture") {
                            CaptureScreen()
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
                            ReviewListScreen(
                                dao = dao,
                                onGameClick = { gameId -> navController.navigate("review/$gameId") }
                            )
                        }
                        composable("review/{gameId}") { backStackEntry ->
                            val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull() ?: 0L
                            ReviewDetailScreen(dao = dao, gameId = gameId)
                        }
                        composable("stats") {
                            StatsScreen(dao = dao)
                        }
                    }
                }
            }
        }
    }
}
