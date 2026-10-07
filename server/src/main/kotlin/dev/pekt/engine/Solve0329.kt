package dev.pekt.engine

/**
 * PE 329 — Prime Frog（素数青蛙）
 *
 * p_t[pos] 为「听到前 t 声且位于 pos」的联合概率。发声后按 1/2 左右移动（两端反弹）。
 * 公共分母 D = 500·3¹⁵·2¹⁴，整型分子递推后约分，返回最简分数 "p/q"。
 *
 * 最终答案：199740353/29386561536000。
 */
internal fun solve0329Impl(): String {
    val squares = 500
    val seq = "PPPPNNPPPNPPNPN"

    val isPrime = BooleanArray(squares + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= squares) {
        if (isPrime[i]) {
            var j = i.toLong() * i
            while (j <= squares) { isPrime[j.toInt()] = false; j += i }
        }
        i++
    }

    var den = 500L
    repeat(seq.length) { den *= 3 }
    repeat(seq.length - 1) { den *= 2 }
    val start = den / 500

    var p = LongArray(squares + 1) { start }
    for (idx in seq.indices) {
        val c = seq[idx]
        val np = LongArray(squares + 1)
        for (pos in 1..squares) {
            if (p[pos] == 0L) continue
            val prime = isPrime[pos]
            val factor = if (c == 'P') { if (prime) 2L else 1L } else { if (prime) 1L else 2L }
            np[pos] = p[pos] * factor / 3
        }
        if (idx < seq.length - 1) {
            val q = LongArray(squares + 1)
            for (pos in 1..squares) {
                if (np[pos] == 0L) continue
                val left = if (pos > 1) pos - 1 else 2
                val right = if (pos < squares) pos + 1 else squares - 1
                q[left] += np[pos] / 2
                q[right] += np[pos] / 2
            }
            p = q
        } else {
            p = np
        }
    }
    var num = 0L
    for (pos in 1..squares) num += p[pos]
    var a = num
    var b = den
    while (b != 0L) { val t = a % b; a = b; b = t }
    val g = a
    return "${num / g}/${den / g}"
}
