/**
 * PE 177 — brute force：对较小角度范围（如 10 的倍数角构成的四边形）
 * 全量枚举两三角形并由正弦/余弦定理判定分裂角是否为整数，统计非相似类。
 * 用于验证规范型去重逻辑（二面体 D8 作用下的 16 种旋转/翻转）。
 */
import kotlin.math.*

private const val TOL = 1e-9
private val RAD = Math.PI / 180.0

private fun canonical(arr: IntArray): String {
    val n = 8
    var best: String? = null
    for (refl in 0..1) {
        val cur = if (refl == 0) arr else IntArray(n) { arr[n - 1 - it] }
        for (r in 0 until n) {
            val sb = StringBuilder(24)
            for (i in 0 until n) {
                if (i > 0) sb.append(',')
                sb.append(cur[(r + i) % n])
            }
            val s = sb.toString()
            if (best == null || s < best) best = s
        }
    }
    return best!!
}

fun main() {
    // 10 度的整数倍角四边形小规模枚举
    val step = 10
    val triples = ArrayList<Triple<Int, Int, Int>>()
    for (a in step..170 step step) {
        for (c in step..(170 - a) step step) {
            triples.add(Triple(a, c, 180 - a - c))
        }
    }
    val seen = HashSet<String>()
    for (t1 in triples) {
        val (a1, c1, b1) = t1
        val ab = sin(c1 * RAD) / sin(b1 * RAD)
        for (t2 in triples) {
            val (a2, c2, b2) = t2
            val ad = sin(c2 * RAD) / sin(b2 * RAD)
            val cosA = cos((a1 + a2) * RAD)
            val bd2 = ab * ab + ad * ad - 2.0 * ab * ad * cosA
            val bd = sqrt(bd2)
            val cosX = ((ab * ab + bd2 - ad * ad) / (2.0 * ab * bd)).coerceIn(-1.0, 1.0)
            val x = acos(cosX) / RAD
            val xr = round(x)
            if (abs(x - xr) < TOL && xr >= 1.0 && xr <= b1 - 1.0) {
                val xi = xr.toInt()
                val yi = 180 - a1 - a2 - xi
                if (yi in 1 until b2) {
                    val ring = intArrayOf(a2, a1, xi, b1 - xi, c1, c2, b2 - yi, yi)
                    seen.add(canonical(ring))
                }
            }
        }
    }
    println("10-degree grid non-similar count: ${seen.size}")
    check(seen.isNotEmpty()) { "must find quadrilaterals on 10-deg grid" }
}
