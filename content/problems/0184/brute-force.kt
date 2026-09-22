/**
 * PE 184 — brute force：对 I_r 的全部点三重枚举，用叉积符号判定原点是否严格在三角形内部。
 * 与 solution.kt 的射线分解公式在小半径上逐一对拍（含题面 8 / 360 / 10600 锚点）。
 */
private fun containsOriginStrict(
    ax: Long, ay: Long, bx: Long, by: Long, cx: Long, cy: Long
): Boolean {
    val d1 = ax * by - ay * bx
    val d2 = bx * cy - by * cx
    val d3 = cx * ay - cy * ax
    return (d1 > 0 && d2 > 0 && d3 > 0) || (d1 < 0 && d2 < 0 && d3 < 0)
}

fun main() {
    for (r in 2..7) {
        val pts = ArrayList<Pair<Int, Int>>()
        for (x in -r + 1 until r) {
            for (y in -r + 1 until r) {
                if (x * x + y * y < r * r) pts.add(x to y)
            }
        }
        var count = 0L
        val n = pts.size
        for (i in 0 until n) {
            val (ax, ay) = pts[i]
            for (j in i + 1 until n) {
                val (bx, by) = pts[j]
                for (k in j + 1 until n) {
                    val (cx, cy) = pts[k]
                    if (containsOriginStrict(
                            ax.toLong(), ay.toLong(), bx.toLong(), by.toLong(),
                            cx.toLong(), cy.toLong()
                        )
                    ) count++
                }
            }
        }
        println("r=$r: brute=$count")
    }
}
