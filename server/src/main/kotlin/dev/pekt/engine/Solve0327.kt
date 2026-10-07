package dev.pekt.engine

/**
 * PE 327 — Rooms of Doom（厄运之室）
 *
 * 从右往左逐间计算所需卡数 need（最右间为 1）。need≥C 时从左侧房搬运：
 * 每次往返净搬运 C−2 张、消耗 C 张，批量整除算出往返次数，直到 need<C，
 * 再把 need+consumed+1 作为左间的新需求。Σ_{C=3}^{40} M(C,30)。
 *
 * 校验：M(3,6)=123、M(4,6)=23、Σ_{C=3}^{4}M(C,6)=146、Σ_{C=3}^{10}M(C,10)=10382。
 * 最终答案：34315549139516。
 */
internal fun solve0327Impl(): Long {
    fun minCards(cards: Int, rooms: Int): Long {
        var need = 1L
        val transport = cards - 2
        var room = rooms
        while (room > 0) {
            var consumed = 0L
            if (need >= cards) {
                var moves = (need - cards) / transport
                if (need - moves * transport >= cards) moves++
                need -= moves * transport
                consumed += moves * cards
            }
            need = need + consumed + 1
            room--
        }
        return need
    }
    var total = 0L
    for (c in 3..40) total += minCards(c, 30)
    return total
}
