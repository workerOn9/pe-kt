/**
 * Project Euler 029 — 暴力解（教学对比用）
 *
 * 直接对每个 a、b 计算 BigInteger 幂并塞进集合去重，数值最大约 200 位。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

fun solveBruteForce(): Long {
    val set = HashSet<BigInteger>()
    for (a in 2..100) for (b in 2..100) set.add(BigInteger.valueOf(a.toLong()).pow(b))
    return set.size.toLong()
}

fun main() {
    println(solveBruteForce())
}
