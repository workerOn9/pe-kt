/**
 * PE 168 — 右旋转数（n 整除自己的右旋转）。
 * 设 n 有 d 位、末位为 a、前 d-1 位为 x（n = 10x + a），右旋转 R = a·10^(d-1) + x。
 * R = m·n（1<=m<=9，因为两者同位数）推出 x = a(10^(d-1) - m) / (10m - 1)。
 * 枚举 d=2..100、m=1..9、a=1..9，只要整除且 x 落在 [0,10^(d-1)) 内即为解。
 * d 可到 100，需 BigInteger。已由 Python 两套独立实现互证、且与 d<=6 的暴力枚举对拍一致。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import java.math.BigInteger

fun solve(): Long {
    var total = BigInteger.ZERO
    val ten = BigInteger.TEN
    for (d in 2..100) {
        val p = ten.pow(d - 1)
        for (m in 1..9) {
            val den = BigInteger.valueOf((10L * m - 1))
            for (a in 1..9) {
                val num = BigInteger.valueOf(a.toLong()).multiply(p.subtract(BigInteger.valueOf(m.toLong())))
                if (num.mod(den).signum() != 0) continue
                val x = num.divide(den)
                if (x.signum() < 0 || x >= p) continue
                val n = ten.multiply(x).add(BigInteger.valueOf(a.toLong()))
                if (n >= p && n < p.multiply(ten)) total = total.add(n)
            }
        }
    }
    return total.mod(BigInteger.valueOf(100000L)).toLong()
}

fun main() {
    println(solve())
}
