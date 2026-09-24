#!/usr/bin/env kotlin
// PE 210 — Obtuse Angled Triangles（钝角三角形）
//
// 思路：三角形至多有一个钝角，故按钝角所在顶点分成三个互斥区域，并扣除退化情形
//       （B 落在直线 y = x 上，与 O、C 共线时无内角可言）：
//         ∠O 钝 ⟺ (B−O)·(C−O) < 0 ⟺ c(x+y) < 0        ⟺ x + y < 0          → 区域 A
//         ∠C 钝 ⟺ (O−C)·(B−C) < 0 ⟺ c(2c−x−y) < 0      ⟺ x + y > r/2        → 区域 B
//         ∠B 钝 ⟺ (O−B)·(C−B) < 0 ⟺ x²+y² − c(x+y) < 0  ⟺ x² + y² < c(x+y)   → 区域 C
//       其中 c = r/4，且由 |x|+|y| ≤ r 知 x+y ≤ r、x+y ≥ −r，三个条件不能同时成立。
//
//       A：#S(r) = T = 2r²+2r+1，#{x+y=0} = 2⌊r/2⌋+1，按 (x,y)→(−x,−y) 对称得
//          #{x+y<0} = (T − (2⌊r/2⌋+1))/2；再减去共线的 x = y < 0 的 ⌊r/2⌋ 个点。
//       B：直线 x+y = k（0 ≤ k ≤ r）与菱形相交的整点数为 k+1+2⌊(r−k)/2⌋，
//          对 k ∈ [r/2+1, r] 求和后化简（r = 4c 时即 8c²+c）；再减去共线的 x = y > c 的 c 个点。
//       C：x²+y² < c(x+y) ⟺ (x−c/2)² + (y−c/2)² < c²/2。该圆盘半径 c/√2，
//          盘内最大 |x|+|y| ≤ c/2+c/2+c/√2·... = 2c = r/2 < r，故完全落在 S(r) 内，
//          无需截断。c 为偶数时 U = x−c/2、V = y−c/2 遍历全部整数，
//          条件化为 U²+V² < R²（R² = c²/2）；精确圆盘点数按列求和
//          #{V : V² < R²−U²} = 2⌊√(R²−U²−1)⌋+1；最后减去共线的 0 < x = y < c 的 c−1 个点。
//
// 复杂度：A、B 为 O(1)；C 枚举 ⌈c/√2⌉ ≈ 1.8×10⁸ 列，每列一次整数开方 —— 本机约 1 s 量级。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

fun main() {
    val r = 1_000_000_000L
    println(solve210(r))
}

/** 返回 N(r)：钝角三角形的点数。 */
fun solve210(r: Long): Long {
    val c = r / 4

    // 区域 A：∠O 钝
    val t = 2 * r * r + 2 * r + 1
    val a = (t - (2 * (r / 2) + 1)) / 2 - r / 2

    // 区域 B：∠C 钝。分层求和 Σ_{k=r/2+1}^{r}(k+1+2⌊(r−k)/2⌋) 的闭式（r = 4c）为 8c²+c
    val bSum = 8 * c * c + c
    val b = bSum - maxOf(0L, r / 2 - c)

    // 区域 C：∠B 钝 —— 精确圆盘点数
    val radius2 = c * c / 2                       // c 偶 ⇒ 整数
    var disc = 0L
    var u = 0L
    while (u * u < radius2) {
        val m = isqrt(radius2 - u * u - 1)         // #{V : V² < R²−U²} = 2m+1
        disc += (2 * m + 1) * if (u == 0L) 1L else 2L
        u++
    }
    val cc = disc - (c - 1)

    return a + b + cc
}

/** ⌊√n⌋ 的纯整数实现：double 估算后校正，避开 3×10¹⁶ 量级的舍入误差。 */
private fun isqrt(n: Long): Long {
    var x = Math.sqrt(n.toDouble()).toLong()
    while (x > 0 && x * x > n) x--
    while ((x + 1) * (x + 1) <= n) x++
    return x
}
