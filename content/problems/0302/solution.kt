#!/usr/bin/env kotlin
/**
 * Project Euler 302 — Strong Achilles Numbers（强阿基里斯数）
 *
 * 题目：正整数 S 是阿基里斯数 ⟺ S 是强大数（powerful：每个素因子的指数 ≥ 2）
 * 且不是完全幂（所有指数之 gcd = 1）。S 称为强阿基里斯数，若 S 与 φ(S) 都是阿基里斯数。
 * 求 S ≤ 10^18 的强阿基里斯数个数。
 *
 * 思路推导
 * ────────
 * 引理 1（素因子有界）：若阿基里斯数 S 的所有素因子指数都 ≤ 2，由强大性每个指数恰为 2，
 * 则指数之 gcd = 2 ≠ 1，S 是完全平方，矛盾。故阿基里斯数必有某个指数 ≥ 3，
 * 最大素因子 p 满足 p^3 | S ≤ 10^18，即 p ≤ 10^6。素因子只需在 ≤ 10^6 的素数中枚举。
 *
 * 引理 2（φ 的结构）：对 S = ∏ p^{e_p}，有
 *   φ(S) = ∏ p^{e_p−1} · (p−1)。
 * 故 φ(S) 的素因子指数表 = 各素因子自身的 (e_p−1) 贡献，叠加 (p−1) 的素因子分解贡献。
 *
 * DFS 枚举：按素数表顺序构造候选 S（每个素因子指数从 2 起、可继续乘 p 增大），维护
 *   cur    —— 当前乘积（≤ limit）
 *   expGcd —— 已选指数的 gcd（S 是否非完全幂）
 *   phiExp —— φ(S) 的素因子指数表（HashMap）
 * 剪枝（欠覆盖）：存在素数 q 使 phiExp[q] < 2 且 phiExp[q] + extraCap[q] < 2 时剪枝，
 * 其中 extraCap[q] = Σ_{p' 素数 ≤ maxPrime, q | (p'−1)} v_q(p'−1)，是未来所有候选
 * 素数对 q 指数的乐观总贡献；连乐观补都补不到 2，则 φ 永远不可能强大。
 *
 * 计数条件（每个节点都判）：cur > 1 && expGcd == 1 && 所有 phiExp ≥ 2 && phiExp 之 gcd == 1。
 * 撤销历史用「快照水位」：addPhi 返回改动前 hist 长度，回溯只弹到该水位，
 * 避免内层递归的 undo 误删外层条目（曾因此把 108 这类数虚报进解集）。
 *
 * 溢出防护：所有乘法判断一律用除法形式（cur > limit / p / p、pe > limit / cur / p 等），
 * 绝不在 Long 上先乘后比（曾因 cur*pe 溢出为负导致死循环式无限递归）。
 *
 * 复杂度：素因子空间 ≤ 78 498（≤ 10^6 的素数个数）；DFS 剪枝后实际扩展节点
 * 远小于素数组合数，全量 limit = 10^18 本机实测约 X ms（见 analysis）。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 AGENTS.md 的 compiler-embeddable shim 编译运行）
 */

import kotlin.math.sqrt

fun main() {
    val limit = 1_000_000_000_000_000_000L
    // 素因子上限：由引理 1 取 min(√limit, 10^6) = 10^6
    val maxPrime = minOf(sqrt(limit.toDouble()).toLong().toInt(), 1_000_000)

    // 素数表（埃氏筛到 maxPrime）
    val isP = BooleanArray(maxPrime + 1) { it >= 2 }
    for (i in 2..sqrt(maxPrime.toDouble()).toInt()) if (isP[i]) {
        var j = i * i; while (j <= maxPrime) { isP[j] = false; j += i }
    }
    val primes = (2..maxPrime).filter { isP[it] }.toIntArray()

    // (p−1) 的素因子分解表（p 为素数）
    fun factor(n: Int): List<Pair<Int, Int>> {
        val out = ArrayList<Pair<Int, Int>>()
        var x = n
        for (q in primes) {
            if (q.toLong() * q > x) break
            if (x % q == 0) {
                var c = 0
                while (x % q == 0) { x /= q; c++ }
                out.add(q to c)
            }
        }
        if (x > 1) out.add(x to 1)
        return out
    }
    val factorP = HashMap<Int, List<Pair<Int, Int>>>()
    // extraCap[q] = Σ_{p ≤ maxPrime, q | p−1} v_q(p−1)，未来 (p'−1) 能补的最大量
    val extraCap = IntArray(maxPrime + 1)
    for (p in primes) {
        val f = factor(p - 1)
        factorP[p] = f
        for ((q, v) in f) if (q <= maxPrime) extraCap[q] += v
    }

    var answer = 0L
    val phiExp = HashMap<Int, Int>()
    val hist = ArrayList<Pair<Int, Int>>()  // 撤销历史（(素数, 旧指数)）

    fun addPhi(p: Int, e: Int): Int {
        val before = hist.size
        if (e >= 2) {
            val old = phiExp[p] ?: 0
            phiExp[p] = old + (e - 1)
            hist.add(p to old)
        }
        for ((q, v) in factorP[p]!!) {
            val old = phiExp[q] ?: 0
            phiExp[q] = old + v
            hist.add(q to old)
        }
        return before
    }

    fun undoPhi(before: Int) {
        while (hist.size > before) {
            val (k, old) = hist.removeAt(hist.size - 1)
            if (old == 0) phiExp.remove(k) else phiExp[k] = old
        }
    }

    fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    fun dfs(pi: Int, cur: Long, expGcd: Int) {
        if (cur > 1 && expGcd == 1) {
            var allGe2 = true
            var phiG = 0
            for ((q, v) in phiExp) {
                if (v < 2) { allGe2 = false; break }
                phiG = gcd(phiG, v)
            }
            if (allGe2 && phiG == 1) answer++
        }
        var j = pi
        while (j < primes.size) {
            val p = primes[j]
            if (cur > limit / p / p) break          // cur·p² 必超限，p 单调增大，直接退出
            var pe = p.toLong() * p                 // p ≤ 1e6 → p² ≤ 1e12，安全
            var e = 2
            while (true) {
                if (cur > limit / pe) break         // 除法判 cur·pe ≤ limit，防溢出
                val nGcd = if (expGcd == 0) e else gcd(expGcd, e)
                val snap = addPhi(p, e)
                var prune = false
                for ((q, v) in phiExp) {
                    if (v < 2 && v + (if (q <= maxPrime) extraCap[q] else 0) < 2) { prune = true; break }
                }
                if (!prune) dfs(j + 1, cur * pe, nGcd)
                undoPhi(snap)
                if (pe > limit / cur / p) break     // 下一步 pe·p 必超限
                pe *= p
                e++
            }
            j++
        }
    }

    val t0 = System.nanoTime()
    dfs(0, 1L, 0)
    val ms = (System.nanoTime() - t0) / 1_000_000.0
    println("ANSWER: $answer  （全量 limit=$limit，耗时 ${"%.1f".format(ms)} ms）")
}
