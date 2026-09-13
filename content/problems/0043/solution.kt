/**
 * Project Euler 043 — Sub-string Divisibility
 *
 * 优化解：按位深度优先搜索并即时剪枝：放完第 4 位起，每个新数字都要让刚形成的
 * 三位子串被对应素数整除，非法分支立即回退，搜索空间远小于 10!。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val primes = intArrayOf(2, 3, 5, 7, 11, 13, 17)
    var sum = 0L
    val digits = IntArray(10)
    val used = BooleanArray(10)
    val d = IntArray(10)
    fun dfs(pos: Int) {
        if (pos == 10) {
            var v = 0L; for (x in d) v = v * 10 + x
            sum += v; return
        }
        for (cand in 0..9) {
            if (used[cand]) continue
            used[cand] = true; d[pos] = cand
            var ok = true
            if (pos >= 3) {
                val num = d[pos - 2] * 100 + d[pos - 1] * 10 + d[pos]
                if (num % primes[pos - 3] != 0) ok = false
            }
            if (ok) dfs(pos + 1)
            used[cand] = false
        }
    }
    dfs(0)
    return sum
}

fun main() {
    println(solve())
}
