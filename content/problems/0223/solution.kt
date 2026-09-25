#!/usr/bin/env kotlin
/**
 * Project Euler 223 — Almost Right-angled Triangles I（几乎直角三角形 I）
 *
 * 思路：条件 a^2 + b^2 = c^2 + 1（a <= b <= c）等价于整系数二次型
 *       Q(a,b,c) = a^2 + b^2 - c^2 的取值 Q = 1，而 Berggren 的三个矩阵
 *           A = [[ 1,-2, 2], [ 2,-1, 2], [ 2,-2, 3]]
 *           B = [[ 1, 2, 2], [ 2, 1, 2], [ 2, 2, 3]]
 *           C = [[-1, 2, 2], [-2, 1, 2], [-2, 2, 3]]
 *       的行列式均为 ±1 且满足 Q(Mv) = Q(v)（勾股树用的是同一组矩阵），
 *       所以它们把「解」映成「解」，且子节点三边同增——周长严格变大。
 *       每个非根解在三个逆矩阵中恰有一个能把三边化小（唯一父亲），
 *       于是 Q = 1 的全部正解构成两棵三叉树，根为 (1,1,1) 与 (1,2,2)：
 *         (1,1,1) 派生 (1,3,3)、(1,5,5)、… 这一族 a = 1 的近退化三角形，
 *         (1,2,2) 派生其余解。
 *       答案 = 这两棵树里周长 <= 25,000,000 的节点总数。子节点需去重
 *       （(1,1,1) 的 A、C 两个矩阵给出同一个 (1,3,3)），并按 a <= b <= c 排序。
 *
 * 已知的小规模核对：N = 1000 -> 1046、N = 5000 -> 6350，与直接枚举
 *   (u,v) = (c-b, c+b)、u*v = a^2-1 的暴力法逐个一致（见 brute-force.kt）。
 * 复杂度：O(答案) 时间（每个解只访问一次，61,614,848 个节点），O(树深) 栈空间。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

/** 三个 Berggren 型矩阵（按行展开）。 */
private val MATS = arrayOf(
    longArrayOf(1, -2, 2, 2, -1, 2, 2, -2, 3),   // A
    longArrayOf(1, 2, 2, 2, 1, 2, 2, 2, 3),      // B
    longArrayOf(-1, 2, 2, -2, 1, 2, -2, 2, 3),   // C
)

/**
 * 统计二次型值 Q 的解（a <= b <= c，非退化三角形 a+b > c）中周长 <= n 的个数。
 * roots 为该 Q 的「根」解（没有更小父亲的解）。
 */
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
            if (x + y <= z) continue          // 退化，丢弃
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
    val n = 25_000_000L
    val answer = countAlmost(n, arrayOf(longArrayOf(1, 1, 1), longArrayOf(1, 2, 2)))
    println("PE 223 answer = $answer")
    check(answer == 61_614_848L) { "223 mismatch: $answer" }
}
