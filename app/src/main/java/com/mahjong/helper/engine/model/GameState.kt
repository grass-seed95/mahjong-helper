package com.mahjong.helper.engine.model

data class PlayerView(
    val discards: List<Tile> = emptyList(),  // 弃牌（按顺序）
    val exposedMelds: List<Meld> = emptyList(),
    val riichiDeclared: Boolean = false
)

data class GameState(
    val myHand: Hand,
    val players: List<PlayerView> = listOf(PlayerView(), PlayerView(), PlayerView()),
    val roundWind: Int = 0,                     // 巡目 (0-based)
    val isDealer: Boolean = false,
    val remainingTiles: Int = 70               // 剩余牌墙估算
) {
    /** 所有已见的牌（我的手牌 + 所有弃牌 + 所有鸣牌） */
    fun allVisibleTiles(): List<Tile> {
        val seen = mutableListOf<Tile>()
        seen.addAll(myHand.allTiles)
        for (meld in myHand.melds) seen.addAll(meld.tiles)
        for (p in players) {
            seen.addAll(p.discards)
            for (meld in p.exposedMelds) seen.addAll(meld.tiles)
        }
        return seen
    }

    /** 对某个对手，已经打过的牌集合 */
    fun playerDiscardedTiles(playerIndex: Int): Set<Tile> =
        players[playerIndex].discards.toSet()
}
