#!/usr/bin/env kotlin
// PE 209 — Circular Logic（循环逻辑）
// 思路：将 64 个 6-bit 输入状态视为节点，定义映射 σ(a,b,c,d,e,f) = (b,c,d,e,f, a^(b&c))。
//       约束等价于：任意状态与其 σ 后继不能同时为 1，即 τ 的 1-集合必须是 σ 图的一个独立集。
//       σ 是置换（每步前 5 位左移 + 新位确定），其功能图分解为不相交环；
//       每环长度为 L 时，独立集数 = 2·F_{L-1} + F_{L-2} = F_{L-1} + F_{L+1}（Lucas 型递推）。
//       全答案 = 各环独立集数的乘积（环间选择独立）。
// 复杂度：64 状态遍历分解环 O(64)，查 Lucas 表乘起来 —— 纳秒级。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

fun main() {
    // σ: 状态 s ∈ [0,63] → (b,c,d,e,f, a^(b&c))，s 的最高位是 a
    fun sigma(s: Int): Int {
        val a = s shr 5 and 1
        val b = s shr 4 and 1
        val c = s shr 3 and 1
        val d = s shr 2 and 1
        val e = s shr 1 and 1
        val f = s and 1
        return (b shl 5) or (c shl 4) or (d shl 3) or (e shl 2) or (f shl 1) or (a xor (b and c))
    }
    // 分解环
    val seen = BooleanArray(64)
    var ans = 1L
    for (s in 0..63) {
        if (seen[s]) continue
        var x = s; var len = 0
        while (!seen[x]) { seen[x] = true; x = sigma(x); len++ }
        ans *= lucas(len)
    }
    println(ans)
}

// 环长 L 的独立集数 = F_{L-1} + F_{L+1}（斐波那契 F_0=0, F_1=1, F_2=1）
// 用快速倍增（返回 (F_n, F_{n+1}) 对），避免 L=1 的边界混淆
private fun lucas(L: Int): Long {
    fun fibPair(n: Int): Pair<Long, Long> {          // (F_n, F_{n+1})
        if (n == 0) return 0L to 1L
        val (a, b) = fibPair(n / 2)                  // a=F_k, b=F_{k+1}
        val c = a * (2 * b - a)                      // F_{2k}
        val d = a * a + b * b                        // F_{2k+1}
        return if (n % 2 == 0) c to d else d to (c + d)
    }
    return fibPair(L - 1).first + fibPair(L + 1).first
}
