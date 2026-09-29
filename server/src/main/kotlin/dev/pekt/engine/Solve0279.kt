package dev.pekt.engine

import dev.pekt.math.gcd

/**
 * PE 279 — Triangles with Integral Sides and an Integral Angle（整数边整数角三角形）：
 * 求周长 ≤ 10^8、三边为整数、至少有一个整数度角的三角形个数。
 *
 * 推导（详见 content/problems/0279/solution.kt 头部与 0279/analysis.md）：
 *   三边为整数时任一角 θ 的余弦 (u²+v²−s²)/(2uv) 为有理数；θ 为整数度使 θ/180 有理，
 *   Niven 定理 ⇒ cos θ ∈ {0, ±1/2, ±1}，去掉退化的 ±1 后 θ ∈ {60°, 90°, 120°}，
 *   对应 s² = u²+v²−uv、u²+v²、u²+v²+uv。30-60-90 的三边比 1 : √3 : 2 无整数实现，
 *   120° 与 60°、90° 与 120° 都不可能共存，故三类互不重叠，答案 = 三类之和。
 *   每类用本原三边参数化 + 缩放计数（本原周长 P 贡献 ⌊10^8/P⌋ 个）：
 *     · 90°：m > n ≥ 1，gcd = 1，m+n 奇；P = 2m(m+n)；
 *     · 60°：m ≥ 2，⌈m/2⌉ ≤ n < m，gcd = 1；g = 3（若 3 | m+n）否则 1，P = (2m²+mn−n²)/g；
 *     · 120°：m > n ≥ 1，gcd = 1，3 ∤ (m−n)；P = 2m² + 3mn + n²。
 *
 * 复杂度：三条求和共约 6×10^7 对 (m, n)（m ≤ √(3·10^8)），O(N) 时间、O(1) 额外空间；
 *   本机 JIT 预热后约 1.6 s（远低于 10 s 熔断线；solution.kt 的同一逻辑用互素标记把逐对 gcd
 *   换成筛法标记后约 96 ms，这里按项目惯例复用 dev.pekt.math.gcd）。
 *
 * 校验：题面无小样例；定义级暴力（枚举 a ≤ b ≤ c 按精确等式判角）与方法 A/B（对偶域参数化）
 *   在 N = 3…60、100、200、500、1000、2000 逐值一致，N=2000 时 90°/60°/120° 三类为
 *   744/1697/672（三类之和 = 总数 3113，验证无重复计数）；引擎实跑结果 416577688 与
 *   solution.kt 主路径、方法 B 及暴力外推一致（公开答案表亦为 416577688，仅作旁证）。
 */
internal fun solve0279Impl(): Long {
    val limit = 100_000_000L
    return count90Engine(limit) + count60Engine(limit) + count120Engine(limit)
}

// 90°：m > n ≥ 1，gcd = 1，m + n 奇（即 m − n 奇），P = 2m(m+n)
private fun count90Engine(limit: Long): Long {
    var total = 0L
    var m = 2L
    while (2 * m * (m + 1) <= limit) {
        var n = if (m % 2L == 0L) 1L else 2L      // m + n 必须为奇数
        while (n < m) {
            val p = 2 * m * (m + n)
            if (p > limit) break                 // p 关于 n 递增
            if (gcd(m, n) == 1L) total += limit / p
            n += 2
        }
        m++
    }
    return total
}

// 60°：m ≥ 2，⌈m/2⌉ ≤ n < m，gcd = 1；3 | m+n 时约掉公因子 3，P = (2m²+mn−n²)/g
private fun count60Engine(limit: Long): Long {
    var total = 0L
    var m = 2L
    while (2 * m * m <= 3 * limit) {
        var n = (m + 1) / 2
        while (n < m) {
            if (gcd(m, n) == 1L) {
                var p = 2 * m * m + m * n - n * n
                if ((m + n) % 3L == 0L) p /= 3
                if (p <= limit) total += limit / p
            }
            n++
        }
        m++
    }
    return total
}

// 120°：m > n ≥ 1，gcd = 1，3 ∤ (m−n)，P = 2m² + 3mn + n²
private fun count120Engine(limit: Long): Long {
    var total = 0L
    var m = 2L
    while (2 * m * m < limit) {
        var n = 1L
        while (n < m) {
            val p = 2 * m * m + 3 * m * n + n * n
            if (p > limit) break                 // p 关于 n 递增
            if ((m - n) % 3L != 0L && gcd(m, n) == 1L) total += limit / p
            n++
        }
        m++
    }
    return total
}
