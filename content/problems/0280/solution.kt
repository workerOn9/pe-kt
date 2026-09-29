#!/usr/bin/env kotlin
/**
 * Project Euler 280 — Ant and Seeds（蚂蚁与种子）
 *
 * 题目：5×5 网格，蚂蚁从中心格出发，每步等概率走到相邻格（不出网格；角 2 个、边 3 个、内部 4 个
 * 选择）。出发时下排 5 个格子各放一粒种子。蚂蚁不携带种子时到达「还有种子的下排格」就拾起该
 * 种子；携带种子时到达「还没有种子的上排格」就把种子放下。求 5 粒种子全部放上上排的期望步数，
 * 保留 6 位小数。
 *
 * 答案编码：期望值是小数，按项目惯例编码为整数 round(答案 × 10^6)（题面要求 6 位小数）。
 *
 * 主路径 A：分层线性方程组（精确求解，无模拟）
 * ────────────────────────────────────────
 * 状态 = (位置 p, 下排剩余种子集合 R, 上排已投放集合 O, 是否携带 c)，满足 |R| + |O| + c = S
 * （S = 种子数 = 5）。期望剩余步数 E 满足 E = 0（R = ∅ 且 c = 0，全部投放完毕）与
 *
 *   E(p, R, O, c) = 1 + (1/deg p) · Σ_{q ~ p} E(next(p, R, O, c, q)).
 *
 * 转移规则（逐条对应题面）：
 *   c = 0 且 q 在下排、其列在 R 中 → 拾起：R ← R \ {列}，c ← 1（位置变成 q）；
 *   c = 1 且 q 在上排、其列不在 O 中 → 放下：O ← O ∪ {列}，c ← 0（位置变成 q）；
 *   其余情况同阶段移动（位置变成 q，R、O、c 不变）。
 * 注意「拾起/放下」是确定性的（触发即发生），这正是方程里没有分支概率的原因。
 *
 * 关键结构：令 level = |R| − |O|。拾起令 |R| 减 1、放下令 |O| 加 1，两者都让 level 恰减 1；
 * 同阶段移动不改变 level。于是阶段 (R, O, c) 构成 DAG，按 level 从低到高求解。同一阶段内只有
 * 位置这一维未知，是 25×25 的线性方程组
 *
 *   (I − Q_{R,O,c}) · E_{R,O,c} = 1 + L · E_{更低层},
 *
 * Q 为阶段内转移矩阵（每行非零项 ≤ 4，各行和为「留在阶段内的概率」），L · E_lower 是泄漏到
 * 已完成阶段的期望值。K = 5、S = 5 时共 462 个阶段（c=0：|R|+|O| = 5 的 252 个；c=1：
 * |R|+|O| = 4 的 210 个），全部用带部分主元的高斯消元求解。
 *
 * 独立复核 B：正向占位测度（伴随方程，换方向 + 换求解器）
 * ────────────────────────────────────────────────────
 * 期望步数 = Σ_s μ(s)，其中 μ(s) 是「过程处于状态 s（即将迈出下一步）」的期望次数。按
 *   μ = inflow + Qᵀ μ   即   μ = Σ_{j≥0} (Qᵀ)^j inflow
 * 用矩阵幂级数迭代求和（不做消元），阶段处理顺序也换成「按 |O| 升序、同 |O| 内先 c=0 后 c=1」。
 * 两条路径在 K=3、1 粒种子、K=5 三种规模上完全一致。
 *
 * 独立复核 C：单程分解 + 宏路径显式枚举（换数学结构）
 * ────────────────────────────────────────────────
 * 过程天然由 10 段「单程」交替组成：拾起段（不携带，从上一个落子格出发，首次踩到某个剩余种子格
 * 结束）与投放段（携带，首次踩到某个空的上排格结束）。对每个「出口集合」预解出：p_s(a) =
 * 从 a 出发恰好从出口 s 离开的概率、f_s(a) = E[步数 · 1{从 s 离开}]，其中
 *   (I−Q)p_s = b_s（b_s(a) = 从 a 一步踩到出口 s 的概率），(I−Q)f_s = p_s
 * （因为 f_s = b_s + Q(p_s + f_s)，而 (I−Q)^{-1}b_s = p_s）。于是
 *
 *   E = Σ_{i=1}^{10} E[第 i 段步数] = Σ_{宏路径前缀 π} P(π) · Σ_s f_s(entry(π)),
 *
 * 也就是把 5!×5! = 14400 条「种子拾取顺序 × 上排投放顺序」宏路径全部显式枚举、逐前缀累加权重
 * 与时长，不经任何反向值函数。
 *
 * 其它校验：5×5 网格上单目标命中时间与 Kemeny–Snell 基本矩阵公式
 * E_a[T_b] = (Z_bb − Z_ab)/π_b（Z = (I − P + Π)^{-1}，π_p = deg p / Σdeg——网格非正则图！）
 * 逐对吻合；平均回转时间 E_a[T_a^+] = 1/π_a = Σdeg / deg(a)（内部 20、边 26.67、角 40）复现；
 * 蒙特卡洛（直接模拟题面规则）给出 95% 置信区间。
 *
 * 复杂度
 * ──────
 * A：462 个阶段 × O(25³) 消元 ≈ 2.4×10^6 flops，毫秒级以内；B：每阶段 Σ(Qᵀ)^j 迭代到尾部
 * < 1e-16，约 10^8 flops；C：64 个出口集合各 1 次 25×25 分解、每个出口再加 2 次回代，
 * 最后是 14400 条宏路径的前缀枚举。
 *
 * 验证
 * ────
 * 1. 结构锚点：网格每个格子的可走步数为 2/3/4（角 4 个、边 12 个、内部 9 个），中心格 = (2,2)，
 *    下排 5 个种子格、上排 5 个投放格；
 * 2. 单目标命中时间 vs Kemeny–Snell 基本矩阵（25×25 对全对拍，最大差 4.97e-13）与平均回转时间；
 * 3. 方法 A / B / C 在 K=3（3 粒种子）、K=5 单粒种子（中心列）、K=5 五粒种子三种规模一致；
 * 4. 蒙特卡洛（同规则直接模拟，200 万样本）均值落在实跑值的 95% 置信区间内；
 * 5. brute-force.kt 用完全独立实现的全状态空间数值解（10270 个可达状态的 Jacobi 值迭代）与
 *    10^8 样本的蒙特卡洛复核同一个值。
 *
 * 答案：期望值 = 430.0882467166…（保留 6 位小数 = 430.088247）⇒ round(×10^6) = 430088247
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0280/solution.kt -d /tmp/kc-0280
 * java -cp /tmp/kc-0280:<kotlin-stdlib> SolutionKt
 */

import java.util.SplittableRandom
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

// ───────────────────────────────── 网格与规则 ─────────────────────────────────

/** 格子编号 p = row * k + col；row = 0 是下排（放种子），row = k − 1 是上排（投放）。 */
private fun neighborsOf(k: Int, p: Int): IntArray {
    val r = p / k
    val c = p % k
    val buf = IntArray(4)
    var n = 0
    if (r > 0) buf[n++] = p - k
    if (r < k - 1) buf[n++] = p + k
    if (c > 0) buf[n++] = p - 1
    if (c < k - 1) buf[n++] = p + 1
    return buf.copyOf(n)
}

/** 预计算邻居表（模拟热路径上不能每次分配数组）。 */
private fun neighborTable(k: Int): Array<IntArray> = Array(k * k) { neighborsOf(k, it) }

private fun bit(x: Int, i: Int): Boolean = (x shr i) and 1 == 1

/** 带部分主元的高斯消元：解 A x = b（A、b 不被修改）。 */
private fun gaussSolve(a0: Array<DoubleArray>, b0: DoubleArray): DoubleArray {
    val n = b0.size
    val a = Array(n) { a0[it].copyOf() }
    val b = b0.copyOf()
    for (col in 0 until n) {
        var piv = col
        for (r in col + 1 until n) if (abs(a[r][col]) > abs(a[piv][col])) piv = r
        check(abs(a[piv][col]) > 1e-12) { "阶段内方程组奇异" }
        if (piv != col) {
            val t = a[piv]; a[piv] = a[col]; a[col] = t
            val tb = b[piv]; b[piv] = b[col]; b[col] = tb
        }
        val pv = a[col][col]
        for (r in col + 1 until n) {
            val f = a[r][col] / pv
            if (f == 0.0) continue
            for (cc in col until n) a[r][cc] -= f * a[col][cc]
            b[r] -= f * b[col]
        }
    }
    val x = DoubleArray(n)
    for (row in n - 1 downTo 0) {
        var s = b[row]
        for (cc in row + 1 until n) s -= a[row][cc] * x[cc]
        x[row] = s / a[row][row]
    }
    return x
}

/**
 * 局部期望值表：value[c][R][O][p]。R 是下排种子列掩码，O 是上排已投放列掩码。
 * 只填 |R| + |O| + c = S 的阶段。
 */
private class ValueTable(val k: Int, val seeds: Int, val start: Int) {
    val n = k * k
    val masks = 1 shl k
    val values = Array(2) { Array(masks) { arrayOfNulls<DoubleArray>(masks) } }
    val center: Int = start

    /** 从阶段 (R, O, c) 的格子 q 迈一步：若跨越到更低层阶段，返回该阶段在 q 处的值；否则 null。 */
    fun leakValue(R: Int, O: Int, c: Int, q: Int): Double? {
        val row = q / k
        val col = q % k
        if (c == 0) {
            if (row == 0 && bit(R, col)) {
                return values[1][R and (1 shl col).inv()][O]!![q]
            }
        } else {
            if (row == k - 1 && !bit(O, col)) {
                return values[0][R][O or (1 shl col)]!![q]
            }
        }
        return null
    }

    /** 求解阶段 (R, O, c) 的 25×25 方程组。 */
    fun solvePhase(R: Int, O: Int, c: Int): DoubleArray {
        val n = this.n
        val a = Array(n) { DoubleArray(n) }
        val b = DoubleArray(n)
        for (p in 0 until n) {
            val nb = neighborsOf(k, p)
            val d = nb.size.toDouble()
            a[p][p] = 1.0
            b[p] = 1.0
            for (q in nb) {
                val leak = leakValue(R, O, c, q)
                if (leak != null) b[p] += leak / d else a[p][q] -= 1.0 / d
            }
        }
        val x = gaussSolve(a, b)
        // 残差自检：直接代回原方程，防止消元出错而无人察觉
        var resid = 0.0
        for (p in 0 until n) {
            val nb = neighborsOf(k, p)
            var rhs = 1.0
            for (q in nb) {
                val leak = leakValue(R, O, c, q)
                rhs += (leak ?: x[q]) / nb.size
            }
            resid = max(resid, abs(x[p] - rhs))
        }
        check(resid < 1e-9) { "阶段 (R=$R,O=$O,c=$c) 残差 $resid 过大" }
        return x
    }
}

/** 方法 A（主路径）：按 level = |R| − |O| 从低到高逐层解 25×25 方程组。 */
private fun solveLayered(k: Int, seeds: Int, start: Int): Double {
    val masks = 1 shl k
    val s = Integer.bitCount(seeds)
    val table = ValueTable(k, seeds, start)

    data class Phase(val r: Int, val o: Int, val c: Int, val level: Int)

    val phases = ArrayList<Phase>()
    for (c in 0..1) {
        for (r in 0 until masks) {
            if (r and seeds.inv() != 0) continue          // R 只能是种子格的子集
            for (o in 0 until masks) {
                if (Integer.bitCount(r) + Integer.bitCount(o) + c != s) continue
                phases.add(Phase(r, o, c, Integer.bitCount(r) - Integer.bitCount(o)))
            }
        }
    }
    phases.sortBy { it.level }                             // 依赖严格在低一层，升序即可解

    for (ph in phases) {
        if (ph.c == 0 && ph.r == 0) {                      // R = ∅ 且不携带 ⇒ 全部投放完毕
            table.values[0][ph.r][ph.o] = DoubleArray(k * k)
            continue
        }
        table.values[ph.c][ph.r][ph.o] = table.solvePhase(ph.r, ph.o, ph.c)
    }
    return table.values[0][seeds][0]!![start]
}

/**
 * 方法 B（独立复核）：正向占位测度 μ(s) = E[处于状态 s 的步数]，E[总步数] = Σ_s μ(s)。
 * 阶段内 μ = Σ_{j≥0}(Qᵀ)^j·inflow，用幂级数迭代（不用消元）；阶段顺序按 |O| 升序、
 * 同 |O| 内先 c=0（拾起段入口）后 c=1（投放段入口）。
 */
private fun solveOccupationMeasure(k: Int, seeds: Int, start: Int): Double {
    val n = k * k
    val masks = 1 shl k
    val s = Integer.bitCount(seeds)
    val nbTable = neighborTable(k)
    val inflow = Array(2) { Array(masks) { Array(masks) { DoubleArray(n) } } }
    inflow[0][seeds][0][start] = 1.0                       // 初始状态记 1 次占位
    var total = 0.0

    for (o in 0 until masks) {
        for (c in 0..1) {
            for (R in 0 until masks) {
                if (R and seeds.inv() != 0) continue
                if (Integer.bitCount(R) + Integer.bitCount(o) + c != s) continue
                if (c == 0 && R == 0) continue             // 终局阶段：过程到此结束，不再计入步数
                val phi = inflow[c][R][o]
                // 预先分好「留在阶段内」与「泄漏出去」的邻居
                val sameNext = Array(n) { IntArray(0) }
                val leakNext = Array(n) { IntArray(0) }
                for (p in 0 until n) {
                    val keep = ArrayList<Int>(4)
                    val leak = ArrayList<Int>(4)
                    for (q in nbTable[p]) {
                        val qr = q / k
                        val qc = q % k
                        val switches = if (c == 0) {
                            qr == 0 && ((R shr qc) and 1) == 1
                        } else {
                            qr == k - 1 && ((o shr qc) and 1) == 0
                        }
                        if (switches) leak.add(q) else keep.add(q)
                    }
                    sameNext[p] = keep.toIntArray()
                    leakNext[p] = leak.toIntArray()
                }
                // μ = Σ_j (Qᵀ)^j φ，用幂级数迭代求和（比消元更「正向」）
                var w = phi.copyOf()
                val mu = DoubleArray(n)
                var iter = 0
                while (true) {
                    var mx = 0.0
                    for (i in 0 until n) {
                        mu[i] += w[i]
                        if (abs(w[i]) > mx) mx = abs(w[i])
                    }
                    if (mx < 1e-16) break
                    val nw = DoubleArray(n)
                    for (p in 0 until n) {
                        val wp = w[p]
                        if (wp == 0.0) continue
                        val share = wp / nbTable[p].size
                        for (q in sameNext[p]) nw[q] += share
                    }
                    w = nw
                    iter++
                    check(iter < 2_000_000) { "方法 B 幂级数不收敛" }
                }
                for (i in 0 until n) total += mu[i]
                // 把泄漏流量推给低层阶段（拾起 → c=1 同 o；放下 → c=0 的 o ∪ {列}）
                for (p in 0 until n) {
                    val muP = mu[p]
                    if (muP == 0.0) continue
                    val share = muP / nbTable[p].size
                    for (q in leakNext[p]) {
                        val qr = q / k
                        val qc = q % k
                        if (c == 0) {
                            inflow[1][R and (1 shl qc).inv()][o][q] += share
                        } else if (qr == k - 1 && !bit(o, qc)) {
                            inflow[0][R][o or (1 shl qc)][q] += share
                        }
                    }
                }
            }
        }
    }
    return total
}

// ─────────────────────── 方法 C：单程分解 + 宏路径显式枚举 ───────────────────────

/**
 * 某个「出口集合」的出口统计：出口集合用 (出口行 row, 掩码 mask) 给出。
 *   pT[a][s] = 从 a 出发、恰好从出口 s 离开的概率；
 *   fT[a][s] = E[步数 · 1{从出口 s 离开}]；
 *   timeT[a] = E[离开所需步数]（= Σ_s fT[a][s]）。
 * 推导：设 Q 为阶段内转移、R = (I − Q)^{-1}。概率 p_s 满足 (I − Q)p_s = b_s（b_s(a) = 从 a
 * 一步踩到 s 的概率）；f_s = E[步数·1{出口 s}] 满足 (I − Q)f_s = p_s。
 */
private class ExitStats(k: Int, exitRow: Int, exitMask: Int) {
    val n = k * k
    val sq = (0 until k).filter { bit(exitMask, it) }.map { exitRow * k + it }
    val pT = Array(n) { DoubleArray(sq.size) }
    val fT = Array(n) { DoubleArray(sq.size) }
    val timeT = DoubleArray(n)

    init {
        val a = Array(n) { DoubleArray(n) }
        for (p in 0 until n) {
            val isExit = sq.contains(p)
            if (isExit) { a[p][p] = 1.0; continue }
            a[p][p] = 1.0
            val nb = neighborsOf(k, p)
            for (q in nb) if (!sq.contains(q)) a[p][q] -= 1.0 / nb.size
        }
        for ((si, sx) in sq.withIndex()) {
            val b = DoubleArray(n)
            for (p in 0 until n) {
                if (sq.contains(p)) continue
                val nb = neighborsOf(k, p)
                if (nb.contains(sx)) b[p] = 1.0 / nb.size
            }
            val p = gaussSolve(a, b)
            val f = gaussSolve(a, p)
            for (q in 0 until n) {
                pT[q][si] = p[q]
                fT[q][si] = f[q]
                timeT[q] += f[q]
            }
        }
        // 自检：非出口格离开概率之和为 1，且 Σ_s f_s = E[离开步数]（溶入下面的方程）
        for (p in 0 until n) {
            if (sq.contains(p)) continue
            var sum = 0.0
            for (si in sq.indices) sum += pT[p][si]
            check(abs(sum - 1.0) < 1e-9) { "出口概率不归一：$sum" }
        }
    }
}

/**
 * 方法 C：把过程看成 10 段单程（拾起/投放交替）。出口集：c=0 时是 R 的种子格（下排），
 * c=1 时是「上排还没投放」的格。用 DFS 显式枚举全部宏路径前缀，逐前缀累加
 *   E[总步数] = Σ_i E[第 i 段步数] = Σ_前缀 P(π) · E[该段步数 | 前缀]。
 */
private fun solveExcursionPaths(k: Int, seeds: Int, start: Int): Double {
    val totalSeeds = Integer.bitCount(seeds)
    val cache = HashMap<Long, ExitStats>()
    fun stats(exitRow: Int, exitMask: Int): ExitStats =
        cache.getOrPut(exitRow.toLong() * 4096 + exitMask) { ExitStats(k, exitRow, exitMask) }

    var total = 0.0
    var nodes = 0L

    fun dfs(c: Int, R: Int, O: Int, entry: Int, weight: Double) {
        nodes++
        val st = if (c == 0) stats(0, R) else stats(k - 1, O.inv() and ((1 shl k) - 1))
        // 本段期望步数（对出口不做条件化）
        total += weight * st.timeT[entry]
        for (si in st.sq.indices) {
            val p = st.pT[entry][si]
            if (p < 1e-14) continue
            val exitSq = st.sq[si]
            val col = exitSq % k
            if (c == 0) {
                val R2 = R and (1 shl col).inv()
                dfs(1, R2, O, exitSq, weight * p)          // 拾起后进入投放段
            } else if (R != 0) {
                dfs(0, R, O or (1 shl col), exitSq, weight * p)  // 放下后进入下一段拾取
            }                                               // R = 0 ⇒ 这是最后一粒，过程结束
        }
    }
    dfs(0, seeds, 0, start, 1.0)
    check(nodes > totalSeeds * totalSeeds) { "宏路径枚举节点数异常：$nodes" }
    return total
}

// ─────────────────────── 命中时间锚点：Kemeny–Snell 基本矩阵 ───────────────────────

/** 单目标命中时间表：hit[a][b] = E_a[T_b]（a ≠ b），由吸收方程组直接解（方法 A 同族机械）。 */
private fun hittingTimesAbsorbing(k: Int): Array<DoubleArray> {
    val n = k * k
    val out = Array(n) { DoubleArray(n) }
    for (b in 0 until n) {
        for (a in 0 until n) {
            if (a == b) continue
            val m = Array(n) { DoubleArray(n) }
            val rhs = DoubleArray(n)
            for (p in 0 until n) {
                if (p == b) { m[p][p] = 1.0; continue }
                m[p][p] = 1.0
                rhs[p] = 1.0
                for (q in neighborsOf(k, p)) if (q != b) m[p][q] -= 1.0 / neighborsOf(k, p).size
            }
            out[a][b] = gaussSolve(m, rhs)[a]
        }
    }
    return out
}

/** Kemeny–Snell：Z = (I − P + Π)^{-1}，π_p = deg(p)/Σdeg（网格非正则！），E_a[T_b] = (Z_bb − Z_ab)/π_b。 */
private fun hittingTimesFundamental(k: Int): Array<DoubleArray> {
    val n = k * k
    val deg = IntArray(n) { neighborsOf(k, it).size }
    val totalDeg = deg.sum().toDouble()
    val a = Array(n) { DoubleArray(n) }
    for (p in 0 until n) {
        for (q in 0 until n) a[p][q] = deg[q] / totalDeg          // Π[p][q] = π_q
        a[p][p] += 1.0
        for (q in neighborsOf(k, p)) a[p][q] -= 1.0 / deg[p]
    }
    // 求逆：逐列解
    val z = Array(n) { DoubleArray(n) }
    for (col in 0 until n) {
        val e = DoubleArray(n)
        e[col] = 1.0
        val x = gaussSolve(a, e)
        for (row in 0 until n) z[row][col] = x[row]
    }
    return Array(n) { p -> DoubleArray(n) { q -> (z[q][q] - z[p][q]) / (deg[q] / totalDeg) } }
}

// ─────────────────────────────── 蒙特卡洛 ───────────────────────────────

/** 直接按题面规则模拟一次，返回总步数（邻居表等预先算好，热路径上零分配）。 */
private fun simulateOnce(
    k: Int,
    seeds: Int,
    start: Int,
    nbTable: Array<IntArray>,
    rowOf: IntArray,
    colOf: IntArray,
    rng: SplittableRandom,
): Long {
    var R = seeds
    var O = 0
    var carrying = false
    var pos = start
    var steps = 0L
    while (true) {
        val nb = nbTable[pos]
        pos = nb[rng.nextInt(nb.size)]
        steps++
        val row = rowOf[pos]
        val col = colOf[pos]
        if (!carrying) {
            if (row == 0 && bit(R, col)) {
                R = R and (1 shl col).inv()
                carrying = true
            }
        } else if (row == k - 1 && !bit(O, col)) {
            O = O or (1 shl col)
            carrying = false
            if (R == 0) return steps                       // 最后一粒种子已放下
        }
    }
}

private class McResult(val mean: Double, val stdErr: Double, val samples: Int)

private fun monteCarlo(k: Int, seeds: Int, start: Int, samples: Int, seed: Long): McResult {
    val nbTable = neighborTable(k)
    val rowOf = IntArray(k * k) { it / k }
    val colOf = IntArray(k * k) { it % k }
    val rng = SplittableRandom(seed)
    var sum = 0.0
    var sumSq = 0.0
    for (i in 0 until samples) {
        val v = simulateOnce(k, seeds, start, nbTable, rowOf, colOf, rng).toDouble()
        sum += v
        sumSq += v * v
    }
    val mean = sum / samples
    val varN = (sumSq / samples - mean * mean).coerceAtLeast(0.0) * samples / (samples - 1)
    return McResult(mean, sqrt(varN / samples), samples)
}

// ─────────────────────────────── 计时与告示 ───────────────────────────────

private fun bestOf3(tag: String, expected: Double, f: () -> Double): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(abs(out - expected) < 1e-9) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

private fun fmt(x: Double): String = "%.12f".format(x)

fun main() {
    val K = 5
    val SEEDS5 = (1 shl K) - 1
    val CENTER5 = (K / 2) * K + K / 2

    // ---------- 1. 结构锚点 ----------
    var deg2 = 0
    var deg3 = 0
    var deg4 = 0
    for (p in 0 until K * K) {
        when (neighborsOf(K, p).size) { 2 -> deg2++; 3 -> deg3++; 4 -> deg4++ }
    }
    check(deg2 == 4 && deg3 == 12 && deg4 == 9) { "可走步数分布应为 4 个角 2 步、12 条边 3 步、9 个内部 4 步" }
    check(CENTER5 == 12 && CENTER5 / K == 2 && CENTER5 % K == 2) { "中心格应为 (2,2)" }
    println("结构锚点：可走步数 2/3/4 的格子数为 $deg2/$deg3/$deg4（题面：2、3 或 4 个选择）；" +
        "中心格 = (2,2)；下排种子格 $K 个、上排投放格 $K 个")

    // ---------- 2. 命中时间：吸收方程 vs Kemeny–Snell 基本矩阵 ----------
    val hitAbs = hittingTimesAbsorbing(K)
    val hitKs = hittingTimesFundamental(K)
    var maxDiff = 0.0
    for (a in 0 until K * K) for (b in 0 until K * K) {
        if (a == b) continue
        maxDiff = max(maxDiff, abs(hitAbs[a][b] - hitKs[a][b]))
    }
    check(maxDiff < 1e-9) { "命中时间两条公式最大差 $maxDiff" }
    // 平均回转时间（Kac）：E_a[T_a^+] = 1 + Σ_q P(a,q)·E_q[T_a] = 1/π_a = Σdeg / deg(a)
    val totalDeg = (0 until K * K).sumOf { neighborsOf(K, it).size }.toDouble()
    var maxReturn = 0.0
    for (a in 0 until K * K) {
        var e = 1.0
        val nb = neighborsOf(K, a)
        for (q in nb) e += hitAbs[q][a] / nb.size
        maxReturn = max(maxReturn, abs(e - totalDeg / nb.size))
    }
    check(maxReturn < 1e-9) { "平均回转时间应恒为 Σdeg/deg(a)，最大偏差 $maxReturn" }
    println("命中时间锚点：25×25 对 E_a[T_b] 与 Kemeny–Snell 公式最大差 ${"%.2e".format(maxDiff)}；" +
        "平均回转时间 E_a[T_a^+] = Σdeg/deg(a)（最大偏差 ${"%.2e".format(maxReturn)}）")

    // ---------- 3. 三种规模上 A / B / C 互证 ----------
    // (a) K = 3、3 粒种子（同规则缩小规模）；(b) K = 5、1 粒种子（中心列）；(c) K = 5、5 粒种子
    val oneSeed = 1 shl (K / 2)
    data class Case(val name: String, val k: Int, val seeds: Int, val start: Int)
    val cases = listOf(
        Case("K=3、3 粒种子", 3, 0b111, 4),
        Case("K=5、1 粒种子（中心列）", 5, oneSeed, CENTER5),
        Case("K=5、5 粒种子（原题）", 5, SEEDS5, CENTER5),
    )
    val answers = HashMap<String, Double>()
    for (case in cases) {
        val a = solveLayered(case.k, case.seeds, case.start)
        val b = solveOccupationMeasure(case.k, case.seeds, case.start)
        val c = solveExcursionPaths(case.k, case.seeds, case.start)
        check(abs(a - b) < 1e-9 && abs(a - c) < 1e-9) {
            "${case.name}：A=$a B=$b C=$c 不一致"
        }
        answers[case.name] = a
        println("${case.name}：方法 A $a，方法 B $b，方法 C $c")
    }
    // 单粒种子还可拆成两个独立命中时间之和（种子格 = 出口单点；上排 = 5 个出口）
    val seedSq = (K / 2)
    val singleSeedCheck = hitAbs[CENTER5][seedSq] + solveExcursionTimeToRow(K, seedSq, K - 1)
    check(abs(singleSeedCheck - answers["K=5、1 粒种子（中心列）"]!!) < 1e-9) {
        "单粒种子 = 命中种子格 + 从种子格命中上排 应为 $singleSeedCheck"
    }
    println("单粒种子分解：E[(2,2)→种子格] + E[种子格→上排] = ${fmt(singleSeedCheck)}（与 A/B/C 一致）")

    val answer = answers["K=5、5 粒种子（原题）"]!!
    val encoded = Math.round(answer * 1e6)

    // ---------- 4. 蒙特卡洛旁证 ----------
    val mcT0 = System.nanoTime()
    val mc = monteCarlo(K, SEEDS5, CENTER5, 2_000_000, 20260929L)
    val mcMs = (System.nanoTime() - mcT0) / 1e6
    val lo = mc.mean - 1.96 * mc.stdErr
    val hi = mc.mean + 1.96 * mc.stdErr
    check(lo <= answer && answer <= hi) { "实跑值 $answer 落在 MC 95%CI [$lo,$hi] 之外" }
    println("蒙特卡洛（200 万样本，直接按题面规则模拟，用时 ${"%.0f".format(mcMs)} ms）：" +
        "均值 ${"%.6f".format(mc.mean)}，95% 置信区间 [${"%.6f".format(lo)}, ${"%.6f".format(hi)}]" +
        "（标准误 ${"%.6f".format(mc.stdErr)}），覆盖实跑值")

    // ---------- 5. 计时 ----------
    solveLayered(K, SEEDS5, CENTER5)
    solveOccupationMeasure(K, SEEDS5, CENTER5)
    solveExcursionPaths(K, SEEDS5, CENTER5)
    val msA = bestOf3("方法 A（分层线性方程组，462 个阶段）", answer) { solveLayered(K, SEEDS5, CENTER5) }
    val msB = bestOf3("方法 B（正向占位测度 + 幂级数，462 个阶段）", answer) {
        solveOccupationMeasure(K, SEEDS5, CENTER5)
    }
    val msC = bestOf3("方法 C（单程分解 + 14400 条宏路径枚举）", answer) { solveExcursionPaths(K, SEEDS5, CENTER5) }

    // ---------- 6. 输出 ----------
    println()
    println("期望步数 = ${fmt(answer)}（原始双精度值）")
    val rounded = Math.round(answer * 1e6)
    println("答案 = $rounded（编码：round(期望值 × 10^6)，目标 6 位小数）")
    println("汇总：A ${"%.3f".format(msA)} ms；B ${"%.3f".format(msB)} ms；C ${"%.3f".format(msC)} ms；" +
        "MC 200 万样本 ${mc.samples} 次")
    println("check() 全部通过")
}

/** 从 a 出发首次到达某一整行的期望步数（吸收方程组；用于单粒种子分解的第二个加项）。 */
private fun solveExcursionTimeToRow(k: Int, a: Int, row: Int): Double {
    val n = k * k
    val m = Array(n) { DoubleArray(n) }
    val rhs = DoubleArray(n)
    for (p in 0 until n) {
        m[p][p] = 1.0
        if (p / k == row) continue
        rhs[p] = 1.0
        for (q in neighborsOf(k, p)) if (q / k != row) m[p][q] -= 1.0 / neighborsOf(k, p).size
    }
    return gaussSolve(m, rhs)[a]
}
