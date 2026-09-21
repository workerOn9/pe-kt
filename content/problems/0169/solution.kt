/**
 * PE 169 — 二的幂之和表示（超二进制表示计数）。
 * 递推：f(2m+1) = f(m)；f(2m) = f(m) + f(m-1)。
 * 输入 10^25 超出 Long 范围，故用 BigInteger；答案本身仍在 Long 内。
 * 复杂度 O(log n)。已由 Python 独立实现验证 = 178653872807（且 f(10)=5 与题面一致）。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import java.math.BigInteger

private val memo = HashMap<BigInteger, Long>()

fun f(n: BigInteger): Long {
    if (n <= BigInteger.ONE) return 1L
    memo[n]?.let { return it }
    val half = n.shiftRight(1)
    val v = if (n.testBit(0)) f(half) else f(half) + f(half.subtract(BigInteger.ONE))
    memo[n] = v
    return v
}

fun solve(): Long = f(BigInteger.TEN.pow(25))

fun main() {
    println(solve())
}
