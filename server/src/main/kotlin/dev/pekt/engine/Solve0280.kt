package dev.pekt.engine

/**
 * PE 280 — Ant and Seeds（蚂蚁与种子）：5×5 网格上蚂蚁从中心格出发做简单随机游走，下排 5 格各放一粒
 * 种子；不携带种子时踩到下排「还有种子」的格子就拾起，携带时踩到上排空格就放下。求 5 粒种子全部
 * 投放完毕的期望步数（题面要求 6 位小数）。
 *
 * 编码：期望值是小数，按项目惯例返回 round(期望值 × 10^6) = 430088247。
 *
 * 推导（详见 content/problems/0280/solution.kt 头部与 0280/analysis.md）：
 *   状态 = (位置 p, 剩余种子集合 R ⊆ 下排, 已投放集合 O ⊆ 上排, 是否携带 c)，|R| + |O| + c = 5。
 *   E = 0（R = ∅ 且 c = 0），否则 E(p,R,O,c) = 1 + (1/deg p)·Σ_{q~p} E(next)；
 *   拾起 / 放下都是确定性触发：c = 0 且 q 在下排、列在 R 中 → R 去掉该列、c 变 1；
 *   c = 1 且 q 在上排、列不在 O 中 → O 加上该列、c 变 0（全部投放即终局）。
 *   关键结构：level = |R| − |O| 在拾起与放下时都恰减 1，同阶段移动不变，因此阶段 (R,O,c) 构成
 *   DAG；按 level 从低到高，每个阶段只需解 25 个未知量的方程组 (I − Q)·E = 1 + L·E_更低层
 *   （Q 为阶段内转移矩阵）。K = 5、S = 5 时共 462 个阶段，全部用带部分主元的高斯消元求解，
 *   与 content/problems/0280/solution.kt 的主路径（方法 A）逐行一致。
 *
 * 复杂度：462 个阶段 × O(25³) 消元 ≈ 2.4×10^6 flops，实测 ~2 ms（JIT 预热后），远低于 10 s 熔断线。
 *   校验：题面结构锚点（各格可走步数 2/3/4）通过；独立路径 B（正向占位测度 + 幂级数迭代）与
 *   C（单程分解 + 14400 条宏路径显式枚举）给出同一值 430.0882467166…；Kemeny–Snell 命中时间公式
 *   25×25 对全对拍一致；蒙特卡洛 10^8 样本的 95% 置信区间覆盖该值；brute-force.kt 的全状态空间
 *   （10270 个自洽状态）Jacobi 值迭代给出同一答案。
 *   本题没有素数筛 / 组合数 / gcd 等通用步骤，逻辑自包含，未用到 dev.pekt.math 工具。
 */
internal fun solve0280Impl(): Long {
    val k = 5
    val seeds = (1 shl k) - 1                       // 下排 5 格都有种子
    val start = (k / 2) * k + k / 2                 // 中心格 (2,2) → 12
    val masks = 1 shl k
    val n = k * k
    val s = Integer.bitCount(seeds)

    val values = Array(2) { Array(masks) { arrayOfNulls<DoubleArray>(masks) } }

    fun neighbors(p: Int): IntArray {
        val r = p / k
        val c = p % k
        val buf = IntArray(4)
        var m = 0
        if (r > 0) buf[m++] = p - k
        if (r < k - 1) buf[m++] = p + k
        if (c > 0) buf[m++] = p - 1
        if (c < k - 1) buf[m++] = p + 1
        return buf.copyOf(m)
    }

    /** 阶段 (R,O,c) 从 q 迈一步的泄漏目标值；同阶段移动返回 null。 */
    fun leak(R: Int, O: Int, c: Int, q: Int): Double? {
        val row = q / k
        val col = q % k
        if (c == 0) {
            if (row == 0 && ((R shr col) and 1) == 1) {
                return values[1][R and (1 shl col).inv()][O]!![q]
            }
        } else {
            if (row == k - 1 && ((O shr col) and 1) == 0) {
                return values[0][R][O or (1 shl col)]!![q]
            }
        }
        return null
    }

    // 阶段按 level = |R| − |O| 升序求解（依赖严格在低一层）
    val phases = ArrayList<IntArray>()              // [level, c, R, O]
    for (c in 0..1) {
        for (R in 0 until masks) {
            if (R and seeds.inv() != 0) continue
            for (O in 0 until masks) {
                if (Integer.bitCount(R) + Integer.bitCount(O) + c != s) continue
                phases.add(intArrayOf(Integer.bitCount(R) - Integer.bitCount(O), c, R, O))
            }
        }
    }
    phases.sortBy { it[0] }

    for (ph in phases) {
        val c = ph[1]
        val R = ph[2]
        val O = ph[3]
        if (c == 0 && R == 0) {                     // 终局：全部投放完毕
            values[0][R][O] = DoubleArray(n)
            continue
        }
        // 组装并求解 (I − Q)·E = 1 + 泄漏项
        val a = Array(n) { DoubleArray(n) }
        val b = DoubleArray(n)
        for (p in 0 until n) {
            val nb = neighbors(p)
            val d = nb.size.toDouble()
            a[p][p] = 1.0
            b[p] = 1.0
            for (q in nb) {
                val v = leak(R, O, c, q)
                if (v != null) b[p] += v / d else a[p][q] -= 1.0 / d
            }
        }
        for (col in 0 until n) {                    // 高斯消元（部分主元）
            var piv = col
            for (r in col + 1 until n) if (kotlin.math.abs(a[r][col]) > kotlin.math.abs(a[piv][col])) piv = r
            check(kotlin.math.abs(a[piv][col]) > 1e-12) { "阶段方程组奇异" }
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
            var acc = b[row]
            for (cc in row + 1 until n) acc -= a[row][cc] * x[cc]
            x[row] = acc / a[row][row]
        }
        values[c][R][O] = x
    }

    val expected = values[0][seeds][0]!![start]
    return Math.round(expected * 1_000_000.0)       // 编码：round(期望值 × 10^6)
}
