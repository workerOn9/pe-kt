/**
 * Project Euler 041 — 暴力解（教学对比用）
 *
 * 从 9 位开始按字典序递减生成全数字排列并逐一判素，不预先利用数位和性质。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isPandigitalN(n: Int): Boolean {
    val s = n.toString()
    val k = s.length
    val seen = BooleanArray(10)
    for (ch in s) { val d = ch - '0'; if (d == 0 || d > k || seen[d]) return false; seen[d] = true }
    return true
}

fun isPrimeLong(n: Long): Boolean {
    if (n < 2) return false
    if (n < 4) return true
    if (n % 2L == 0L) return false
    var d = 3L
    while (d <= n / d) { if (n % d == 0L) return false; d += 2 }
    return true
}

fun solveBruteForce(): Long {
    for (k in 9 downTo 1) {
        val used = BooleanArray(10)
        val buf = IntArray(k)
        var found = -1L
        fun dfs(pos: Int): Boolean {
            if (pos == k) {
                var v = 0L; for (x in buf) v = v * 10 + x
                if (isPrimeLong(v)) { found = v; return true }
                return false
            }
            for (cd in k downTo 1) {
                if (used[cd]) continue
                used[cd] = true; buf[pos] = cd
                if (dfs(pos + 1)) return true
                used[cd] = false
            }
            return false
        }
        if (dfs(0)) return found
    }
    return 0
}

fun main() {
    println(solveBruteForce())
}
