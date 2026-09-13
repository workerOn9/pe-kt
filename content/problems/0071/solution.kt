/**
 * Project Euler 071 — Ordered Fractions
 *
 * 优化解：设最接近 $3/7$ 且小于它的最简分数为 $p/q$（$q \le 10^6$）。差值为
 *   $$\frac{3}{7} - \frac{p}{q} = \frac{3q - 7p}{7q} = \frac{k}{7q},\qquad k = 3q-7p \ge 1 \text{ 为整数}.$$
 * 要让差值最小：先取 $k = 1$（任何 $k \ge 2$ 的候选都比同一个 $q$ 下 $k=1$ 的候选远），
 * 再取满足 $3q \equiv 1 \pmod 7$ 的**最大** $q \le 10^6$。
 * $3q \equiv 1 \pmod 7 \iff q \equiv 5 \pmod 7$，从 $10^6$ 向下最多退 6 步即得
 * $q = 999997$，$p = (3q-1)/7 = 428570$。
 * 此时 $\gcd(p,q) = 1$ 自动成立：任何公因子整除 $7p = 3q-1$ 与 $3q$，故整除 1。
 *
 * 复杂度：O(1)（最多 7 次整数运算）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val n = 1_000_000L
    var q = n
    while ((3 * q - 1) % 7 != 0L) q--
    return (3 * q - 1) / 7
}

fun main() {
    println(solve())
}
