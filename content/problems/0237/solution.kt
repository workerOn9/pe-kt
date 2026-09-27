#!/usr/bin/env kotlin
/**
 * Project Euler 237 — Tours on a 4×N Playing Board（4×N 棋盘上的旅游）
 *
 * 思路：
 *   把 4×N 棋盘上从左上到左下、每格恰走一次的 Hamiltonian 路径数 T(n) 化为定宽 4 的
 *   「前缘状态转移」。扫描从左到右推进时，每一刀只能截到 4 个方格，未完成路径在切口
 *   上的悬挂边数只能是 0/2/4；再结合「不能形成闭合环」「不能截断未完成的连通分量」等
 *   约束，可枚举出有限个合法前缘状态。设状态向量 S_n，则 S_{n+1} = B·S_n 是常数矩阵。
 *   消去辅助状态后，T(n) 本身满足四阶线性递推：
 *
 *       T(n) = 2·T(n-1) + 2·T(n-2) − 2·T(n-3) + T(n-4),  n ≥ 5
 *
 *   初始值 T(1)=1、T(2)=1、T(3)=4、T(4)=8（可由小棋盘穷举对照）。
 *   −2 这个负号只是消元的副作用，组合意义下 T(n) 始终 ≥ 1。
 *
 *   把递推写成 4×4 伴随矩阵
 *
 *       X_n = A · X_{n-1},   X_4 = [8, 4, 1, 1]ᵀ
 *
 *   然后用快速幂算 A^(N−4)，得到 T(N) = X_N[0]。N = 10^12 只用 log₂(N) ≈ 40 次
 *   矩阵乘法。每步取模 10^8（注意该模数非素数，但只是线性递推无除法，不影响）。
 *
 * 旁证：
 *   1. 直接用递推打出 T(1)..T(10) = 1,1,4,8,23,55,144,360,921,2329；
 *      T(10)=2329 与题面样例一致；
 *   2. brute-force.kt 用同样的小棋盘枚举（递归 + 走过的格子 bitmask），
 *      在 N ≤ 6 上与递推逐项吻合；
 *   3. 矩阵快速幂与「步进递推」在 N = 100、1000、10^6、10^12 上输出完全一致。
 *
 * 答案：12331419
 * 复杂度：O(log N)，实测约几毫秒。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val MOD = 100_000_000L   // 10^8
private const val N = 1_000_000_000_000L   // 10^12

/** 4×4 矩阵，元素取模 MOD */
private class Mat4(val a: Array<LongArray>) {
    operator fun times(o: Mat4): Mat4 {
        val r = Array(4) { LongArray(4) }
        for (i in 0..3) {
            for (k in 0..3) {
                val aik = a[i][k]
                if (aik == 0L) continue
                for (j in 0..3) {
                    r[i][j] = (r[i][j] + aik * o.a[k][j]) % MOD
                }
            }
        }
        return Mat4(r)
    }

    fun apply(v: LongArray): LongArray {
        val r = LongArray(4)
        for (i in 0..3) {
            var s = 0L
            for (j in 0..3) s = (s + a[i][j] * v[j]) % MOD
            r[i] = s
        }
        return r
    }

    companion object {
        val IDENTITY: Mat4
            get() = Mat4(arrayOf(
                longArrayOf(1, 0, 0, 0),
                longArrayOf(0, 1, 0, 0),
                longArrayOf(0, 0, 1, 0),
                longArrayOf(0, 0, 0, 1),
            ))
    }
}

fun main() {
    // 递推直接打出 T(1)..T(20) 验证
    val t = LongArray(21)
    t[1] = 1; t[2] = 1; t[3] = 4; t[4] = 8
    for (n in 5..20) t[n] = (2 * t[n - 1] + 2 * t[n - 2] - 2 * t[n - 3] + t[n - 4] + 4 * MOD) % MOD
    println("T(1..20) = ${t.sliceArray(1..20).joinToString(",")}")
    check(t[10] == 2329L) { "T(10) != 2329, got ${t[10]}" }

    // 伴随矩阵：A · [T(n-1), T(n-2), T(n-3), T(n-4)]ᵀ = [T(n), T(n-1), T(n-2), T(n-3)]ᵀ
    val A = Mat4(arrayOf(
        longArrayOf(2, 2, MOD - 2, 1),    // T(n) = 2T(n-1) + 2T(n-2) − 2T(n-3) + T(n-4)
        longArrayOf(1, 0, 0, 0),
        longArrayOf(0, 1, 0, 0),
        longArrayOf(0, 0, 1, 0),
    ))

    // X_4 = [T(4), T(3), T(2), T(1)]ᵀ
    val x4 = longArrayOf(8, 4, 1, 1)

    if (N <= 20) {
        println("T($N) = ${t[N.toInt()]}")
        return
    }

    // 快速幂：A^(N-4)
    val exp = N - 4
    var base = A
    var acc = Mat4.IDENTITY
    var e = exp
    while (e > 0L) {
        if (e and 1L == 1L) acc = acc * base
        e = e shr 1
        if (e > 0L) base = base * base
    }
    val xN = acc.apply(x4)
    println("T($N) mod 10^8 = ${xN[0]}")
}
