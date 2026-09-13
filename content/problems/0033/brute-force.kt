/**
 * Project Euler 033 — 暴力解（教学对比用）
 *
 * 对每对两位数用字符串逐位比对，直接按交叉相乘验证等值，不做任何位数剪枝。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun gcdL(a: Long, b: Long): Long {
    var x = kotlin.math.abs(a); var y = kotlin.math.abs(b)
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

fun solveBruteForce(): Long {
    fun ok(n: Int, d: Int): Boolean {
        if (n % 10 == 0 && d % 10 == 0) return false
        val ns = n.toString(); val ds = d.toString()
        for (i in 0..1) for (j in 0..1) {
            if (ns[i] != ds[j]) continue
            val nn = ns[1 - i] - '0'; val dd = ds[1 - j] - '0'
            if (dd == 0) continue
            if (nn * d == dd * n && n < d) return true
        }
        return false
    }
    var num = 1; var den = 1
    for (n in 10..99) for (d in 10..99) if (ok(n, d)) { num *= n; den *= d }
    return (den / gcdL(num.toLong(), den.toLong())).toLong()
}

fun main() {
    println(solveBruteForce())
}
