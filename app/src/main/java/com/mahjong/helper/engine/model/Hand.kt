package com.mahjong.helper.engine.model

data class Meld(val tiles: List<Tile>, val type: MeldType)

enum class MeldType { PONG, KONG, EXPOSED_KONG } // 碰、暗杠、明杠

data class Hand(
    val freeTiles: List<Tile>,           // 手牌（未组成面子的自由牌）
    val melds: List<Meld> = emptyList(), // 已碰/杠的面子
    val drawnTile: Tile? = null          // 刚摸到的牌
) {
    val size: Int get() = freeTiles.size

    /** 全部手牌（含刚摸的牌）用于向听数计算 */
    val allTiles: List<Tile> get() =
        if (drawnTile != null) freeTiles + drawnTile else freeTiles

    companion object {
        fun fromString(s: String): Hand {
            val tiles = s.split(",").mapNotNull { Tile.parse(it.trim()) }
            return Hand(tiles.sorted())
        }
    }
}
