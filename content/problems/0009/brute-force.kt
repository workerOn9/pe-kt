/**
 * Project Euler 009 — 暴力解（教学对比用）
 *
 * 枚举 a < b，c 由 c = sum − a − b 直接确定，检查 a²+b² = c²，O(n²)。
 */

fun solveBruteForce(sum: Int = 1000): Long {
    for (a in 1 until sum / 3) {                 // a < b < c ⇒ a < sum/3
        for (b in a + 1 until (sum - a) / 2) {   // b < c ⇒ b < (sum-a)/2
            val c = sum - a - b
            if (a * a + b * b == c * c) return a.toLong() * b * c
        }
    }
    error("无解")
}

fun main() {
    println(solveBruteForce())
}
