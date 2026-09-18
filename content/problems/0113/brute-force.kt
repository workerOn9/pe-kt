/**
 * Project Euler 113 — 对照解（逐长度、末位数字动态规划）
 *
 * 思路：不使用组合恒等式。up[d]、down[d] 分别保存当前长度末位为 d 的递增、递减数个数。
 * 附加数字 d 时，递增可接此前末位≤d 的数，递减可接此前末位≥d 的数。
 * 一位数从 1..9 初始化，禁止前导零；递减允许后续出现 0。每层扣掉 9 个常数位数。
 * 复杂度：O(D·10²) 时间、O(10) 额外空间；真正枚举至 10^100 不可行，因此采用此精确对照。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun countByDigits(digits: Int): Long {
    require(digits in 1..100)
    var up = LongArray(10) { if (it == 0) 0L else 1L }
    var down = up.copyOf()
    var total = 0L
    for (length in 1..digits) {
        total += up.sum() + down.sum() - 9L
        if (length == digits) break
        val nextUp = LongArray(10)
        val nextDown = LongArray(10)
        for (digit in 0..9) {
            for (previous in 0..digit) nextUp[digit] += up[previous]
            for (previous in digit..9) nextDown[digit] += down[previous]
        }
        up = nextUp
        down = nextDown
    }
    return total
}

fun solveBruteForce(): Long {
    check(countByDigits(1) == 9L)
    check(countByDigits(6) == 12951L)
    check(countByDigits(10) == 277032L)
    return countByDigits(100)
}

fun main() { println(solveBruteForce()) }
