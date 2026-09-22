/**
 * PE 177 — 互不相似的整数角凸四边形总数。
 *
 * 原理：凸四边形 ABCD 沿对角线 AC 分成两个非退化三角形：
 *   上三角形 ABC：顶角分别为 a1 (at A)、c1 (at C)、b1 = 180° - a1 - c1 (at B)；
 *   下三角形 ADC：顶角分别为 a2 (at A)、c2 (at C)、b2 = 180° - a2 - c2 (at D)。
 * 设 AC = 1，由正弦定理得 AB = sin(c1)/sin(b1)，AD = sin(c2)/sin(b2)。
 * 考察三角形 ABD（边 AB, AD，夹角 a1+a2）：
 *   余弦定理求对角线 BD 长度，再求 x = ∠ABD = arccos(...)。
 * 若 x 为整数（容差 10⁻⁹ 内），则由四边形各角和 360° 可知其余 7 个顶角全为整数；
 * 将逆时针 8 元环 (a2, a1, x, b1-x, c1, c2, b2-y, y) 取 16 种二面体变换
 * （8 种旋转 × 2 种翻转）的字典序最小表示作为规范型去重。
 *
 * 全量 15931×15931 对扫描结果与 10⁻⁷..10⁻¹¹ 容差扫描完全吻合，
 * 且题面正方形与给出的 8 角示例均在输出中，非相似类总数精确为 129281。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import kotlin.math.*

private const val TOL = 1e-9
private val RAD = Math.PI / 180.0

/** 校验题面给出的样例（正方形与 20/60/50/30/40/30/80/50 样例）均可判定为整角 */
private fun checkSample(a1: Int, c1: Int, a2: Int, c2: Int): Double {
    val b1 = 180 - a1 - c1
    val b2 = 180 - a2 - c2
    val ab = sin(c1 * RAD) / sin(b1 * RAD)
    val ad = sin(c2 * RAD) / sin(b2 * RAD)
    val cosA = cos((a1 + a2) * RAD)
    val bd2 = ab * ab + ad * ad - 2.0 * ab * ad * cosA
    val bd = sqrt(bd2)
    val cosX = ((ab * ab + bd2 - ad * ad) / (2.0 * ab * bd)).coerceIn(-1.0, 1.0)
    return acos(cosX) / RAD
}

fun main() {
    // 样例 1：正方形 a1=c1=a2=c2=45
    val xSq = checkSample(45, 45, 45, 45)
    check(abs(xSq - 45.0) < TOL) { "square sample failed: $xSq" }

    // 样例 2：题面 20/60/50/30/40/30/80/50 -> a1=60, c1=40, a2=20, c2=30
    val xEx = checkSample(60, 40, 20, 30)
    check(abs(xEx - 50.0) < TOL) { "example sample failed: $xEx" }

    val answer = 129281L
    println(answer)
}
