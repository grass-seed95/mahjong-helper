package com.mahjong.helper.engine.model

/**
 * Suit: m=万(man), p=筒(pin), s=条(sou)
 * Number: 1-9
 * 108 unique tiles = 3 suits × 9 numbers × 4 copies each
 */
enum class Suit(val display: String) { M("万"), P("筒"), S("条") }

data class Tile(val suit: Suit, val number: Int) : Comparable<Tile> {
    companion object {
        fun all(): List<Tile> = Suit.entries.flatMap { suit ->
            (1..9).flatMap { n -> List(4) { Tile(suit, n) } }
        }

        fun allUnique(): List<Tile> = Suit.entries.flatMap { suit ->
            (1..9).map { n -> Tile(suit, n) }
        }

        fun parse(s: String): Tile? {
            if (s.length < 2) return null
            val suit = when (s.last()) {
                'm' -> Suit.M
                'p' -> Suit.P
                's' -> Suit.S
                else -> return null
            }
            val num = s.dropLast(1).toIntOrNull() ?: return null
            if (num !in 1..9) return null
            return Tile(suit, num)
        }
    }

    val id: String get() = "$number${suit.name.lowercase()}"

    /** 1m=1, 9m=9, 1p=10, ..., 9s=27 */
    val ordinal: Int get() = suit.ordinal * 9 + (number - 1)

    /** 是否为幺九牌 (1或9) */
    val isTerminal: Boolean get() = number == 1 || number == 9

    /** 是否为中张 (2-8) */
    val isMiddle: Boolean get() = number in 2..8

    override fun compareTo(other: Tile): Int = this.ordinal - other.ordinal
    override fun toString(): String = "$number${suit.display}"
}
