#!/usr/bin/env kotlin
/**
 * Project Euler 252 — Convex Holes（凸空穴）
 *
 * 思路：
 *   给定点集 P，求以 P 中点为顶点、内部不含 P 中任何点的最大面积凸多边形
 *   （边界上允许有给定点）。全程用「双倍面积」（Shoelace 整数值）避免浮点。
 *
 *   规范锚点：任一凸空穴取其字典序最小顶点（先 y 最小、再 x 最小）记为 p，
 *   其余顶点都在 p 的上方（或同一水平线右侧）。以 p 为扇心做三角剖分：
 *   顶点按绕 p 的极角严格递增记为 v1,…,vk，则
 *     · 每个扇三角形 (p, v_t, v_{t+1}) 内部不能有 P 中的点；
 *     · 每条对角线 (p, v_t)（2 ≤ t ≤ k−1）上不能有 P 中的点
 *       —— 对角线严格位于多边形内部；多边形边界（含首尾两条边 (p,v1)、(p,vk)）
 *          上有点是允许的。
 *   多边形双倍面积 = Σ cross(v_t − p, v_{t+1} − p)（每项为正）。
 *
 *   DP：F[b][c] = 以凸链 p→…→b→c 结尾时的最大双倍面积。
 *     · 种子（三角形 p,b,c）：F = cross(b−p, c−p)，要求该三角形内部无点；
 *     · 转移（四边形及以上）：F[b][c] = F[a][b] + cross(b−p, c−p)，要求
 *         orient(a,b,c) > 0（在 b 处左转）、segEmpty(b)、三角形 (p,b,c) 内部无点；
 *     · 闭合：orient(b,c,p) > 0 时 F[b][c] 即一个完整凸空穴的双倍面积。
 *
 *   效率关键是两处判空的线性化（总复杂度 O(n³)，n = 500）：
 *     1) 「三角形 (p,b,c) 内部无点」：夹在极角 (θ_b, θ_c) 之间的点 q 必落在直线 pb
 *        左侧的开半平面内，从 b 看其方向都在同一个开半圆里；此时
 *        q 严格在三角形内 ⟺ q 比 c 更逆时针（cross(q−b, c−b) > 0）。
 *        于是只需维护前述方向最逆时针的点 m，c 合法 ⟺ cross(m−b, c−b) ≥ 0。
 *        与 p 共线（同极角）的点单独分组、整组折叠，避免把边界点误判为内部点。
 *     2) 转移里 max over a 的约束 orient(a,b,c) > 0：固定 b 时，把 a、c 都按
 *        「从 b 看去的方向」排序，合法 a 恰是满足 φ_a < φ_c + π 的前缀；c 按方向
 *        递增扫过时该前缀单调增长，双指针滑窗即可 O(1) 均摊取前缀最大值。
 *        每个点作为中心的方向序只预处理一次（全局 O(n² log n)），扫描时过滤候选点。
 *
 * 旁证：
 *   · 题面 20 点样例：本程序给出双倍面积 2099389 = 1049694.5，与题面一致；
 *   · brute-force.kt（Jarvis 步进法 + 射线法，与解法不共享核心代码）枚举 2^20 个子集，
 *     独立复现 1049694.5；solution.kt 内另用全子集暴力对 60 组随机小点集（n = 8…12）验证；
 *   · 结构不同的朴素 DP（显式三重循环 + 逐点扫描判空）在样例与前 50/100/150/200 点子集上
 *     与最优路径给出同一数值；
 *   · 重建取到最大值的 9 边形，独立复核「严格凸 + 内部无 P 中点 + 鞋带面积一致」；
 *   · 点集旋转 180°（(x,y) ↦ (−x,−y)）重跑，答案不变；
 *   · 公开旁证：freeCodeCamp 的 PE 题解对第 252 题断言 convexHoles() === 104924。
 *
 * 答案：104924.0（双倍面积 209848）；仓库编码 round(area×10^8) = 双倍面积 × 5×10^7 = 10492400000000
 * 复杂度：O(n³) 时间、O(n²) 空间；n = 500 本机 JIT 预热后约 0.6 s。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

// =====================================================================
// 1. 点集生成（题面伪随机序列）
// =====================================================================

private const val GEN_MOD = 50_515_093L

private fun generateRawPoints(count: Int): Pair<IntArray, IntArray> {
    val xs = IntArray(count)
    val ys = IntArray(count)
    var s = 290797L
    for (k in 0 until count) {
        s = s * s % GEN_MOD
        xs[k] = (s % 2000L).toInt() - 1000
        s = s * s % GEN_MOD
        ys[k] = (s % 2000L).toInt() - 1000
    }
    return Pair(xs, ys)
}

private fun dedupePoints(xs: IntArray, ys: IntArray): Pair<IntArray, IntArray> {
    val seen = HashSet<Long>()
    val ox = IntArray(xs.size)
    val oy = IntArray(xs.size)
    var k = 0
    for (i in xs.indices) {
        val key = xs[i].toLong() shl 32 or (ys[i].toLong() and 0xFFFFFFFFL)
        if (seen.add(key)) {
            ox[k] = xs[i]
            oy[k] = ys[i]
            k++
        }
    }
    return Pair(ox.copyOf(k), oy.copyOf(k))
}

// =====================================================================
// 2. 快速解法：按字典序最小顶点做扇形剖分 + 凸链 DP（O(n³)）
// =====================================================================

private class FastSolver(
    private val n: Int,
    private val xs: IntArray,
    private val ys: IntArray,
) {
    /** dirOrder[c]：其余点按绕 c 的极角升序排列 */
    private val dirOrder = Array(n) { IntArray(if (n > 1) n - 1 else 0) }

    /** dirPos[c][x]：x 在 dirOrder[c] 中的位置（x = c 时为 −1） */
    private val dirPos = Array(n) { IntArray(n) }

    private val rank = IntArray(n) { -1 }
    private val dp = LongArray(n * n)
    private val pred = IntArray(n * n)
    private val cand = IntArray(n)
    private val aList = IntArray(n)
    private val cList = IntArray(n)
    private val triOk = BooleanArray(n)
    private val segEmpty = BooleanArray(n)
    private val mergeBuf = IntArray(n)

    var bestArea = 0L
        private set
    private var bestP = -1
    private var bestB = -1
    private var bestC = -1

    init {
        for (c in 0 until n) {
            val ord = dirOrder[c]
            var k = 0
            for (x in 0 until n) if (x != c) ord[k++] = x
            sortByAngle(ord, k, xs[c], ys[c], false)
            dirPos[c][c] = -1
            for (i in 0 until k) dirPos[c][ord[i]] = i
        }
    }

    // ---------------------------------------------------------------
    // 排序工具（精确整数比较，不用浮点）
    // ---------------------------------------------------------------

    private fun lessIdx(a: Int, b: Int, cx: Int, cy: Int, tieDist: Boolean): Boolean {
        val ax = xs[a] - cx
        val ay = ys[a] - cy
        val bx = xs[b] - cx
        val by = ys[b] - cy
        val ha = if (ay > 0 || (ay == 0 && ax > 0)) 0 else 1
        val hb = if (by > 0 || (by == 0 && bx > 0)) 0 else 1
        if (ha != hb) return ha < hb
        val cr = ax.toLong() * by - ay.toLong() * bx
        if (cr != 0L) return cr > 0L
        if (!tieDist) return a < b
        val da = ax.toLong() * ax + ay.toLong() * ay
        val db = bx.toLong() * bx + by.toLong() * by
        if (da != db) return da < db
        return a < b
    }

    private fun sortByAngle(a: IntArray, count: Int, cx: Int, cy: Int, tieDist: Boolean) {
        if (count < 2) return
        mergeSort(a, 0, count, cx, cy, tieDist)
    }

    private fun mergeSort(a: IntArray, lo: Int, hi: Int, cx: Int, cy: Int, tieDist: Boolean) {
        if (hi - lo < 2) return
        val mid = (lo + hi) ushr 1
        mergeSort(a, lo, mid, cx, cy, tieDist)
        mergeSort(a, mid, hi, cx, cy, tieDist)
        var i = lo
        var j = mid
        var k = lo
        while (i < mid && j < hi) {
            mergeBuf[k++] = if (lessIdx(a[i], a[j], cx, cy, tieDist)) a[i++] else a[j++]
        }
        while (i < mid) mergeBuf[k++] = a[i++]
        while (j < hi) mergeBuf[k++] = a[j++]
        for (t in lo until hi) a[t] = mergeBuf[t]
    }

    // ---------------------------------------------------------------
    // 主流程
    // ---------------------------------------------------------------

    fun solve(): Long {
        bestArea = 0L
        for (p in 0 until n) solveAnchor(p)
        return bestArea
    }

    private fun solveAnchor(p: Int) {
        val px = xs[p]
        val py = ys[p]
        rank[p] = -1
        var m = 0
        for (x in 0 until n) {
            if (x == p) continue
            val dy = ys[x] - py
            if (dy > 0 || (dy == 0 && xs[x] > px)) cand[m++] = x
        }
        if (m < 2) return
        // 候选点按绕 p 的极角排序（同角按距离升序）
        sortByAngle(cand, m, px, py, true)
        for (i in 0 until m) rank[cand[i]] = i

        // segEmpty[b]：线段 (p,b) 内部无候选点（排序后看前驱是否同角）
        for (i in 0 until m) {
            val c = cand[i]
            segEmpty[c] = if (i == 0) true else {
                val b = cand[i - 1]
                (xs[b] - px).toLong() * (ys[c] - py) - (ys[b] - py).toLong() * (xs[c] - px) != 0L
            }
        }

        // 清理本锚点用到的 dp / pred
        for (i in 0 until m) {
            val ci = cand[i]
            for (j in i + 1 until m) {
                dp[ci * n + cand[j]] = -1L
                pred[ci * n + cand[j]] = -1
            }
        }

        for (bi in 0 until m) {
            val b = cand[bi]
            val bx = xs[b]
            val by = ys[b]

            // ---------------- 第一遍：三角判空 + 种子（按极角序，整组折叠） --------
            var mPhi = -1
            var mPhiX = 0
            var mPhiY = 0
            var gi = bi + 1
            while (gi < m) {
                var gj = gi + 1
                val gx = xs[cand[gi]] - px
                val gy = ys[cand[gi]] - py
                while (gj < m) {
                    val dx = xs[cand[gj]] - px
                    val dy = ys[cand[gj]] - py
                    if (dx.toLong() * gy - dy.toLong() * gx != 0L) break
                    gj++
                }
                for (ci in gi until gj) {
                    val c = cand[ci]
                    val ok = if (mPhi < 0) true else {
                        (mPhiX - bx).toLong() * (ys[c] - by) - (mPhiY - by).toLong() * (xs[c] - bx) >= 0L
                    }
                    triOk[c] = ok
                    if (ok) {
                        val turn = (bx - px).toLong() * (ys[c] - py) - (by - py).toLong() * (xs[c] - px)
                        if (turn > 0L) {
                            dp[b * n + c] = turn
                            pred[b * n + c] = -1
                        }
                    }
                }
                // b 自己所在的那一组是「射线 p→b 上」的点，永远不在三角形内部，不折叠
                val isBGroup = gi == bi + 1 &&
                    (xs[cand[bi]] - px).toLong() * (ys[cand[gi]] - py) -
                    (ys[cand[bi]] - py).toLong() * (xs[cand[gi]] - px) == 0L
                if (!isBGroup) {
                    for (ci in gi until gj) {
                        val x = cand[ci]
                        if (mPhi < 0) {
                            mPhi = x; mPhiX = xs[x]; mPhiY = ys[x]
                        } else {
                            val cr = (mPhiX - bx).toLong() * (ys[x] - by) - (mPhiY - by).toLong() * (xs[x] - bx)
                            if (cr > 0L) {
                                mPhi = x; mPhiX = xs[x]; mPhiY = ys[x]
                            }
                        }
                    }
                }
                gi = gj
            }

            // ---------------- 第二遍：转移（按绕 b 的方向序，双指针前缀最大值） ---
            if (segEmpty[b]) {
                val ord = dirOrder[b]
                val posP = dirPos[b][p]
                val deg = n - 1
                var cntA = 0
                var cntC = 0
                var idx = posP
                for (s in 1 until n) {
                    idx++
                    if (idx >= deg) idx -= deg
                    val x = ord[idx]
                    if (x == p) continue
                    val rx = rank[x]
                    if (rx < 0) continue
                    if (rx < bi) aList[cntA++] = x else if (rx > bi) cList[cntC++] = x
                }
                var ptr = 0
                var bestAVal = -1L
                var bestAPoint = -1
                for (t in 0 until cntC) {
                    val c = cList[t]
                    val cbx = (xs[c] - bx).toLong()
                    val cby = (ys[c] - by).toLong()
                    while (ptr < cntA) {
                        val a = aList[ptr]
                        val cr = cbx * (ys[a] - by) - cby * (xs[a] - bx)
                        if (cr > 0L) {
                            val v = dp[a * n + b]
                            if (v > bestAVal) {
                                bestAVal = v
                                bestAPoint = a
                            }
                            ptr++
                        } else {
                            break
                        }
                    }
                    if (bestAVal >= 0 && triOk[c]) {
                        val turn = (bx - px).toLong() * (ys[c] - py) - (by - py).toLong() * (xs[c] - px)
                        if (turn > 0L) {
                            val v = bestAVal + turn
                            if (v > dp[b * n + c]) {
                                dp[b * n + c] = v
                                pred[b * n + c] = bestAPoint
                            }
                        }
                    }
                }
            }

            // ---------------- 第三遍：闭合 --------------------------------
            for (ci in bi + 1 until m) {
                val c = cand[ci]
                val v = dp[b * n + c]
                if (v > bestArea) {
                    val cr = (xs[c] - bx).toLong() * (py - by) - (ys[c] - by).toLong() * (px - bx)
                    if (cr > 0L) {
                        bestArea = v
                        bestP = p
                        bestB = b
                        bestC = c
                    }
                }
            }
        }

        for (i in 0 until m) rank[cand[i]] = -1
    }

    /** 回溯出取到最优值的多边形顶点（逆时针，含锚点） */
    fun reconstructBest(): IntArray {
        if (bestArea <= 0L) return IntArray(0)
        val p = bestP
        val b0 = bestB
        val c0 = bestC
        // dp / pred 数组在所有锚点间复用，重跑一次最优锚点把 pred 还原成当时的取值
        val saved = bestArea
        bestArea = 0L
        solveAnchor(p)
        bestArea = saved
        val path = ArrayList<Int>()
        var b = b0
        var c = c0
        path.add(c)
        while (true) {
            path.add(b)
            val pr = pred[b * n + c]
            if (pr < 0) break
            c = b
            b = pr
        }
        path.add(p)
        path.reverse()
        return path.toIntArray()
    }
}

// =====================================================================
// 3. 朴素解法（结构不同的第二条路径：显式三重循环 + 逐点扫描判空）
// =====================================================================

private fun orient(ax: Int, ay: Int, bx: Int, by: Int, cx: Int, cy: Int): Long =
    (bx - ax).toLong() * (cy - ay) - (by - ay).toLong() * (cx - ax)

/** 三角形 (a,b,c)（要求逆时针）内部是否无点 */
private fun triangleEmpty(
    n: Int, xs: IntArray, ys: IntArray,
    a: Int, b: Int, c: Int,
): Boolean {
    for (x in 0 until n) {
        if (x == a || x == b || x == c) continue
        val xx = xs[x]
        val xy = ys[x]
        if (orient(xs[a], ys[a], xs[b], ys[b], xx, xy) > 0L &&
            orient(xs[b], ys[b], xs[c], ys[c], xx, xy) > 0L &&
            orient(xs[c], ys[c], xs[a], ys[a], xx, xy) > 0L
        ) return false
    }
    return true
}

/** 线段 (a,b) 内部是否无点 */
private fun segmentEmpty(n: Int, xs: IntArray, ys: IntArray, a: Int, b: Int): Boolean {
    for (x in 0 until n) {
        if (x == a || x == b) continue
        val ux = (xs[x] - xs[a]).toLong()
        val uy = (ys[x] - ys[a]).toLong()
        val vx = (xs[b] - xs[a]).toLong()
        val vy = (ys[b] - ys[a]).toLong()
        if (ux * vy - uy * vx != 0L) continue
        if (ux * vx + uy * vy <= 0L) continue
        if (ux * ux + uy * uy < vx * vx + vy * vy) return false
    }
    return true
}

private fun naiveMaxHole(n: Int, xs: IntArray, ys: IntArray): Long {
    var best = 0L
    val cand = IntArray(n)
    for (p in 0 until n) {
        val px = xs[p]
        val py = ys[p]
        var m = 0
        for (x in 0 until n) if (x != p) {
            val dy = ys[x] - py
            if (dy > 0 || (dy == 0 && xs[x] > px)) cand[m++] = x
        }
        if (m < 2) continue
        // 插入排序：按极角、同角按距离升序
        for (i in 1 until m) {
            val v = cand[i]
            var j = i - 1
            while (j >= 0) {
                val u = cand[j]
                val ux = (xs[u] - px).toLong()
                val uy = (ys[u] - py).toLong()
                val vx = (xs[v] - px).toLong()
                val vy = (ys[v] - py).toLong()
                val cr = ux * vy - uy * vx
                val uAfter = if (cr != 0L) cr < 0L else {
                    val du = ux * ux + uy * uy
                    val dv = vx * vx + vy * vy
                    if (du != dv) du > dv else u > v
                }
                if (!uAfter) break
                cand[j + 1] = cand[j]
                j--
            }
            cand[j + 1] = v
        }
        val dp = LongArray(m * m) { -1L }
        for (bi in 0 until m) {
            for (ci in bi + 1 until m) {
                val b = cand[bi]
                val c = cand[ci]
                val turn = orient(px, py, xs[b], ys[b], xs[c], ys[c])
                if (turn <= 0L) continue
                var v = -1L
                val te = triangleEmpty(n, xs, ys, p, b, c)
                if (te) v = turn
                if (te && segmentEmpty(n, xs, ys, p, b)) {
                    for (ai in 0 until bi) {
                        val a = cand[ai]
                        val base = dp[ai * m + bi]
                        if (base >= 0L && orient(xs[a], ys[a], xs[b], ys[b], xs[c], ys[c]) > 0L) {
                            val candV = base + turn
                            if (candV > v) v = candV
                        }
                    }
                }
                dp[bi * m + ci] = v
                if (v > best && orient(xs[b], ys[b], xs[c], ys[c], px, py) > 0L) best = v
            }
        }
    }
    return best
}

// =====================================================================
// 4. 全子集暴力（小规模，与解法不共享核心代码）
// =====================================================================

private fun bruteMaxHole(n: Int, xs: IntArray, ys: IntArray): Long {
    val order = (0 until n).sortedWith(compareBy({ xs[it] }, { ys[it] }))
    val sel = IntArray(n)
    val member = BooleanArray(n)
    val stack = IntArray(2 * n + 1)
    var best = 0L
    val limit = 1 shl n
    for (mask in 7 until limit) {
        var k = 0
        java.util.Arrays.fill(member, false)
        for (i in 0 until n) if ((mask ushr i) and 1 == 1) {
            val v = order[i]
            sel[k++] = v
            member[v] = true
        }
        if (k < 3) continue
        // 单调链求凸包（严格凸：去共线）
        var hs = 0
        for (i in 0 until k) {
            while (hs >= 2 && orient(xs[stack[hs - 2]], ys[stack[hs - 2]], xs[stack[hs - 1]], ys[stack[hs - 1]], xs[sel[i]], ys[sel[i]]) <= 0L) hs--
            stack[hs++] = sel[i]
        }
        val t = hs + 1
        for (i in k - 2 downTo 0) {
            while (hs >= t && orient(xs[stack[hs - 2]], ys[stack[hs - 2]], xs[stack[hs - 1]], ys[stack[hs - 1]], xs[sel[i]], ys[sel[i]]) <= 0L) hs--
            stack[hs++] = sel[i]
        }
        val hullSize = hs - 1
        if (hullSize != k) continue
        var area2 = 0L
        for (i in 0 until hullSize) {
            val u = stack[i]
            val w = stack[(i + 1) % hullSize]
            area2 += xs[u].toLong() * ys[w] - xs[w].toLong() * ys[u]
        }
        if (area2 <= best) continue
        // 内部不能有其它给定点
        var empty = true
        for (x in 0 until n) {
            if (member[x]) continue
            var inside = true
            for (i in 0 until hullSize) {
                val u = stack[i]
                val w = stack[(i + 1) % hullSize]
                if (orient(xs[u], ys[u], xs[w], ys[w], xs[x], ys[x]) <= 0L) {
                    inside = false
                    break
                }
            }
            if (inside) {
                empty = false
                break
            }
        }
        if (empty) best = area2
    }
    return best
}

// =====================================================================
// 5. 最优多边形独立验证
// =====================================================================

private fun verifyHole(poly: IntArray, n: Int, xs: IntArray, ys: IntArray): Long {
    val k = poly.size
    require(k >= 3) { "多边形顶点数不足：$k" }
    require(poly.toSet().size == k) { "顶点有重复" }
    var area2 = 0L
    for (i in 0 until k) {
        val u = poly[i]
        val w = poly[(i + 1) % k]
        require(orient(xs[u], ys[u], xs[w], ys[w], xs[poly[(i + 2) % k]], ys[poly[(i + 2) % k]]) > 0L) {
            "顶点 ${poly[(i + 1) % k]} 处不是严格左转"
        }
        area2 += xs[u].toLong() * ys[w] - xs[w].toLong() * ys[u]
    }
    require(area2 > 0L) { "面积为非正：$area2" }
    val vertex = BooleanArray(n)
    for (v in poly) vertex[v] = true
    for (x in 0 until n) {
        if (vertex[x]) continue
        var inside = true
        for (i in 0 until k) {
            val u = poly[i]
            val w = poly[(i + 1) % k]
            if (orient(xs[u], ys[u], xs[w], ys[w], xs[x], ys[x]) <= 0L) {
                inside = false
                break
            }
        }
        require(!inside) { "点 $x 严格落在多边形内部" }
    }
    return area2
}

// =====================================================================
// 6. main
// =====================================================================

private fun randomSet(rnd: java.util.Random, n: Int, span: Int): Pair<IntArray, IntArray> {
    val used = HashSet<Long>()
    val xa = IntArray(n)
    val ya = IntArray(n)
    var k = 0
    while (k < n) {
        val x = rnd.nextInt(span)
        val y = rnd.nextInt(span)
        val key = x.toLong() shl 32 or (y.toLong() and 0xFFFFFFFFL)
        if (used.add(key)) {
            xa[k] = x
            ya[k] = y
            k++
        }
    }
    return Pair(xa, ya)
}

private fun solveFull(xs: IntArray, ys: IntArray): Long = FastSolver(xs.size, xs, ys).solve()

fun main() {
    // ---------- 0) 生成器自检 ----------
    val (raw500x, raw500y) = generateRawPoints(500)
    check(raw500x[0] == 527 && raw500y[0] == 144) { "第 1 个点应为 (527, 144)" }
    check(raw500x[1] == -488 && raw500y[1] == 732) { "第 2 个点应为 (-488, 732)" }
    check(raw500x[2] == -454 && raw500y[2] == -947) { "第 3 个点应为 (-454, -947)" }
    val (xs500, ys500) = dedupePoints(raw500x, raw500y)
    println(
        "生成器自检：前 3 点 (527,144) (-488,732) (-454,-947) 与题面一致；" +
            "500 点去重后 ${xs500.size} 个点",
    )

    // ---------- 1) 20 点样例（题面答案 1049694.5） ----------
    val (raw20x, raw20y) = generateRawPoints(20)
    val (xs20, ys20) = dedupePoints(raw20x, raw20y)
    val fast20 = solveFull(xs20, ys20)
    check(fast20 == 2_099_389L) { "20 点样例快速 DP 得到 $fast20，应为 2099389" }
    val naive20 = naiveMaxHole(xs20.size, xs20, ys20)
    check(naive20 == fast20) { "20 点样例：朴素 DP $naive20 ≠ 快速 DP $fast20" }
    val brute20 = bruteMaxHole(xs20.size, xs20, ys20)
    check(brute20 == fast20) { "20 点样例：全子集暴力 $brute20 ≠ 快速 DP $fast20" }
    println("20 点样例：快速 DP = 朴素 DP = 2^20 全枚举 = $fast20（双倍面积）= ${areaText(fast20)}，与题面一致")

    // ---------- 2) 随机小规模：三种方法互证 ----------
    val rnd = java.util.Random(252252L)
    var randomCases = 0
    for (span in intArrayOf(10, 25, 60)) {
        for (nn in intArrayOf(8, 9, 10, 11, 12)) {
            repeat(4) {
                val (rx, ry) = randomSet(rnd, nn, span)
                val (dx, dy) = dedupePoints(rx, ry)
                if (dx.size < 3) return@repeat
                val f = solveFull(dx, dy)
                val nv = naiveMaxHole(dx.size, dx, dy)
                val bf = bruteMaxHole(dx.size, dx, dy)
                check(f == nv && f == bf) { "随机点集 n=${dx.size} span=$span：快速 $f 朴素 $nv 暴力 $bf" }
                randomCases++
            }
        }
    }
    println("随机小规模三方法互证（全子集暴力为基准）：$randomCases 组全部一致")

    // ---------- 3) 前缀子实例：朴素 DP vs 快速 DP ----------
    for (size in intArrayOf(50, 100, 150, 200)) {
        val (sx, sy) = dedupePoints(raw500x.copyOf(size), raw500y.copyOf(size))
        val tf0 = System.nanoTime()
        val f = solveFull(sx, sy)
        val fastMs = (System.nanoTime() - tf0) / 1e6
        val tn0 = System.nanoTime()
        val nv = naiveMaxHole(sx.size, sx, sy)
        val naiveMs = (System.nanoTime() - tn0) / 1e6
        check(f == nv) { "前缀 $size 点：快速 DP $f ≠ 朴素 DP $nv" }
        println(
            "前缀子实例 $size 点（去重后 ${sx.size}）：快速 DP = 朴素 DP = $f（${areaText(f)}）；" +
                "快速 ${"%.1f".format(fastMs)} ms vs 朴素 ${"%.1f".format(naiveMs)} ms",
        )
    }

    // ---------- 4) 全规模 500 点 ----------
    val answer = solveFull(xs500, ys500)
    println("全规模 500 点：最大双倍面积 = $answer，即面积 = ${areaText(answer)}")

    // 旋转 180° 复核
    val rotX = IntArray(xs500.size) { -xs500[it] }
    val rotY = IntArray(ys500.size) { -ys500[it] }
    val rot = solveFull(rotX, rotY)
    check(rot == answer) { "旋转 180° 后答案不一致：$rot vs $answer" }
    println("旋转 180° 复核：$rot（一致）")

    // 重建最优多边形并独立验证
    val solver = FastSolver(xs500.size, xs500, ys500)
    check(solver.solve() == answer)
    val poly = solver.reconstructBest()
    val verified = verifyHole(poly, xs500.size, xs500, ys500)
    check(verified == answer) { "重建多边形面积 $verified ≠ $answer" }
    println("最优多边形验证：${poly.size} 个顶点，严格凸 + 内部无 P 中点 + 面积一致")

    // ---------- 5) 计时：JIT 预热后 3 轮取最优 ----------
    var bestMs = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val v = FastSolver(xs500.size, xs500, ys500).solve()
        val ms = (System.nanoTime() - t0) / 1e6
        check(v == answer)
        if (ms < bestMs) bestMs = ms
        println("  第 ${round + 1} 轮完整求解：${"%.1f".format(ms)} ms")
    }
    println("完整求解（含方向序预处理 + DP）：${"%.1f".format(bestMs)} ms（3 轮最优，JIT 预热后）")

    // ---------- 6) 答案编码 ----------
    val encoded = answer * 50_000_000L
    println("答案：面积 = ${areaText(answer)}；仓库编码 round(area × 10^8) = $encoded")
    println("check() 全部通过")
}

private fun areaText(area2: Long): String =
    "${area2 / 2}.${if (area2 % 2L == 0L) "0" else "5"}"
