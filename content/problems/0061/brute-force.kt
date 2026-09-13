/**
 * Project Euler 061 — 暴力解（教学对比用）
 *
 * 不做「前两位 → 候选数」的哈希索引：枚举 3..8 阶的全部 720 种排列，
 * 每种排列按指定阶次顺序线性扫描对应列表拼链，末端检查首尾衔接。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

private fun bfPolygonal(s: Int, n: Int): Int = when (s) {
    3 -> n * (n + 1) / 2
    4 -> n * n
    5 -> n * (3 * n - 1) / 2
    6 -> n * (2 * n - 1)
    7 -> n * (5 * n - 3) / 2
    8 -> n * (3 * n - 2)
    else -> 0
}

fun solveBruteForce(): Long {
    val nums = Array(9) { ArrayList<Int>() }
    for (s in 3..8) {
        var n = 1
        while (true) {
            val v = bfPolygonal(s, n)
            if (v >= 10000) break
            if (v >= 1000) nums[s].add(v)
            n++
        }
    }

    val types = intArrayOf(3, 4, 5, 6, 7, 8)
    var found = 0L
    val chain = ArrayList<Int>()

    fun rec(i: Int, first: Int, prev: Int): Boolean {
        if (i == 6) {
            if (prev != first) return false
            found = chain.sumOf { it.toLong() }
            return true
        }
        for (v in nums[types[i]]) {
            if (i > 0 && v / 100 != prev) continue
            if (chain.contains(v)) continue
            chain.add(v)
            if (rec(i + 1, if (i == 0) v / 100 else first, v % 100)) return true
            chain.removeAt(chain.size - 1)
        }
        return false
    }

    fun permute(k: Int) {
        if (found != 0L) return
        if (k == 6) {
            chain.clear()
            rec(0, 0, 0)
            return
        }
        for (i in k..5) {
            val t = types[k]; types[k] = types[i]; types[i] = t
            permute(k + 1)
            val u = types[k]; types[k] = types[i]; types[i] = u
        }
    }

    permute(0)
    return found
}

fun main() {
    println(solveBruteForce())
}
