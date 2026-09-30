#!/usr/bin/env kotlin
/**
 * Project Euler 300 — Protein Folding（蛋白质折叠）
 *
 * 题目：n 位 H/P 串（2^n 个，等概率）折成二维方格上的自回避行走（n 个格点、n−1 步），
 * 「折叠后位于相邻格点的一对 H」记一个 H-H 接触点；每个串取接触数最大的折叠，
 * 求 n = 15 时全体串的平均值（精确小数）。题面样例：n = 8 时平均 = 850/256 = 3.3203125。
 *
 * 计数约定（先核对再求解）
 * ──────────────────────
 * 接触点 = 折叠后格点相邻的两个 H 元素，**包括序列上相邻的骨架对**：
 * 序列相邻的两元素在任何折叠下都格点相邻，题面的 850/256 只有计入骨架对才成立。
 * （只数非骨架对的话，n = 8 时 8 个格点最多 10 条相邻边、减去 7 条骨架边，每个串至多 3 个
 * 接触，平均不可能达到 3.32；main 第 1 节会把两种约定都算出来对照。）
 * 把串的接触数写成
 *     contact(s, f) = A(s, f) + B(s)，
 * 其中 A 数非骨架对（|i−j| ≥ 2），B(s) = 串中相邻 HH 对的个数（与折叠无关的常数）。于是
 *     opt(s) = (max_f A(s, f)) + B(s)，
 * 求解只需在「非骨架接触掩码」上取最大值。
 *
 * 算法
 * ────
 * 1. 折叠空间 = 15 个格点 + 14 步自回避行走。平移/旋转不改变接触结构，故固定首步向东
 *    （吃掉 4 个旋转），枚举 2374444/4 = 593611 条行走（2374444 是 14 步方格自回避行走数，
 *    OEIS A001411）；反射对称不另外规范化，由掩码去重顺带处理。
 * 2. 每条折叠算一个「非骨架接触掩码」：位编号取遍 (i, j)（i < j, j ≥ i+2）共 91 对。
 *    掩码很稀疏：非骨架接触数 = 占据格点的相邻边数 − 14 ≤ 22 − 14 = 8（15 格点最紧凑的
 *    3×5 矩形有 22 条相邻边），所以至多 8 位，升序打包进一个 Long（每位 7 bit、高 8 bit
 *    存位数）当键，排序去重，得到规模远小于行走数的掩码集合。
 * 3. 逐串求最优：对每个串构造它的 H 对掩码，按 popcount 从大到小扫掩码桶（score ≤ popcount，
 *    故 best 追上当前桶的 popcount 后其余掩码都不可能更好）；串自身的理论上界
 *    cap = min(C(h,2) − B(s), h + ⌊e/2⌋)（h 个 H、其中 e 个落在端点上：内部 H 有 2 个自由邻位、
 *    端点的 H 有 3 个，故接触数 ≤ (2h+e)/2）用于「达到即停」——注意上界只能用来提前停，
 *    不能用来跳过 popcount 更高的桶（更高的桶里可能有得分恰好等于上界的掩码）。
 *    另外，倒着读的串与正着读的串最优值相同（walk 倒着走仍是自回避行走）：只扫 s ≤ reverse(s)
 *    的串（回文权重 1、其余 2），启用前逐键验证掩码集合在反转下封闭。
 * 4. 平均值 S/2^15 的分母是 2 的幂，小数有限：S/2^15 = S·5^15/10^15，用 BigInteger 精确展开。
 *
 * 复杂度
 * ──────
 * 折叠枚举 O(593611 × 14) 次格点操作；逐串扫描 O(2^14 × 桶 8、桶 7… 到 best 为止)，
 * 其中掩码规模与桶分布见运行输出；实测毫秒数见 main 的计时节。
 *
 * 运行
 * ────
 * OUTDIR=/tmp/kc-300 bash scripts/kotlinc-shim.sh content/problems/0300/solution.kt
 * java -Xmx2g -cp /tmp/kc-300:<kotlin-stdlib> SolutionKt
 */

import java.math.BigInteger

private val DX = intArrayOf(1, -1, 0, 0)
private val DY = intArrayOf(0, 0, 1, -1)

private const val PACK_SHIFT = 56    // 键：低 56 bit 放最多 8 个 7 bit 位编号，高 8 bit 放位数

// ═══════════════════════ 1. 规范化折叠枚举 → 接触掩码集合 ═══════════════════════

/** 位编号：(i, j)（i < j, j ≥ i+2）按字典序编号。 */
private fun pairBit(n: Int, i: Int, j: Int): Int = (i * (2 * n - i - 3)) / 2 + (j - i - 2)

/**
 * 规范化折叠枚举器：首步固定向东（消除整体旋转），DFS 生成全部自回避行走，
 * 每条行走产出一个「非骨架接触掩码」键（升序位编号打包）。
 */
private class Foldings(private val n: Int) {
    private val w = 2 * n + 1
    private val off = n
    private val grid = IntArray(w * w) { -1 }        // 格点 -> 站点编号，-1 空
    private val bitsBuf = IntArray(16)               // 本行走收集到的接触位（压栈追加）
    private val packBuf = IntArray(16)               // push 时复制一份再排序，避免破坏 DFS 共享前缀
    private var keys = LongArray(if (n <= 2) 1 else 1 shl 15)
    private var keyCount = 0

    var walkCount = 0L
        private set

    init {
        if (n < 2) {
            keys[0] = 0L                      // 单元素串：一条 0 步行走、0 个接触
            keyCount = 1
            walkCount = 1L
        } else {
            grid[off * w + off] = 0
            grid[(off + 1) * w + off] = 1     // 首步向东：站点 1 在 (1, 0)
            dfs(2, 1, 0, 0)
        }
    }

    /** 走完一条行走：把接触位复制出来升序排序后打包成键（升序位编号 = 规范形式）。 */
    private fun push(bits: Int) {
        for (a in 0 until bits) packBuf[a] = bitsBuf[a]
        for (a in 1 until bits) {
            val v = packBuf[a]
            var b = a - 1
            while (b >= 0 && packBuf[b] > v) {
                packBuf[b + 1] = packBuf[b]
                b--
            }
            packBuf[b + 1] = v
        }
        var acc = 0L
        for (a in 0 until bits) acc = (acc shl 7) or packBuf[a].toLong()
        if (keyCount == keys.size) keys = keys.copyOf(keys.size * 2)
        keys[keyCount++] = (bits.toLong() shl PACK_SHIFT) or acc
        walkCount++
    }

    private fun dfs(step: Int, x: Int, y: Int, bits: Int) {
        if (step == n) {
            push(bits)
            return
        }
        for (d in 0 until 4) {
            val nx = x + DX[d]
            val ny = y + DY[d]
            if (grid[(nx + off) * w + (ny + off)] >= 0) continue
            // 新站点的四邻里，除骨架邻居（站点 step-1）外的已访问站点都是接触
            var cnt = bits
            for (e in 0 until 4) {
                val j = grid[(nx + DX[e] + off) * w + (ny + DY[e] + off)]
                if (j >= 0 && step - j >= 2) bitsBuf[cnt++] = pairBit(n, j, step)
            }
            check(cnt <= 8) { "非骨架接触数超过 8，打包键溢出" }
            grid[(nx + off) * w + (ny + off)] = step
            dfs(step + 1, nx, ny, cnt)
            grid[(nx + off) * w + (ny + off)] = -1
            // bitsBuf 无需回滚：压栈的下一分支会从 bits 处覆盖（push 的排序只置换前缀集合）
        }
    }

    /** 排序去重后的掩码键。 */
    fun distinctKeys(): LongArray {
        val arr = keys.copyOf(keyCount)
        arr.sort()
        var m = 0
        for (i in arr.indices) if (i == 0 || arr[i] != arr[i - 1]) arr[m++] = arr[i]
        return arr.copyOf(m)
    }
}

/** 把掩码键展开成两个 Long（91 位）+ popcount 分桶，供逐串求最优扫描。 */
private class MaskLibrary(keys: LongArray) {
    val m0 = LongArray(keys.size)
    val m1 = LongArray(keys.size)
    val pc = IntArray(keys.size)
    var maxPc = 0
        private set

    init {
        for (idx in keys.indices) {
            val k = keys[idx]
            val bits = (k ushr PACK_SHIFT).toInt()
            val packed = k and ((1L shl PACK_SHIFT) - 1)
            var a0 = 0L
            var a1 = 0L
            for (a in 0 until bits) {
                val bit = ((packed ushr (7 * (bits - 1 - a))) and 0x7F).toInt()
                if (bit < 64) a0 = a0 or (1L shl bit) else a1 = a1 or (1L shl (bit - 64))
            }
            m0[idx] = a0
            m1[idx] = a1
            pc[idx] = bits
            check(java.lang.Long.bitCount(a0) + java.lang.Long.bitCount(a1) == bits) { "掩码键往返不一致" }
            if (bits > maxPc) maxPc = bits
        }
    }

    val buckets: Array<IntArray> by lazy {
        val lists = Array(maxPc + 1) { ArrayList<Int>() }
        for (idx in pc.indices) lists[pc[idx]].add(idx)
        Array(maxPc + 1) { p -> lists[p].toIntArray() }
    }
}

// ═══════════════════════ 2. 逐串求最优（主路径） ═══════════════════════

/** 位编号 -> (i, j)（与 pairBit 互逆）。 */
private fun pairOf(n: Int, bit: Int): IntArray {
    var rest = bit
    for (i in 0 until n) {
        val cnt = n - i - 2
        if (rest < cnt) return intArrayOf(i, i + 2 + rest)
        rest -= cnt
    }
    error("位编号越界：$bit")
}

/**
 * 掩码键在「串反转」下的像：i ↦ n−1−i，对 (i,j) ↦ (n−1−j, n−1−i)。
 * 折叠集合对反转封闭（一条行走倒着走还是自回避行走，接触对随位置反转），这是逐串扫描
 * 只跑一半串的合法性依据。
 */
private fun reversalMappedKey(n: Int, key: Long): Long {
    val bits = (key ushr PACK_SHIFT).toInt()
    val packed = key and ((1L shl PACK_SHIFT) - 1)
    val mapped = IntArray(bits)
    for (a in 0 until bits) {
        val b = ((packed ushr (7 * (bits - 1 - a))) and 0x7F).toInt()
        val p = pairOf(n, b)
        mapped[a] = pairBit(n, n - 1 - p[1], n - 1 - p[0])
    }
    mapped.sort()
    var acc = 0L
    for (v in mapped) acc = (acc shl 7) or v.toLong()
    return (bits.toLong() shl PACK_SHIFT) or acc
}

/** 掩码集合在串反转下是否封闭（对称性剪枝的前提，逐键显式验证）。 */
private fun verifyReversalClosure(n: Int, keys: LongArray): Boolean {
    val set = HashSet<Long>(keys.size * 2)
    for (k in keys) set.add(k)
    for (k in keys) if (reversalMappedKey(n, k) !in set) {
        println("    反转不封闭的掩码：$k -> ${reversalMappedKey(n, k)}")
        return false
    }
    return true
}

/**
 * 返回 LongArray(sumA, sumB)：
 *   sumA = Σ_s max_f A(s, f)（只数非骨架接触），sumB = Σ_s opt(s)（按题面约定，含骨架对）。
 * 利用串反转对称 opt(s) = opt(reverse(s))：只对 s ≤ reverse(s) 的串求解，权重 1（回文）或 2。
 */
private fun optimalContactSums(n: Int, lib: MaskLibrary): LongArray {
    val total = 1 shl n
    val m0 = lib.m0
    val m1 = lib.m1
    val buckets = lib.buckets
    var sumA = 0L
    var sumB = 0L
    for (s in 0 until total) {
        val rev = Integer.reverse(s) ushr (32 - n)
        if (s > rev) continue                       // 与反转后的串同值，交给代表元
        val weight = if (s == rev) 1L else 2L
        // H 对掩码：两端都是 H 的非骨架对（不能只按 H 位置 OR 起点掩码——那会把 P 位置也算进来）
        var hm0 = 0L
        var hm1 = 0L
        var h = 0
        var i = 0
        while (i < n) {
            if ((s shr i) and 1 == 1) {
                h++
                for (j in i + 2 until n) {
                    if ((s shr j) and 1 == 1) {
                        val bit = pairBit(n, i, j)
                        if (bit < 64) hm0 = hm0 or (1L shl bit) else hm1 = hm1 or (1L shl (bit - 64))
                    }
                }
            }
            i++
        }
        val backboneHH = Integer.bitCount(s and (s shr 1))          // B(s)
        val ends = (s and 1) + ((s shr (n - 1)) and 1)              // 端点上的 H 个数
        val ub1 = h * (h - 1) / 2 - backboneHH                      // 非骨架 H 对总数
        val ub2 = h + ends / 2                                      // 自由邻位（内部 2、端点 3）÷ 2
        val cap = minOf(ub1, ub2)                                   // 该串理论上界
        var best = 0
        var p = lib.maxPc
        while (cap > 0 && p > best) {
            val arr = buckets[p]
            for (idx in arr) {
                val v = java.lang.Long.bitCount(m0[idx] and hm0) + java.lang.Long.bitCount(m1[idx] and hm1)
                if (v > best) {
                    best = v
                    if (best == p) break                // 本桶已满分，后续掩码不可能更好
                }
            }
            if (best >= cap) break                      // 达到该串的理论上界
            p--
        }
        sumA += weight * best
        sumB += weight * (best + backboneHH)
    }
    return longArrayOf(sumA, sumB)
}

/** 单个串的最优接触数（约定 B），直接扫全部掩码（用于题面插图串核对）。 */
private fun singleStringOptimum(n: Int, lib: MaskLibrary, s: Int): Int {
    var hm0 = 0L
    var hm1 = 0L
    for (i in 0 until n) if ((s shr i) and 1 == 1) for (j in i + 2 until n) {
        if ((s shr j) and 1 == 1) {
            val bit = pairBit(n, i, j)
            if (bit < 64) hm0 = hm0 or (1L shl bit) else hm1 = hm1 or (1L shl (bit - 64))
        }
    }
    var best = 0
    for (idx in lib.pc.indices) {
        val v = java.lang.Long.bitCount(lib.m0[idx] and hm0) + java.lang.Long.bitCount(lib.m1[idx] and hm1)
        if (v > best) best = v
    }
    return best + Integer.bitCount(s and (s shr 1))
}

// ═══════════════════════ 3. 朴素独立实现（小 n 对拍） ═══════════════════════

/**
 * 逐条枚举全部 4^(n−1) 条行走（不做任何对称规范化、不去重），每条行走的接触掩码
 * 直接对全部 2^n 个串打分。n ≤ 11（位编号 ≤ 45，单 Long 够放）时可用，作为独立对照。
 */
private fun naiveContactSums(n: Int): LongArray {
    val bitTotal = n * (n - 1) / 2 - (n - 1)
    check(bitTotal <= 62) { "朴素路径仅支持 n ≤ 11" }
    val total = 1 shl n
    val hmA = LongArray(total)
    for (s in 0 until total) {
        var m = 0L
        for (i in 0 until n) if ((s shr i) and 1 == 1) for (j in i + 2 until n) {
            if ((s shr j) and 1 == 1) m = m or (1L shl pairBit(n, i, j))
        }
        hmA[s] = m
    }
    val bestA = LongArray(total)
    val off = n
    val w = 2 * n + 1
    val grid = IntArray(w * w) { -1 }
    if (n == 1) {
        return longArrayOf(0L, 0L)
    }
    fun walk(step: Int, x: Int, y: Int, mask: Long) {
        if (step == n) {
            for (s in 0 until total) {
                val v = java.lang.Long.bitCount(mask and hmA[s]).toLong()
                if (v > bestA[s]) bestA[s] = v
            }
            return
        }
        for (d in 0 until 4) {
            val nx = x + DX[d]
            val ny = y + DY[d]
            if (grid[(nx + off) * w + (ny + off)] >= 0) continue
            var add = 0L
            for (e in 0 until 4) {
                val j = grid[(nx + DX[e] + off) * w + (ny + DY[e] + off)]
                if (j >= 0 && step - j >= 2) add = add or (1L shl pairBit(n, j, step))
            }
            grid[(nx + off) * w + (ny + off)] = step
            walk(step + 1, nx, ny, mask or add)
            grid[(nx + off) * w + (ny + off)] = -1
        }
    }
    grid[off * w + off] = 0
    walk(1, 0, 0, 0L)
    var sumA = 0L
    var sumB = 0L
    for (s in 0 until total) {
        sumA += bestA[s]
        sumB += bestA[s] + Integer.bitCount(s and (s shr 1))
    }
    return longArrayOf(sumA, sumB)
}

// ═══════════════════════ 4. 精确小数与工具 ═══════════════════════

/** sum / 2^exp 的精确十进制（分母是 2 的幂，小数有限；末尾 0 去掉）。 */
private fun exactDecimal(sum: Long, exp: Int): String {
    val num = BigInteger.valueOf(sum).multiply(BigInteger.valueOf(5).pow(exp))
    val digits = num.toString()
    val intPart = if (digits.length > exp) digits.substring(0, digits.length - exp) else "0"
    var frac = if (digits.length > exp) digits.substring(digits.length - exp)
    else "0".repeat(exp - digits.length) + digits
    frac = frac.trimEnd('0')
    return if (frac.isEmpty()) intPart else "$intPart.$frac"
}

/** H/P 串（低位 = 第 0 位）打印：H 为 'H'，P 为 'P'。 */
private fun string(s: Int, n: Int): String = buildString { for (i in 0 until n) append(if ((s shr i) and 1 == 1) 'H' else 'P') }

private fun bestOf3(tag: String, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { r ->
        val t0 = System.nanoTime()
        val v = f()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
        println("  第 ${r + 1} 轮：${"%.1f".format(ms)} ms（校验量 $v）")
    }
    println("  $tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

// ═══════════════════════ main ═══════════════════════

/** 一次完整求解的结果（供各节复用与打印）。 */
private class Run(
    val walkCount: Long,
    val maskCount: Int,
    val maxPc: Int,
    val bucketSizes: List<Int>,
    val closureOk: Boolean,
    val sums: LongArray,
    val prepMs: Double,
    val scanMs: Double,
)

/** 完整求解 n 位串：规范化折叠枚举 → 掩码去重 → 反转封闭性核对 → 逐串求最优。 */
private fun solve(n: Int): Run {
    val t0 = System.nanoTime()
    val f = Foldings(n)
    val keys = f.distinctKeys()
    val closure = verifyReversalClosure(n, keys)
    check(closure) { "掩码集合在序列反转下不封闭，对称性剪枝不合法" }
    val lib = MaskLibrary(keys)
    val t1 = System.nanoTime()
    val sums = optimalContactSums(n, lib)
    val t2 = System.nanoTime()
    return Run(
        f.walkCount, lib.pc.size, lib.maxPc, (0..lib.maxPc).map { lib.buckets[it].size }, closure, sums,
        (t1 - t0) / 1e6, (t2 - t1) / 1e6,
    )
}

fun main() {
    println("== 1. 计数约定核对：题面 n = 8 样例 ==")
    run {
        val r = solve(8)
        val den = 1L shl 8
        println("  规范化折叠 ${r.walkCount} 条 → 去重掩码 ${r.maskCount} 个（最大 popcount ${r.maxPc}）")
        println("  约定 A（只数非骨架对）：${r.sums[0]}/$den = ${exactDecimal(r.sums[0], 8)}")
        println("  约定 B（数全部相邻 H-H 对，含骨架）：${r.sums[1]}/$den = ${exactDecimal(r.sums[1], 8)}")
        check(r.sums[1] == 850L) { "n = 8 与题面 850/256 不符（约定 B）" }
        check(r.sums[0] != 850L)
        println("  ✓ 约定 B 得 850/256 = ${exactDecimal(850, 8)}，与题面一致；约定 A 不成立（非骨架接触数 ≤ 8 格点相邻边 10 − 骨架 7 = 3）")
    }

    println()
    println("== 2. 题面插图串核对：HHPPHHHPHHPH（12 元素）最优 = 9 ==")
    run {
        val n = 12
        var bits = 0
        for (i in 0 until n) if ("HHPPHHHPHHPH"[i] == 'H') bits = bits or (1 shl i)
        println("  串 ${Integer.bitCount(bits)} 个 H：${string(bits, n)}")
        val t0 = System.nanoTime()
        val lib = MaskLibrary(Foldings(n).distinctKeys())
        val msFold = (System.nanoTime() - t0) / 1e6
        val opt = singleStringOptimum(n, lib, bits)
        println("  最优接触数 = $opt（去重后掩码 ${lib.pc.size} 个，枚举耗时 ${"%.0f".format(msFold)} ms）")
        check(opt == 9) { "题面说该串最优为 9，实得 $opt" }
        println("  ✓ 与题面「the right folding has nine H-H contact points, which is optimal」一致")
    }

    println()
    println("== 3. 小 n 全表对拍：掩码去重法（｜含反转对称剪枝）vs 朴素 4^(n−1) 枚举 ==")
    for (n in 1..8) {
        val r = solve(n)
        val naive = naiveContactSums(n)
        check(r.sums[0] == naive[0]) { "n = $n：非骨架和 ${r.sums[0]} ≠ 朴素 ${naive[0]}" }
        check(r.sums[1] == naive[1]) { "n = $n：约定 B 和 ${r.sums[1]} ≠ 朴素 ${naive[1]}" }
        println(
            "  n = $n：Σopt = ${r.sums[1]}（掩码 ${r.maskCount} 个 / 行走 ${r.walkCount} 条），" +
                "平均 = ${exactDecimal(r.sums[1], n)}（= ${r.sums[1]}/${1 shl n}），与朴素枚举一致"
        )
    }

    println()
    println("== 4. n = 15 求解 ==")
    val N = 15
    val r = solve(N)
    println("  规范化折叠（首步向东）${r.walkCount} 条 = 全部 14 步自回避行走 2374444 条的 1/4（旋转等价）")
    println("  去重后非骨架接触掩码 ${r.maskCount} 个（压缩率 ${"%.1f".format(100.0 * r.maskCount / r.walkCount)}%）")
    println("  掩码按接触数分布：" + r.bucketSizes.withIndex().joinToString(", ") { (p, c) -> "$p:$c" })
    println("  掩码集合在序列反转下封闭：${r.closureOk}（逐键验证，故可只扫一半的串）")
    println("  Σ_s opt(s) = ${r.sums[1]} ；Σ_s max_f A(s,f) = ${r.sums[0]}（差 = Σ_s B(s) = ${r.sums[1] - r.sums[0]}）")
    println("  平均 = ${r.sums[1]}/2^15 = ${exactDecimal(r.sums[1], N)}")
    println("  单轮全流程 ${"%.1f".format(r.prepMs + r.scanMs)} ms（枚举+去重+封闭核对 ${"%.1f".format(r.prepMs)} ms，逐串扫描 ${"%.1f".format(r.scanMs)} ms）")

    println()
    println("== 5. 计时（best-of-3，JIT 预热后）==")
    val msOpt = bestOf3("主路径：折叠枚举 + 掩码去重 + 逐串扫描（n = 15）") { solve(N).sums[1] }
    val msNaive = bestOf3("朴素路径：4^(n−1) 枚举（n = 8，对拍用）") { naiveContactSums(8)[1] }
    println()
    println("== 6. 结果 ==")
    println("n = 15 时全体 2^15 个 H/P 串最优折叠的平均 H-H 接触数 = ${exactDecimal(r.sums[1], N)}")
    println("（= ${r.sums[1]}/32768，精确小数，无四舍五入；主路径 ${"%.3f".format(msOpt)} ms，朴素 n = 8 ${"%.3f".format(msNaive)} ms）")
}
