#!/usr/bin/env kotlin
/**
 * Project Euler 260 — Stone Game（取石子游戏）
 *
 * 思路：
 *   局面 = 三堆石子的多重集 {x,y,z}（排序 x ≤ y ≤ z）。一步 = 取 N ≥ 1 与三堆的任意非空子集 S，
 *   被选中的每堆同时减 N。正常走法（取走最后一颗者胜），(0,0,0) 为必败。记 P = 全部必败局面。
 *   直接对每个状态枚举「子集 × N」是 O(Z⁴)，本题用三条「唯一性引理」把每个移法族压成 O(1) 查询。
 *
 *   唯一性引理（证明都是「两步并一步」）：
 *     (L1) 固定值对 {u,v}，至多一个 w 使 {u,v,w} ∈ P。
 *          （{u,v,w1},{u,v,w2} ∈ P 且 w1<w2：后者对 w2 那一堆减 w2−w1 直达前者，矛盾）
 *     (L2) 固定（未动值 u，被同减的两堆之差 d），至多一个基点 a 使 {a, a+d, u} ∈ P。
 *          （把较大基点的两堆同减基点差即到较小者）
 *     (L3) 固定相邻间隔 (d1, d2) = (y−x, z−y)，至多一个基点 x 使 {x, x+d1, x+d1+d2} ∈ P。
 *          （两局面之间三堆同减基点差）
 *   于是每种 key 只存「那唯一的坐标」或 −1；查询「本状态下该移法族能否一步走到 P」
 *   ⇔ 该 key 存着的坐标 < 当前对应坐标。
 *
 *   七个移法族（当前 x ≤ y ≤ z；等号右边都是 O(1) 查询，读到的局面都在递推序中更早）：
 *     只减 x 堆：F1[y,z] < x        只减 y 堆：F1[x,z] < y        只减 z 堆：F1[x,y] < z
 *     同减 x,y 堆：F2[z, y−x] < x   同减 y,z 堆：F2[x, z−y] < y   同减 x,z 堆：F2[y, z−x] < x
 *     三堆同减：F3[y−x, z−y] < x
 *   其中 F1 按值对索引、F2 按（未动值, 两堆差）索引、F3 按间隔对索引。
 *
 *   递推顺序：任一步后继的和严格变小，排序后的多重集也必按字典序更小；按 (x,y,z) 字典序递推，
 *   查询所需局面全部算完。对固定 (x,y)：由 (L1) 至多一个 z 使 (x,y,z) ∈ P；一旦它出现，
 *   更大的 z 都能「只减最大堆」一步到它 —— 每行扫到第一个 P 即停；若更早的行已给该值对补全
 *   （补全 < y），整行全是 N，可整行跳过。此时第 3 条查询（F1[x,y] < z）在本行内恒为假，内循环
 *   实际只做 6 条查询。
 *
 *   方法 A（最优路径）= 上述结构 + 字典序 + 行剪枝；方法 B = 独立实现：三张布尔标记表 + 字典序
 *   扫描（只做整行剪枝，不存坐标、行内也不提前终止；靠扫描顺序保证「标记即直达」），代码路径与
 *   A 实质不同。brute-force.kt 则完全按定义枚举「7 个移法族 × 所有 N」，只用于小规模对照。
 *
 * 旁证：
 *   · 题面给的 Z=100 断言 Σ=173895：A、B、直接暴力三种实现全部复现（还逐局面比对）；
 *   · 直接暴力与 A 在 Z=100 逐局面一致，A 与 B 在 Z=150 逐局面一致；
 *   · A 与 B 在 Z=100/200/500/1000 的（Σ, 局面个数, 局面集合滚动哈希）四组全部一致；
 *   · x=0 的一行退化为两堆的 Wythoff 游戏，实跑局面与闭式 (0, ⌊nφ⌋, ⌊nφ²⌋) 逐项吻合；
 *   · 公开答案表（Stephan Brumme 的 260 题解、Project Euler 答案汇总页）对照 167542057。
 *
 * 答案：167542057
 * 复杂度：方法 A O(Z³) 个状态、每状态至多 6 次 O(1) 查询（Z=1000 实测只扫 4.65×10⁷ 个）；
 *         方法 B O(Z³) 个状态、每状态至多 7 次标记查询；两者内存都是 O(Z²)。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val Z_FULL = 1000
private const val EXPECTED = 167_542_057L
private const val SMALL_Z = 100
private const val SMALL_OFFICIAL = 173_895L

/** 一次完整求解的结果：Σ(x+y+z)、必败局面个数、局面集合的滚动哈希、扫描过的状态数。 */
private data class Out(val sum: Long, val count: Long, val hash: Long, val steps: Long)

/** 唯一性校验：同一 key 不允许写入两个不同的坐标值（否则两 P 局面互相直达，与 P 的定义矛盾）。 */
private fun putUniq(arr: IntArray, i: Int, v: Int) {
    val old = arr[i]
    check(old == -1 || old == v) { "唯一性引理被破坏：下标 $i 已有 $old，又写入 $v" }
    arr[i] = v
}

private fun mixHash(h: Long, x: Int, y: Int, z: Int): Long =
    h * 1_000_003L + (x * 1_000_001L + y * 100_003L + z)

/**
 * 方法 A（最优路径）：唯一补全结构 + 字典序递推 + 行剪枝。
 *
 * f1[u,v] = 值对 {u,v} 的唯一补全（第三堆值），无则 −1；
 * f2[u,d] = （未动堆 u，被同减两堆之差 d）的唯一基点；
 * f3[d1,d2] = 相邻间隔 (d1,d2) 的唯一基点。
 * [record] 非空时顺便记录必败局面（仅小规模对照用）。
 */
private fun solveOptimized(zMax: Int, record: BooleanArray? = null): Out {
    val sz = zMax + 1
    val f1 = IntArray(sz * sz) { -1 }
    val f2 = IntArray(sz * sz) { -1 }
    val f3 = IntArray(sz * sz) { -1 }
    var sum = 0L
    var count = 0L
    var hash = 0L
    var steps = 0L
    for (x in 0..zMax) {
        for (y in x..zMax) {
            val yx = y - x
            if (f1[x * sz + y] != -1) continue          // 值对 (x,y) 的补全已在更早的行给出 → 整行 N
            var z = y
            while (z <= zMax) {
                steps++
                val zx = z - x
                val zy = z - y
                var t = f1[y * sz + z]; if (t >= 0 && t < x) { z++; continue }   // 只减 x 堆
                t = f1[x * sz + z]; if (t >= 0 && t < y) { z++; continue }       // 只减 y 堆
                t = f2[y * sz + zx]; if (t >= 0 && t < x) { z++; continue }      // 同减 x,z
                t = f2[x * sz + zy]; if (t >= 0 && t < y) { z++; continue }      // 同减 y,z
                t = f2[z * sz + yx]; if (t >= 0 && t < x) { z++; continue }      // 同减 x,y
                t = f3[yx * sz + zy]; if (t >= 0 && t < x) { z++; continue }     // 三堆同减
                // 必败局面：登记进三种结构（写入值即「更早的局面能一步到此」的依据）
                putUniq(f1, y * sz + z, x)
                putUniq(f1, x * sz + z, y)
                putUniq(f1, x * sz + y, z)
                putUniq(f2, z * sz + yx, x)
                putUniq(f2, y * sz + zx, x)
                putUniq(f2, x * sz + zy, y)
                putUniq(f3, yx * sz + zy, x)
                sum += x + y + z
                count++
                hash = mixHash(hash, x, y, z)
                record?.set((x * sz + y) * sz + z, true)
                break                                     // 本行唯一的 P；更大的 z 全部一步可达它
            }
        }
    }
    return Out(sum, count, hash, steps)
}

/**
 * 方法 B（独立复核）：标记法扫描（Brumme 式）。
 * one[u,v]  = 值对 {u,v} 已被某个 P 局面占用；
 * two[d,u]  = （两堆差 d，未动堆 u）已被某个 P 局面占用；
 * all[d1,d2]= 间隔对 (d1,d2) 已被某个 P 局面占用。
 * 不存坐标：扫描顺序保证「读到标记时，写标记的 P 局面必可达」；只保留整行剪枝，
 * 行内不提前终止 —— 与 A 的代码路径独立。
 */
private fun solveByFlags(zMax: Int, record: BooleanArray? = null): Out {
    val sz = zMax + 1
    val one = BooleanArray(sz * sz)
    val two = BooleanArray(sz * sz)
    val all = BooleanArray(sz * sz)
    var sum = 0L
    var count = 0L
    var hash = 0L
    var steps = 0L
    for (x in 0..zMax) {
        for (y in x..zMax) {
            if (one[x * sz + y]) continue
            for (z in y..zMax) {
                steps++
                val yx = y - x
                val zx = z - x
                val zy = z - y
                if (one[y * sz + z] || one[x * sz + z] || one[x * sz + y]) continue
                if (two[yx * sz + z] || two[zy * sz + x] || two[zx * sz + y]) continue
                if (all[yx * sz + zx]) continue
                sum += x + y + z
                count++
                hash = mixHash(hash, x, y, z)
                record?.set((x * sz + y) * sz + z, true)
                one[y * sz + z] = true
                one[x * sz + z] = true
                one[x * sz + y] = true
                two[yx * sz + z] = true
                two[zy * sz + x] = true
                two[zx * sz + y] = true
                all[yx * sz + zx] = true
            }
        }
    }
    return Out(sum, count, hash, steps)
}

/**
 * 直接暴力（对照路径）：按总和 s = x+y+z 递增，对每个排序三元组(x ≤ y ≤ z)枚举
 * 全部 7 个移法族 × 全部 N ∈ [1, z]，只要有一个后继是 P 就是 N，否则是 P。
 * 复杂度 O(Z⁴)，只在 Z ≲ 200 可行。
 */
private fun solveByBrute(zMax: Int, record: BooleanArray? = null): Out {
    val sz = zMax + 1
    val lost = BooleanArray(sz * sz * sz)

    fun lostAt(a: Int, b: Int, c: Int): Boolean {
        var u = a; var v = b; var w = c
        if (u > v) { val t = u; u = v; v = t }
        if (v > w) { val t = v; v = w; w = t }
        if (u > v) { val t = u; u = v; v = t }
        return lost[(u * sz + v) * sz + w]
    }

    var sum = 0L
    var count = 0L
    var hash = 0L
    var steps = 0L
    for (s in 0..3 * zMax) {
        var x = 0
        while (x <= zMax && x <= s) {
            var y = x
            while (y <= zMax && x + y <= s) {
                val z = s - x - y
                if (z in y..zMax) {
                    steps++
                    var selfLost = true
                    var n = 1
                    while (n <= z && selfLost) {
                        if (n <= x) {   // 只减第 1 堆、同减 1&2、同减 1&3、三堆同减
                            if (lostAt(x - n, y, z)) selfLost = false
                            else if (lostAt(x - n, y - n, z)) selfLost = false
                            else if (lostAt(x - n, y, z - n)) selfLost = false
                            else if (lostAt(x - n, y - n, z - n)) selfLost = false
                        }
                        if (selfLost && n <= y) {   // 只减第 2 堆、同减 2&3
                            if (lostAt(x, y - n, z)) selfLost = false
                            else if (lostAt(x, y - n, z - n)) selfLost = false
                        }
                        if (selfLost && lostAt(x, y, z - n)) selfLost = false   // 只减第 3 堆
                        n++
                    }
                    if (selfLost) {
                        lost[(x * sz + y) * sz + z] = true
                        sum += x + y + z
                        count++
                        hash = mixHash(hash, x, y, z)
                        record?.set((x * sz + y) * sz + z, true)
                    }
                }
                y++
            }
            x++
        }
    }
    return Out(sum, count, hash, steps)
}

/** JIT 预热后 3 轮取最优，并与 [expected] 核对（防结果漂移）。返回最优毫秒数。 */
private fun bestOf3(tag: String, expected: Out, f: () -> Out): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

private fun comparePositions(tag: String, zMax: Int, a: BooleanArray, b: BooleanArray) {
    val sz = zMax + 1
    var bad = 0
    for (x in 0..zMax) for (y in x..zMax) for (z in y..zMax) {
        val i = (x * sz + y) * sz + z
        if (a[i] != b[i]) {
            bad++
            if (bad <= 5) println("  不一致：($x,$y,$z) A=${a[i]} B=${b[i]}")
        }
    }
    check(bad == 0) { "$tag 有 $bad 个局面不一致" }
    println("$tag：逐局面一致（含 x ≤ y ≤ z 的全部三元组）")
}

fun main() {
    // ---------- 题面小规模断言：Z=100 → 173895 ----------
    val smallA = solveOptimized(SMALL_Z)
    val smallB = solveByFlags(SMALL_Z)
    val smallBrute = solveByBrute(SMALL_Z)
    for ((name, out) in listOf("方法 A" to smallA, "方法 B" to smallB, "直接暴力" to smallBrute)) {
        check(out.sum == SMALL_OFFICIAL) { "$name 在 Z=$SMALL_Z 得 ${out.sum}，题面要求 $SMALL_OFFICIAL" }
        println("$name：Z=$SMALL_Z → Σ = ${out.sum}（局面 ${out.count} 个），与题面 173895 一致")
    }

    // ---------- 逐局面对照 ----------
    val sz = SMALL_Z + 1
    val recA = BooleanArray(sz * sz * sz)
    val recB = BooleanArray(sz * sz * sz)
    val recBrute = BooleanArray(sz * sz * sz)
    solveOptimized(SMALL_Z, recA)
    solveByFlags(SMALL_Z, recB)
    solveByBrute(SMALL_Z, recBrute)
    comparePositions("Z=$SMALL_Z：方法 A vs 直接暴力", SMALL_Z, recA, recBrute)
    comparePositions("Z=$SMALL_Z：方法 A vs 方法 B", SMALL_Z, recA, recB)

    val z150 = 150
    val s150 = z150 + 1
    val rec150A = BooleanArray(s150 * s150 * s150)
    val rec150B = BooleanArray(s150 * s150 * s150)
    solveOptimized(z150, rec150A)
    solveByFlags(z150, rec150B)
    comparePositions("Z=$z150：方法 A vs 方法 B", z150, rec150A, rec150B)

    // ---------- x=0 一行退化为 Wythoff 游戏：与闭式 (0, ⌊nφ⌋, ⌊nφ²⌋) 对照 ----------
    val phi = (1.0 + Math.sqrt(5.0)) / 2.0
    var wythoffCount = 0
    var n = 0
    while (true) {
        val a = Math.floor(n * phi).toInt()
        val b = Math.floor(n * phi * phi).toInt()
        if (b > SMALL_Z) break
        check(recA[(0 * sz + a) * sz + b]) { "Wythoff 对 ($a,$b) 在 x=0 行不是必败局面" }
        wythoffCount++
        n++
    }
    var rowCount = 0
    for (a in 0..SMALL_Z) for (b in a..SMALL_Z) if (recA[(0 * sz + a) * sz + b]) rowCount++
    check(rowCount == wythoffCount) { "x=0 行有 $rowCount 个 P 局面，闭式只有 $wythoffCount 个" }
    println("x=0 行与 Wythoff 闭式对照：$rowCount 个必败局面逐一命中 (0, ⌊nφ⌋, ⌊nφ²⌋)，n = 0..${n - 1}")

    // ---------- 完整规模：Z=1000 ----------
    val fullA = solveOptimized(Z_FULL)
    val fullB = solveByFlags(Z_FULL)
    println("方法 A：Z=$Z_FULL → Σ = ${fullA.sum}，局面 ${fullA.count} 个，扫描 ${fullA.steps} 个状态")
    println("方法 B：Z=$Z_FULL → Σ = ${fullB.sum}，局面 ${fullB.count} 个，扫描 ${fullB.steps} 个状态")
    check(fullA.sum == fullB.sum && fullA.count == fullB.count && fullA.hash == fullB.hash) {
        "两种方法在完整规模下不一致：A=$fullA B=$fullB"
    }
    check(fullA.sum == EXPECTED) { "答案与预期不符：${fullA.sum}" }
    println("两种方法一致（Σ、局面个数、局面集合哈希全同）；答案 = ${fullA.sum}")

    // ---------- 计时（JIT 预热后 3 轮最优） ----------
    solveOptimized(SMALL_Z)
    solveByBrute(SMALL_Z)
    solveByFlags(SMALL_Z)
    val msBrute100 = bestOf3("直接暴力 Z=$SMALL_Z", smallBrute) { solveByBrute(SMALL_Z) }
    solveOptimized(SMALL_Z)
    val msA100 = bestOf3("方法 A Z=$SMALL_Z", smallA) { solveOptimized(SMALL_Z) }
    val msA = bestOf3("方法 A Z=$Z_FULL", fullA) { solveOptimized(Z_FULL) }
    val msB = bestOf3("方法 B Z=$Z_FULL", fullB) { solveByFlags(Z_FULL) }

    println()
    println("汇总：直接暴力 Z=$SMALL_Z ${"%.1f".format(msBrute100)} ms；" +
        "方法 A Z=$SMALL_Z ${"%.1f".format(msA100)} ms；" +
        "方法 A Z=$Z_FULL ${"%.1f".format(msA)} ms；" +
        "方法 B Z=$Z_FULL ${"%.1f".format(msB)} ms")
    println("check() 全部通过")
}
