/**
 * Project Euler 005 — 暴力解（教学对比用）
 *
 * 从 20 开始按 20 步进，逐个检查能否被 11..20 整除。
 * 需要试到 232792560，约 1160 万次外循环 —— 能出结果但慢了六个数量级。
 */

fun solveBruteForce(): Long {
    var n = 20L
    while (true) {
        var ok = true
        for (d in 11..20) {
            if (n % d != 0L) { ok = false; break }
        }
        if (ok) return n
        n += 20
    }
}

fun main() {
    println(solveBruteForce())
}
