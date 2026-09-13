/**
 * Project Euler 068 — Magic 5-gon Ring
 *
 * 优化解：把五条线写成 $(outer_k, inner_k, inner_{k+1})$，$k = 0..4$（下标模 5）。
 * 五条线之和都等于 $S$，于是
 *   $\sum_k (outer_k + 2\,inner_k) = \sum outer + 2\sum inner = 5S$，
 * 又因为 1..10 总和为 55，即 $\sum outer + \sum inner = 55$，代入得
 *   $S = (55 + \sum inner) / 5$。
 * 这说明**内点一经选定，外点就被完全确定**：$outer_k = S - inner_k - inner_{k+1}$。
 * 于是只需枚举内点的有序五元组（$\binom{10}{5}\cdot5! = 30240$ 种），
 * 检查算出的外点恰好是剩下的 5 个数即可。
 * 拼接串从数值最小的外点所在组开始顺时针读取；10 出现在外点时它只被读一次，
 * 总长恰为 16 位（10 在内点会被相邻两条线各读一次，成为 17 位串，题目不要）。
 *
 * 复杂度：O(C(10,5)·5!) 时间，O(1) 额外空间。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val inner = IntArray(5)
    val used = BooleanArray(11)
    var best = 0L

    fun evaluate(innerSum: Int) {
        if ((55 + innerSum) % 5 != 0) return
        val s = (55 + innerSum) / 5
        val outer = IntArray(5)
        val seen = BooleanArray(11)
        for (k in 0 until 5) {
            val o = s - inner[k] - inner[(k + 1) % 5]
            if (o < 1 || o > 10 || used[o] || seen[o]) return
            seen[o] = true
            outer[k] = o
        }
        var hasTen = false
        for (o in outer) if (o == 10) hasTen = true
        if (!hasTen) return // 只有 10 在外点时才得到 16 位串

        var start = 0
        for (k in 1 until 5) if (outer[k] < outer[start]) start = k
        val sb = StringBuilder()
        for (k in 0 until 5) {
            val i = (start + k) % 5
            sb.append(outer[i]).append(inner[i]).append(inner[(i + 1) % 5])
        }
        val v = sb.toString().toLong()
        if (v > best) best = v
    }

    fun assign(pos: Int, sum: Int) {
        if (pos == 5) {
            evaluate(sum)
            return
        }
        for (v in 1..10) {
            if (used[v]) continue
            used[v] = true
            inner[pos] = v
            assign(pos + 1, sum + v)
            used[v] = false
        }
    }

    assign(0, 0)
    return best
}

fun main() {
    println(solve())
}
