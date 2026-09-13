/**
 * Project Euler 091 — Right Triangles with Integer Coordinates
 *
 * 思路：直角只可能落在 O、P、Q 之一，按直角顶点分类计数。
 *  · 直角在 O：P·Q = 0，坐标非负下只能是 P 在 y 轴、Q 在 x 轴（或反之），
 *    无序点对贡献 50×50。
 *  · 直角在 P 或 Q：条件 (Q−P)·(O−P) = 0，即 Q − P 落在与向量 P 垂直的一维子空间上。
 *    枚举每个可能的直角顶点 V ≠ O：取 g = gcd(Vx,Vy)，垂直方向的原始整数向量为
 *    w = (Vy/g, −Vx/g)，于是另一顶点 = V + j·w（j ∈ ℤ，j ≠ 0 排除两点重合）。
 *    因为 |w| 分量 ≤ 50，格点范围又限死了位移，只需检查 j ∈ [−50, 50]。
 *    该枚举对每个直角三角形恰好计一次（直角顶点唯一）。
 *
 * 复杂度：O(N³)（点数 × 每个点的候选步长），N=50 实测 &lt; 1 ms。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private const val p091N = 50

private fun p091Gcd(a: Int, b: Int): Int {
    var x = a
    var y = b
    while (y != 0) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

fun solve(): Long {
    val n = p091N
    var total = n.toLong() * n
    var rightAtP = 0L
    for (x in 0..n) for (y in 0..n) {
        if (x == 0 && y == 0) continue
        val g = p091Gcd(x, y)
        val wx = y / g
        val wy = -x / g
        for (j in -n..n) {
            if (j == 0) continue
            val qx = x + j * wx
            val qy = y + j * wy
            if (qx in 0..n && qy in 0..n) rightAtP++
        }
    }
    return total + rightAtP
}

fun main() {
    println(solve())
}
