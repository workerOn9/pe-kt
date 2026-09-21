/**
 * PE 172 — brute force：枚举所有 L 位数，检查每个数字出现次数 ≤ maxCount。
 * 只适合小 L（10^L 量级），用于与组合公式对拍。
 */
fun bruteForce(L: Int, maxCount: Int): Long {
    var count = 0L
    fun rec(pos: Int, counts: IntArray) {
        if (pos == L) { count++; return }
        for (d in 0..9) {
            if (pos == 0 && d == 0) continue
            if (counts[d] >= maxCount) continue
            counts[d]++
            rec(pos + 1, counts)
            counts[d]--
        }
    }
    rec(0, IntArray(10))
    return count
}

fun main() {
    println("L=4 max=1 -> " + bruteForce(4, 1))
    println("L=4 max=2 -> " + bruteForce(4, 2))
    println("L=5 max=2 -> " + bruteForce(5, 2))
}
