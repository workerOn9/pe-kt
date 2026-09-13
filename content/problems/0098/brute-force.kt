/**
 * Project Euler 098 — Anagramic Squares（暴力解）
 *
 * 思路：不做模式索引。对每一对异位词，把该长度的所有平方数逐个试一遍：
 * 逐位建立字母→数字映射并检查单射，再把它套到第二个词上，用整数平方根判断结果
 * 是否为平方数。逻辑直白，但候选平方数是一个都没过滤的全集。
 * 需从题目目录运行（读取同目录 words.txt）。
 * 复杂度：O(异位词对数 × 该长度平方数个数 × 词长)。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

private fun p098Digits(n: Long): Int = when {
    n < 10L -> 1
    n < 100L -> 2
    n < 1_000L -> 3
    n < 10_000L -> 4
    n < 100_000L -> 5
    n < 1_000_000L -> 6
    n < 10_000_000L -> 7
    n < 100_000_000L -> 8
    n < 1_000_000_000L -> 9
    n < 10_000_000_000L -> 10
    n < 100_000_000_000L -> 11
    n < 1_000_000_000_000L -> 12
    n < 10_000_000_000_000L -> 13
    n < 100_000_000_000_000L -> 14
    n < 1_000_000_000_000_000L -> 15
    n < 10_000_000_000_000_000L -> 16
    n < 100_000_000_000_000_000L -> 17
    else -> 18
}

private fun p098IsSquare(v: Long): Boolean {
    if (v < 0) return false
    val r = Math.sqrt(v.toDouble()).toLong()
    var k = r - 2
    if (k < 0) k = 0
    while (k * k < v) k++
    return k * k == v
}

fun solveBruteForce(path: String = "words.txt"): Long {
    val words = java.io.File(path).readText().split(',')
        .map { it.trim().trim('"') }.filter { it.isNotEmpty() }

    val groups = LinkedHashMap<String, MutableList<String>>()
    for (w in words) groups.getOrPut(w.toCharArray().sorted().joinToString("")) { ArrayList() }.add(w)

    val wanted = HashSet<Int>()
    for (g in groups.values) if (g.size > 1) for (w in g) wanted.add(w.length)

    val squaresByLen = HashMap<Int, MutableList<Long>>()
    for (len in wanted) squaresByLen[len] = ArrayList()
    if (wanted.isNotEmpty()) {
        val maxLen = wanted.max()
        var n = 1L
        while (p098Digits(n * n) <= maxLen) {
            val sq = n * n
            squaresByLen[p098Digits(sq)]?.add(sq)
            n++
        }
    }

    var best = 0L
    for (g in groups.values) {
        if (g.size < 2) continue
        for (i in g.indices) for (j in i + 1 until g.size) {
            val w1 = g[i]
            val w2 = g[j]
            if (w1 == w2) continue
            for (sq in squaresByLen[w1.length] ?: continue) {
                val s1 = sq.toString()
                val map = HashMap<Char, Char>()
                val used = HashSet<Char>()
                var ok = true
                for (k in w1.indices) {
                    val ch = w1[k]
                    val dg = s1[k]
                    val old = map[ch]
                    if (old == null) {
                        if (!used.add(dg)) {
                            ok = false
                            break
                        }
                        map[ch] = dg
                    } else if (old != dg) {
                        ok = false
                        break
                    }
                }
                if (!ok) continue
                val t = StringBuilder(w2.length)
                for (ch in w2) t.append(map[ch] ?: ' ')
                if (t[0] == '0') continue
                val v = t.toString().toLongOrNull() ?: continue
                if (!p098IsSquare(v)) continue
                if (sq > best) best = sq
                if (v > best) best = v
            }
        }
    }
    return best
}

fun main() {
    println(solveBruteForce())
}
