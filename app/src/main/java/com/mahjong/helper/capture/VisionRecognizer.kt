package com.mahjong.helper.capture

import android.graphics.Bitmap
import android.graphics.Rect
import com.mahjong.helper.engine.model.GameState
import com.mahjong.helper.engine.model.Hand
import com.mahjong.helper.engine.model.Tile

/**
 * Configuration defining where to find each element on screen.
 * Stored per mini-program, set during calibration.
 */
data class ScreenConfig(
    val myHandRect: Rect,          // 我的手牌区域
    val myMeldsRect: Rect,         // 我的碰/杠区
    val discardsRect: List<Rect>,  // 牌河弃牌区（我+3家）
    val drawnTileRect: Rect        // 刚摸的牌位置
)

class VisionRecognizer(
    private val config: ScreenConfig,
    private val templates: Map<String, Bitmap>   // "1m" → tile template image
) {
    /**
     * Analyze a screenshot and extract the game state.
     * Returns null if recognition confidence is too low.
     */
    fun recognize(screenshot: Bitmap): GameState? {
        // Crop each region and run template matching
        val myHandTiles = recognizeRegion(screenshot, config.myHandRect)
        if (myHandTiles.size < 10) return null

        val drawnTile = recognizeRegion(screenshot, config.drawnTileRect).firstOrNull()

        val hand = Hand(
            freeTiles = if (drawnTile != null) {
                myHandTiles.filter { it != drawnTile }.sorted()
            } else {
                myHandTiles.sorted()
            },
            drawnTile = drawnTile
        )

        val players = config.discardsRect.map { rect ->
            val discards = recognizeRegion(screenshot, rect)
            com.mahjong.helper.engine.model.PlayerView(discards = discards)
        }

        return GameState(
            myHand = hand,
            players = players,
            roundWind = estimateRoundWind(players.flatMap { it.discards })
        )
    }

    private fun recognizeRegion(screenshot: Bitmap, region: Rect): List<Tile> {
        val cropped = Bitmap.createBitmap(screenshot, region.left, region.top,
            region.width(), region.height())
        val results = mutableListOf<Pair<Float, Tile>>()

        for ((tileId, template) in templates) {
            val confidence = templateMatch(cropped, template)
            if (confidence > 0.7) {
                Tile.parse(tileId)?.let { results.add(confidence to it) }
            }
        }
        return results.sortedByDescending { it.first }.map { it.second }
    }

    /**
     * Simple normalized cross-correlation template matching.
     * For MVP, returns the max correlation score in the cropped region.
     */
    private fun templateMatch(source: Bitmap, template: Bitmap): Float {
        if (source.width < template.width || source.height < template.height) return 0f

        var bestMatch = 0f
        val step = 2  // stride for performance

        for (y in 0..(source.height - template.height) step step) {
            for (x in 0..(source.width - template.width) step step) {
                var score = 0f
                for (ty in 0 until template.height step step) {
                    for (tx in 0 until template.width step step) {
                        val sp = source.getPixel(x + tx, y + ty)
                        val tp = template.getPixel(tx, ty)
                        // Compare RGB channels
                        val dr = ((sp shr 16) and 0xFF) - ((tp shr 16) and 0xFF)
                        val dg = ((sp shr 8) and 0xFF) - ((tp shr 8) and 0xFF)
                        val db = (sp and 0xFF) - (tp and 0xFF)
                        if (dr * dr + dg * dg + db * db < 10000) score++
                    }
                }
                val total = ((template.width / step) * (template.height / step)).toFloat()
                val match = score / total
                if (match > bestMatch) bestMatch = match
            }
        }
        return bestMatch
    }

    private fun estimateRoundWind(allDiscards: List<Tile>): Int = allDiscards.size / 4
}
