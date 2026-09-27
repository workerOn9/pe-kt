#!/usr/bin/env kotlin
/**
 * Project Euler 237 — brute-force：4×N 棋盘上穷举所有 Hamiltonian 路径（仅 N ≤ 6）。
 *
 * 把棋盘当作 4 行 N 列的方格图（每格一个 0..3 行 × 0..N-1 列的 id），从 (0,0) 出发
 * 走到 (3,0)，每格恰走一次。用 DFS + 位图记录已访问格，验证 T(1)..T(6) 是否与递推一致：
 *   T(1)=1, T(2)=1, T(3)=4, T(4)=8, T(5)=23, T(6)=55
 */

private const val H = 4

private fun countTours(N: Int): Long {
    val total = H * N
    val visited = BooleanArray(total)
    visited[0] = true                       // start at top-left, id=0
    fun neighbors(v: Int): List<Int> {
        val r = v / N; val c = v % N
        val out = ArrayList<Int>(4)
        if (r > 0) out += (r - 1) * N + c
        if (r < H - 1) out += (r + 1) * N + c
        if (c > 0) out += r * N + (c - 1)
        if (c < N - 1) out += r * N + (c + 1)
        return out
    }
    var count = 0L
    fun dfs(v: Int, steps: Int) {
        if (v == (H - 1) * N) {             // bottom-left
            if (steps == total) count++
            return
        }
        for (u in neighbors(v)) {
            if (!visited[u]) {
                visited[u] = true
                dfs(u, steps + 1)
                visited[u] = false
            }
        }
    }
    dfs(0, 1)
    return count
}

fun main() {
    val expected = longArrayOf(0, 1, 1, 4, 8, 23, 55)
    for (N in 1..6) {
        val c = countTours(N)
        println("T($N) = $c  (expected ${expected[N]})")
        check(c == expected[N]) { "mismatch at N=$N" }
    }
    println("brute-force OK")
}
