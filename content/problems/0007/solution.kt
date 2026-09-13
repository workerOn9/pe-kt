/**
 * Project Euler 007 — 10 001st Prime
 *
 * 优化解：埃氏筛 + 素数定理上界。
 * 第 n 个素数 p_n 满足 p_n < n(ln n + ln ln n)（n ≥ 6），
 * 据此确定筛的边界，一次筛出所有素数后取第 10001 个，O(B log log B)。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import kotlin.math.ln

/** 第 n 个素数的安全上界（Rosser 定理，n ≥ 6 时严格成立） */
fun upperBound(n: Int): Int {
    val l = ln(n.toDouble())
    return (n * (l + ln(l))).toInt() + 1
}

/** 埃氏筛求第 n 个素数（n 从 1 起计） */
fun solve(n: Int = 10001): Long {
    val bound = upperBound(n)
    val isComposite = BooleanArray(bound + 1)
    var count = 0
    for (i in 2..bound) {
        if (!isComposite[i]) {
            count++
            if (count == n) return i.toLong()
            // 标记 i 的倍数；从 i*i 开始，更小的倍数已被更小的素数标记
            var j = i.toLong() * i
            while (j <= bound) {
                isComposite[j.toInt()] = true
                j += i
            }
        }
    }
    error("上界 $bound 内不足 $n 个素数")
}

fun main() {
    println(solve())
}
