#!/usr/bin/env kotlin
// PE 202 — Laserbeam（等边三角形激光反射路径计数）
// 思路：展开法。把每次反射改为「反射三角形」，光束路径变为平面上的直线段，
//       终点是入射顶点在某个镜像三角形中的像。建立三角格子坐标：
//       像点 A' = m·u + n·w（u=(1,0)，w=(1/2,√3/2)），顶点 A 的全部像构成
//       子格子 { m ≡ n (mod 3) }（三角格子的 3-染色在反射群下不变）。
//       直线段穿过的网格线数 = 反弹数 = |m|+|n|+|m+n|-3；
//       光束必须射入三角形内部 ⇒ 像点落在 (0°,60°) 扇区 ⇔ m,n ≥ 1。
//       路径不能经过其它顶点 ⇔ gcd(m,n)=1。
//       于是 N 次反弹的路径数 = #{ (m,n): m,n≥1, m+n=(N+3)/2, m≡n (mod 3), gcd(m,n)=1 }。
// 复杂度：O(√S + d(S)·2^ω) —— 分解 S=(N+3)/2 后对无平方因子除数做容斥。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

fun main() {
    val n = 12017639147L
    val s = (n + 3) / 2
    if (s % 3 == 0L) { println(0); return }  // 3|S 时 m≡n≡0 (mod 3)，gcd≥3，无解

    // 分解 S = (N+3)/2（试除到 √S 足够，√6e9 ≈ 7.8e4）
    val primes = mutableListOf<Long>()
    var rem = s
    var d = 2L
    while (d * d <= rem) {
        if (rem % d == 0L) {
            primes.add(d)
            while (rem % d == 0L) rem /= d
        }
        d = if (d == 2L) 3L else d + 2
    }
    if (rem > 1) primes.add(rem)

    // 容斥：count = Σ_{d|S, μ(d)≠0} μ(d) · #{ j ≤ (S-1)/d : d·j ≡ target (mod 3) }
    val target = (2 * (s % 3)) % 3   // m ≡ target (mod 3)，由 m≡n, m+n=S 推出
    val m = s - 1
    var total = 0L
    val k = primes.size
    for (mask in 0 until (1 shl k)) {
        var dd = 1L
        var bits = 0
        for (i in 0 until k) if ((mask shr i and 1) == 1) { dd *= primes[i]; bits++ }
        val q = m / dd
        // d·j ≡ target (mod 3)，3∤S ⇒ 3∤d
        val cj = (target * modInv3(dd % 3)) % 3
        val first = if (cj >= 1) cj else 3
        val cnt = if (first > q) 0 else 1 + (q - first) / 3
        total += if (bits % 2 == 0) cnt else -cnt
    }
    println(total)
}

fun modInv3(a: Long): Long = when (a) {
    1L -> 1L
    2L -> 2L
    else -> error("3 不应整除 d")
}
