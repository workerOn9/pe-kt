/**
 * PE 180 — 阶为 35 的黄金三元组的相异 s(x, y, z) 之和。
 *
 * 原理：代数因式分解：
 *   f_n(x, y, z) = (x + y - z) · (xⁿ + yⁿ - zⁿ)
 * 若 f_n(x, y, z) = 0，则要么 x + y = z，要么 xⁿ + yⁿ = zⁿ。
 * 由费马大定理（有理数域上的齐次方程），在正有理数域上 xⁿ + yⁿ = zⁿ 有解当且仅当
 *   n ∈ {1, 2, -1, -2}。
 * 对应四种几何关系：
 *   n = 1:  z = x + y
 *   n = 2:  z² = x² + y²
 *   n = -1: 1/z = 1/x + 1/y  =>  z = xy / (x + y)
 *   n = -2: 1/z² = 1/x² + 1/y²  =>  z² = x²y² / (x² + y²)
 *
 * 枚举所有既约分数 a/b（0 < a < b <= 35，共 383 个），
 * 检查算出的 z 是否仍在集合内，将相异的 s = x + y + z 化为既约分数存入集合。
 * 对集合中所有 s 求和得到 t = u/v，返回 u + v。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import java.math.BigInteger

private data class Frac(val num: BigInteger, val den: BigInteger) : Comparable<Frac> {
    companion object {
        fun of(a: Long, b: Long): Frac {
            val g = gcd(Math.abs(a), Math.abs(b))
            val s = if (b < 0) -1L else 1L
            return Frac(
                BigInteger.valueOf(s * a / g),
                BigInteger.valueOf(s * b / g)
            )
        }
        private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
    }

    operator fun plus(other: Frac): Frac {
        val n = num.multiply(other.den).add(other.num.multiply(den))
        val d = den.multiply(other.den)
        val g = n.gcd(d)
        return Frac(n.divide(g), d.divide(g))
    }

    override fun compareTo(other: Frac): Int {
        return num.multiply(other.den).compareTo(other.num.multiply(den))
    }
}

private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

private fun isqrt(n: Long): Long {
    val r = Math.sqrt(n.toDouble()).toLong()
    return if (r * r == n) r else if ((r + 1) * (r + 1) == n) r + 1 else -1L
}

fun solve180(k: Int = 35): Long {
    // 构造集合内全部既约分数
    val ratList = ArrayList<Pair<Int, Int>>()
    val ratSet = HashSet<Pair<Int, Int>>()
    for (b in 2..k) {
        for (a in 1 until b) {
            if (gcd(a, b) == 1) {
                ratList.add(a to b)
                ratSet.add(a to b)
            }
        }
    }

    val distinctS = HashSet<Frac>()

    for (i in 0 until ratList.size) {
        val (a1, b1) = ratList[i]
        for (j in 0 until ratList.size) {
            val (a2, b2) = ratList[j]

            // 1) n = 1: z = x + y = (a1*b2 + a2*b1) / (b1*b2)
            val n1 = a1.toLong() * b2 + a2.toLong() * b1
            val d1 = b1.toLong() * b2
            val g1 = BigInteger.valueOf(n1).gcd(BigInteger.valueOf(d1)).toLong()
            val rn1 = (n1 / g1).toInt()
            val rd1 = (d1 / g1).toInt()
            if (rd1 <= k && (rn1 to rd1) in ratSet) {
                distinctS.add(Frac.of(rn1.toLong(), rd1.toLong()) + Frac.of(a1.toLong(), b1.toLong()) + Frac.of(a2.toLong(), b2.toLong()))
            }

            // 2) n = -1: z = (x*y)/(x+y) = (a1*a2)/(a1*b2 + a2*b1)
            val nm1 = a1.toLong() * a2
            val dm1 = a1.toLong() * b2 + a2.toLong() * b1
            val gm1 = BigInteger.valueOf(nm1).gcd(BigInteger.valueOf(dm1)).toLong()
            val rnm1 = (nm1 / gm1).toInt()
            val rdm1 = (dm1 / gm1).toInt()
            if (rdm1 <= k && (rnm1 to rdm1) in ratSet) {
                distinctS.add(Frac.of(rnm1.toLong(), rdm1.toLong()) + Frac.of(a1.toLong(), b1.toLong()) + Frac.of(a2.toLong(), b2.toLong()))
            }

            // 3) n = 2: z^2 = x^2 + y^2 = (a1^2*b2^2 + a2^2*b1^2) / (b1^2*b2^2)
            val num2 = a1.toLong() * a1 * b2 * b2 + a2.toLong() * a2 * b1 * b1
            val den2 = b1.toLong() * b1 * b2 * b2
            val g2 = BigInteger.valueOf(num2).gcd(BigInteger.valueOf(den2)).toLong()
            val sn2 = isqrt(num2 / g2)
            val sd2 = isqrt(den2 / g2)
            if (sn2 > 0 && sd2 > 0 && sd2 <= k && (sn2.toInt() to sd2.toInt()) in ratSet) {
                distinctS.add(Frac.of(sn2, sd2) + Frac.of(a1.toLong(), b1.toLong()) + Frac.of(a2.toLong(), b2.toLong()))
            }

            // 4) n = -2: z^2 = (x^2*y^2)/(x^2+y^2) = (a1^2*a2^2) / (a1^2*b2^2 + a2^2*b1^2)
            val numM2 = a1.toLong() * a1 * a2 * a2
            val denM2 = a1.toLong() * a1 * b2 * b2 + a2.toLong() * a2 * b1 * b1
            val gM2 = BigInteger.valueOf(numM2).gcd(BigInteger.valueOf(denM2)).toLong()
            val snM2 = isqrt(numM2 / gM2)
            val sdM2 = isqrt(denM2 / gM2)
            if (snM2 > 0 && sdM2 > 0 && sdM2 <= k && (snM2.toInt() to sdM2.toInt()) in ratSet) {
                distinctS.add(Frac.of(snM2, sdM2) + Frac.of(a1.toLong(), b1.toLong()) + Frac.of(a2.toLong(), b2.toLong()))
            }
        }
    }

    var total = Frac(BigInteger.ZERO, BigInteger.ONE)
    for (s in distinctS) {
        total = total + s
    }
    return total.num.add(total.den).toLong()
}

fun main() {
    val answer = solve180()
    println(answer)
}
