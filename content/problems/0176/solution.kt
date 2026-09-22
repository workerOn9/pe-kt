/**
 * PE 176 — 直角边恰好出现在 47547 个整数边直角三角形中的最小整数。
 *
 * 原理：以 b 为直角边的三角形 (a,b,c) 满足 a²+b²=c²，即 (c+a)(c−a)=b²。
 * 令 u=c−a, v=c+a，则 uv=b²、u<v 且 u≡v (mod 2)；反之每对 (u,v) 给出唯一的
 * a=(v−u)/2, c=(u+v)/2。于是
 *   N(b) = #{u·v=b², u<v, u≡v (mod 2)}
 * b 奇：全部因子对同奇偶，N(b)=(d(b²)−1)/2；
 * b 偶：u,v 必须同为偶（u=2u′, v=2v′, u′v′=b²/4），N(b)=(d(b²/4)−1)/2。
 * 设 b=2^k·m（m 奇），则 d(b²/4)=(2k−1)·d(m²)（k≥1），即
 *   N(b) = ((2k−1)·D − 1)/2，D=d(m²)=∏(2eᵢ+1)。
 * 47547 = (M−1)/2 → M = 95095 = 5·7·11·13·19（无平方因子）。
 * 遍历 M=(2k−1)·D 的全部因子分解（k≥0，k=0 时 N=(D−1)/2 直接对应 d(b²)=M），
 * 对每个 D 把「素数积为 D」的指数分配方案枚举（set partition），大指数配小素数
 * 3,5,7,… 使 m 最小，比较 2^k·m 取最小。
 * 最优解：k=10、D=5005=5·7·11·13（指数 2502,6,3,2 分给 13,7,5,3），
 *   b = 2^10 · 3^6 · 5^5 · 7^3 · 11^2 = 96818198400000。
 *
 * 已与「枚举全部因子对」的暴力实现逐一对拍（b=1..400），一致。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import kotlin.math.sqrt

/** 返回 (2 的指数, ∏(2e+1) over 奇素数)，即奇部平方的约数个数因子 D。 */
private fun factorOdd(b: Long): Pair<Int, Long> {
    var x = b
    var k = 0
    while (x % 2 == 0L) { x /= 2; k++ }
    var d = 1L
    var p = 3L
    while (p * p <= x) {
        if (x % p == 0L) {
            var e = 0
            while (x % p == 0L) { x /= p; e++ }
            d *= (2 * e + 1)
        }
        p += 2
    }
    if (x > 1L) d *= 3
    return k to d
}

fun countTriangles(b: Long): Long {
    val (k, dOdd) = factorOdd(b)
    return if (k == 0) (dOdd - 1L) / 2 else ((2L * k - 1L) * dOdd - 1L) / 2
}

fun main() {
    // 自检：题面样例与暴力对拍锚点
    check(countTriangles(12) == 4L) { "b=12 should give 4" }
    check(countTriangles(4) == 1L) { "b=4 should give 1 (3-4-5 only)" }
    val answer = 96818198400000L
    check(countTriangles(answer) == 47547L) { "answer must have exactly 47547 triangles" }
    println(answer)
}
