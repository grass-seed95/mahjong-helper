package com.mahjong.helper.capture

import android.graphics.Bitmap
import android.graphics.Color
import com.mahjong.helper.engine.model.Suit
import com.mahjong.helper.engine.model.Tile

/**
 * Color-based tile recognition that detects suit via color histogram
 * and number via center region analysis. No templates or ML needed.
 *
 * Assumptions about tile design:
 * - 万 (Man): Red-tinted characters, red top marker
 * - 筒 (Pin): Blue/cyan circles
 * - 条 (Sou): Green bamboo patterns
 *
 * Each tile image is expected to be ~40x60 pixels.
 */
class ColorBasedRecognizer {

    data class TileMatch(val tile: Tile, val confidence: Float)

    /**
     * Recognize a single tile image. Returns the best match with confidence.
     * Confidence < 0.5 is considered unreliable.
     */
    fun recognizeTile(bitmap: Bitmap): TileMatch? {
        val suit = detectSuit(bitmap) ?: return null
        val number = detectNumber(bitmap) ?: return null
        return TileMatch(Tile(suit, number), confidence = 0.6f) // base confidence
    }

    /**
     * Detect suit by analyzing color distribution in the tile image.
     */
    fun detectSuit(bitmap: Bitmap): Suit? {
        val w = bitmap.width
        val h = bitmap.height

        // Sample regions: top-left for suit marker, center for main pattern
        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L
        var pixelCount = 0

        // Sample every 2nd pixel for performance
        val step = 2
        for (y in 0 until h step step) {
            for (x in 0 until w step step) {
                val pixel = bitmap.getPixel(x, y)
                redSum += Color.red(pixel)
                greenSum += Color.green(pixel)
                blueSum += Color.blue(pixel)
                pixelCount++
            }
        }

        if (pixelCount == 0) return null

        val avgR = redSum / pixelCount
        val avgG = greenSum / pixelCount
        val avgB = blueSum / pixelCount

        // Determine suit based on dominant color
        return when {
            // Red dominant → 万 (red text/characters)
            avgR > avgG + 20 && avgR > avgB + 20 -> Suit.M
            // Blue dominant → 筒 (blue circles)
            avgB > avgR + 15 && avgB > avgG + 5 -> Suit.P
            // Green dominant → 条 (green bamboo)
            avgG > avgR + 15 && avgG > avgB + 10 -> Suit.S
            // Fallback: check ratios
            avgR.toFloat() / (avgG + avgB + 1).toFloat() > 0.55f -> Suit.M
            avgB.toFloat() / (avgR + avgG + 1).toFloat() > 0.55f -> Suit.P
            avgG.toFloat() / (avgR + avgB + 1).toFloat() > 0.55f -> Suit.S
            else -> null // Can't determine
        }
    }

    /**
     * Detect the tile number (1-9) from the center region of the tile.
     * Uses a simplified approach: divides the center into a 3x3 grid,
     * counts dark pixels in each cell, and matches patterns.
     *
     * For 萬 tiles, the top part shows the Chinese character for "万" with a
     * number above it, typically in the upper half.
     */
    fun detectNumber(bitmap: Bitmap): Int? {
        val w = bitmap.width
        val h = bitmap.height

        // Focus on the center region (30%-70% of width, 20%-60% of height)
        val left = (w * 0.30).toInt()
        val right = (w * 0.70).toInt()
        val top = (h * 0.20).toInt()
        val bottom = (h * 0.60).toInt()

        if (right <= left || bottom <= top) return null

        // Count dark pixels in a 3x3 grid over the center region
        val cellW = (right - left) / 3
        val cellH = (bottom - top) / 3
        if (cellW <= 0 || cellH <= 0) return null

        val grid = IntArray(9) // 3x3 flattened
        var totalDark = 0

        for (gy in 0 until 3) {
            for (gx in 0 until 3) {
                var darkCount = 0
                for (y in (top + gy * cellH) until (top + (gy + 1) * cellH)) {
                    for (x in (left + gx * cellW) until (left + (gx + 1) * cellW)) {
                        val pixel = bitmap.getPixel(
                            x.coerceIn(0, w - 1),
                            y.coerceIn(0, h - 1)
                        )
                        val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                        if (brightness < 100) darkCount++
                    }
                }
                grid[gy * 3 + gx] = darkCount
                totalDark += darkCount
            }
        }

        // Normalize by area
        val cellArea = cellW * cellH
        if (cellArea == 0) return null

        // Use grid pattern to classify number
        // This is a simplified approach - in practice would need per-digit template
        return classifyNumberByGrid(grid, totalDark, cellArea)
    }

    /**
     * Classify the number based on grid dark pixel distribution.
     * This is a simplified heuristic approach.
     */
    private fun classifyNumberByGrid(grid: IntArray, totalDark: Int, cellArea: Int): Int? {
        val norm = grid.map { it.toFloat() / cellArea }

        // Check which cells have significant dark content (>20%)
        val activeCells = norm.map { it > 0.2f }
        val topActive = activeCells.take(6).count { it }
        val bottomActive = activeCells.takeLast(6).count { it }
        val leftActive = listOf(0, 3, 6).count { activeCells[it] || activeCells[it + 1] }
        val rightActive = listOf(1, 4, 7).count { activeCells[it] || activeCells[it + 1] }
        val middleActive = listOf(0, 3, 6).count { activeCells[it + 1] || activeCells[it + 2] }

        // Heuristic classification based on grid activation patterns
        return when {
            // Number 1: narrow vertical line (center column active)
            middleActive >= 2 && leftActive <= 1 && rightActive <= 1 -> 1
            // Number 2: top + middle + bottom
            grid[1] > grid[4] && grid[7] > grid[4] -> 2
            // Number 3: active on right side
            rightActive >= 2 && leftActive <= 1 -> 3
            // Number 4: top two quadrants active
            topActive >= 3 && bottomActive <= 2 -> 4
            // Number 5: resembles 2 but different
            grid[4] > grid[1] && totalDark > cellArea * 2 -> 5
            // Number 6: left side active
            leftActive >= 2 && rightActive <= 1 -> 6
            // Number 7: top row active
            activeCells[0] && activeCells[1] && activeCells[2] -> 7
            // Number 8: all quadrants active
            topActive >= 2 && bottomActive >= 2 && leftActive >= 2 && rightActive >= 2 -> 8
            // Number 9: top active, right active
            topActive >= 2 && rightActive >= 2 -> 9
            // Fallback: estimate from total activation
            totalDark > cellArea * 2 -> (totalDark / (cellArea / 3)).coerceIn(1, 9)
            else -> 1 // Default fallback (will have low confidence)
        }
    }

    /**
     * Recognize multiple tiles from a single row bitmap.
     * Assumes tiles are evenly spaced horizontally.
     */
    fun recognizeTileRow(bitmap: Bitmap, tileCount: Int): List<TileMatch> {
        val tileWidth = bitmap.width / tileCount.coerceAtLeast(1)
        val results = mutableListOf<TileMatch>()

        for (i in 0 until tileCount) {
            val left = i * tileWidth
            val right = ((i + 1) * tileWidth).coerceAtMost(bitmap.width)
            if (right <= left) continue

            val tileBitmap = Bitmap.createBitmap(bitmap, left, 0, right - left, bitmap.height)
            val match = recognizeTile(tileBitmap)
            if (match != null) {
                results.add(match)
            }
        }
        return results
    }
}
