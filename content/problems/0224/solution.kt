#!/usr/bin/env kotlin
/**
 * Project Euler 224 — Almost Right-angled Triangles II（几乎直角三角形 II）
 *
 * 思路：与 223 完全同构，只是二次型取值换成 Q = a^2 + b^2 - c^2 = -1。
 *       同一组 Berggren 矩阵 A、B、C（见 223 的 solution.kt）满足 Q(Mv) = Q(v)，
 *       对 Q = -1 的解集同样成立；其正解构成一棵三叉树，唯一的根是 (2,2,3)
 *       （2^2 + 2^2 = 3^2 - 1），每个非根解恰有一个「逆矩阵父亲」，子节点周长严格变大。
 *       答案 = 该树中周长 <= 75,000,000 的节点总数（子节点去重、按 a <= b <= c 排序）。
 *
 * 与 223 的差别：Q = -1 没有 a = 1 的近退化族（1 + b^2 = c^2 - 1 无整数解），
 *   因此解数少得多（周长 7.5e7 才 4,137,330 个节点），DFS 更快。
 * 小规模核对：N = 1000 -> 55、N = 5000 -> 281，与按定义枚举 (a,b) 的暴力法一致。
 * 复杂度：O(答案) 时间，O(树深) 栈空间。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private val MATS = arrayOf(
    longArrayOf(1, -2, 2, 2, -1, 2, 2, -2, 3),   // A
    longArrayOf(1, 2, 2, 2, 1, 2, 2, 2, 3),      // B
    longArrayOf(-1, 2, 2, -2, 1, 2, -2, 2, 3),   // C
)

/** 统计 Q = a^2 + b^2 - c^2 的解（a <= b <= c，a+b > c）中周长 <= n 的个数。 */
private fun countAlmost(n: Long, roots: Array<LongArray>): Long {
    var cap = 1 shl 10
    var sa = LongArray(cap)
    var sb = LongArray(cap)
    var sc = LongArray(cap)
    var sp = 0
    for (r in roots) { sa[sp] = r[0]; sb[sp] = r[1]; sc[sp] = r[2]; sp++ }

    val ca = LongArray(3); val cb = LongArray(3); val cc = LongArray(3)
    val ok = BooleanArray(3)
    var total = 0L

    while (sp > 0) {
        sp--
        val a = sa[sp]; val b = sb[sp]; val c = sc[sp]
        total++
        for (m in 0 until 3) {
            ok[m] = false
            val M = MATS[m]
            var x = M[0] * a + M[1] * b + M[2] * c
            var y = M[3] * a + M[4] * b + M[5] * c
            var z = M[6] * a + M[7] * b + M[8] * c
            if (x < 1L || y < 1L || z < 1L) continue
            if (x > y) { val t = x; x = y; y = t }
            if (y > z) { val t = y; y = z; z = t }
            if (x > y) { val t = x; x = y; y = t }
            if (x + y <= z) continue
            ca[m] = x; cb[m] = y; cc[m] = z; ok[m] = true
        }
        for (m in 0 until 3) {
            if (!ok[m]) continue
            var dup = false
            for (k in 0 until m) {
                if (ok[k] && ca[k] == ca[m] && cb[k] == cb[m] && cc[k] == cc[m]) { dup = true; break }
            }
            if (dup) continue
            if (ca[m] + cb[m] + cc[m] <= n) {
                if (sp == cap) {
                    cap *= 2
                    sa = sa.copyOf(cap); sb = sb.copyOf(cap); sc = sc.copyOf(cap)
                }
                sa[sp] = ca[m]; sb[sp] = cb[m]; sc[sp] = cc[m]; sp++
            }
        }
    }
    return total
}

fun main() {
    val n = 75_000_000L
    val answer = countAlmost(n, arrayOf(longArrayOf(2, 2, 3)))
    println("PE 224 answer = $answer")
    check(answer == 4_137_330L) { "224 mismatch: $answer" }
}
