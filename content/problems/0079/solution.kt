/**
 * Project Euler 079 — Passcode Derivation
 *
 * 思路：一次登录尝试给出「三个数字在口令中出现的先后顺序」，即一组偏序约束。
 * 对每次尝试的每个数字对 (前面的数字 → 后面的数字) 建一条有向边；
 * 口令中出现的每个数字都必须出现（题目问的就是口令本身，被抽到的字符来自口令），
 * 所以口令最短长度 = 出现的不同数字个数，而任何满足全部偏序的拓扑序都是合法口令。
 * 只要约束图恰有唯一的拓扑序，它就是最短口令；用 Kahn 式「每次取唯一入度为 0 的顶点」构造它。
 * 复杂度：O(尝试数 × 3 + 10²) 建图，拓扑排序 O(10²)，与输入规模无关。
 *
 * 需从题目目录运行（读取同目录下的 keylog.txt）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.io.File

fun solve(path: String = "keylog.txt"): Long {
    val attempts = File(path).readLines().map { it.trim() }.filter { it.isNotEmpty() }

    val before = Array(10) { BooleanArray(10) }
    val present = BooleanArray(10)
    for (attempt in attempts) {
        for (i in attempt.indices) {
            val a = attempt[i] - '0'
            present[a] = true
            for (j in i + 1 until attempt.length) before[a][attempt[j] - '0'] = true
        }
    }

    val used = BooleanArray(10)
    val builder = StringBuilder()
    repeat(present.count { it }) {
        var candidate = -1
        for (d in 0..9) {
            if (!present[d] || used[d]) continue
            var hasUnusedPredecessor = false
            for (e in 0..9) {
                if (e != d && present[e] && !used[e] && before[e][d]) {
                    hasUnusedPredecessor = true
                    break
                }
            }
            if (!hasUnusedPredecessor) {
                require(candidate == -1) { "topological order is not unique" }
                candidate = d
            }
        }
        require(candidate != -1) { "cycle detected in precedence graph" }
        used[candidate] = true
        builder.append('0' + candidate)
    }
    return builder.toString().toLong()
}

fun main() {
    println(solve())
}
