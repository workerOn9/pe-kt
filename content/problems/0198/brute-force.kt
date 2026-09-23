package dev.pekt.problems

/** 欧几里得 gcd（standalone 编译不依赖 kotlin.math.gcd）。 */
private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

/**
 * PE 198 暴力解：按定义逐个检查。
 * 对每个既约分数 x = p/q（0 < x < 1/100，q <= dMax），扫描分母上限
 * d = 1..q-1，维护「分母 <= d 中距 x 最近的下侧/上侧分数」，
 * 两者距离相等（等距平局）即为歧义数。O(q^2) 级别，dMax 只能取小值。
 */
fun bruteForce198(dMax: Int = 2000): Int {
    var count = 0
    for (q in 2..dMax) {
        val pMax = (q - 1) / 100            // p/q < 1/100
        for (p in 1..pMax) {
            if (gcd(p, q) != 1) continue
            var bestBelowNum = -1L; var bestBelowV = 1L
            var bestAboveNum = -1L; var bestAboveV = 1L
            var ambiguous = false
            for (v in 1 until q) {
                val pv = p.toLong() * v
                val uB = (pv - 1) / q        // 最大 u/v < p/q
                val dB = pv - q * uB         // 距离 = dB/(q*v)
                val uA = pv / q + 1          // 最小 u/v > p/q
                val dA = q * uA - pv         // 距离 = dA/(q*v)
                if (bestBelowNum < 0 || dB * bestBelowV < bestBelowNum * v) {
                    bestBelowNum = dB; bestBelowV = v.toLong()
                }
                if (bestAboveNum < 0 || dA * bestAboveV < bestAboveNum * v) {
                    bestAboveNum = dA; bestAboveV = v.toLong()
                }
                // dB/(q*vb) == dA/(q*va)  ⟺  dB*va == dA*vb
                if (bestBelowNum * bestAboveV == bestAboveNum * bestBelowV) {
                    ambiguous = true
                    break
                }
            }
            if (ambiguous) count++
        }
    }
    return count
}

fun main() {
    val t = System.nanoTime()
    val c = bruteForce198(2000)
    val ms = (System.nanoTime() - t) / 1e6
    println("brute count(D=2000)=$c bestMs=${"%.1f".format(ms)}")
}
