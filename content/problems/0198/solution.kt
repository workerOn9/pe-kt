package dev.pekt.problems

/**
 * Problem 198: Ambiguous Numbers
 *
 * 思路：歧义数 ⟺ x 恰为某对 Farey 邻居的中点。
 *   在分母上限 d 下出现等距平局 ⟺ x = (a/b + c/e)/2，且 a/b、c/e 在 F_d 中相邻
 *   （Farey 邻居满足 ae - cb = ±1）；中点既约且分母恰为 q = 2be，
 *   于是只需在 Stern-Brocot 树上 DFS 所有邻居对 (h/k, H/K)：
 *   中点 (hK+Hk)/(2kK) 满足 2kK <= 10^8 且中点 < 1/100 即计数。
 *   剪枝：区间左端点 >= 1/100 则其中点及所有后代中点均 >= 1/100，整枝丢弃。
 *
 * 答案：52374425
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve198(): Long {
    val d = 100_000_000L
    // 手工栈，每项 4 个 int：(h, k, H, K)，表示 Farey 邻居区间 [h/k, H/K]
    var stack = IntArray(1 shl 12)
    var sp = 0
    fun push(h: Int, k: Int, H: Int, K: Int) {
        if (sp + 4 > stack.size) stack = stack.copyOf(stack.size * 2)
        stack[sp++] = h; stack[sp++] = k; stack[sp++] = H; stack[sp++] = K
    }
    push(0, 1, 1, 1)
    var count = 0L
    while (sp > 0) {
        val K = stack[--sp]
        val H = stack[--sp]
        val k = stack[--sp]
        val h = stack[--sp]
        val q = 2L * k * K                 // 中点的既约分母
        val p = h.toLong() * K + H.toLong() * k  // 中点分子（恒为奇数，自动既约）
        if (p * 100 < q) count++           // x < 1/100
        val medNum = h + H                 // 中介分数 (h+H)/(k+K)
        val medDen = k + K
        // 先压右半区间，后压左半，出栈时左先处理（顺序不影响计数）
        if (2L * K * medDen <= d && medNum.toLong() * 100 < medDen) {
            push(medNum, medDen, H, K)
        }
        if (2L * k * medDen <= d && h.toLong() * 100 < k) {
            push(h, k, medNum, medDen)
        }
    }
    return count
}

fun main() {
    var best = Long.MAX_VALUE
    var ans = 0L
    repeat(3) {
        val t = System.nanoTime()
        ans = solve198()
        best = minOf(best, System.nanoTime() - t)
    }
    println("answer=$ans bestMs=${"%.1f".format(best / 1e6)}")
}
