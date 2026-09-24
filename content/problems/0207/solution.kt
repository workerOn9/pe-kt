#!/usr/bin/env kotlin
// PE 207 — Integer Partition Game（整数分拆方程）
// 思路：4^t = 2^t + k，令 x = 2^t，则 k = x² - x = x(x-1)。
//       每个正整数 k 恰有一个实数 t 对应一个分拆（x>1 单调）；分拆全体按 k = x(x-1) 枚举，x 递增。
//       完美 ⟺ t ∈ Z ⟺ x = 2^t 是 2 的幂。
//       P(m) 在 k = x(x-1) 处取得 c/(x-1)，c = 到 x 为止 2 的幂个数 = ⌊log2 x⌋ + 1。
//       扫描 x，找第一个使 c/(x-1) < 1/12345（即 c·12345 < x-1）的 x，输出 k = x(x-1)。
// 复杂度：x 扫到约 1.6×10^6 —— 毫秒级；全整数运算无浮点。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

fun main() {
    var perfect = 0L                     // 目前为止的完美分拆数（= 已遇到的 2 的幂个数）
    var x = 2L                           // x = 2^t > 1；x=2 即 k=2（4^1=2^1+2）
    while (true) {
        if (x and (x - 1) == 0L) perfect++   // x 是 2 的幂 → 完美分拆
        // P(k) = perfect/(x-1) < 1/12345  ⟺  perfect·12345 < x-1
        if (perfect * 12345L < x - 1) {
            println(x * (x - 1))
            break
        }
        x++
    }
}
