package dev.pekt.engine

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * PE 275 — Balanced Sculptures（平衡雕塑）：n 个 blocks + 1 个 plinth 组成连通多联骨牌，
 * plinth 中心在 (0,0)、blocks 的 y > 0、blocks 质心的 x 坐标为 0；关于 y 轴的镜像算同一种。
 * 求 n = 18 时的雕塑数。
 *
 * 推导（详见 content/problems/0275/solution.kt 头部与 0275/analysis.md）：
 *   1. plinth 的四邻居里只有 (0,1) 可能放 block（其余三格 y ≤ 0），故 (0,1) 必为 block、
 *      且 blocks 去掉 plinth 后自身连通。坐标下移一格（根格 = (0,0)）后条件变为
 *      「Σx = 0」，镜像等价的分母用 Burnside：A(n) = (F(n) + S(n)) / 2。
 *   2. 有限域（菱形）：格 (x,y) 到根的最短路 ≥ |x| + y，路径含 |x| + y + 1 个格子，
 *      故 |x| + y ≤ n − 1；把搜索限制在菱形内。
 *   3. Redelmeier 规范枚举：候选表 U + 单调选择（取第 i 个候选格时，U[i+1..] 与其新邻居
 *      构成子问题的候选表，U[0..i−1] 在本支被永久否定），每个连通形状恰生成一次。
 *   4. 剪枝：剩余 r 格最多提供 maxCorr[r]（域内最大的 r 个 |x| 之和）的力矩修正，
 *      故 |sumx| > maxCorr[r] 时整支剪掉。
 *   5. 直接按镜像等价类枚举：对称阶段把候选格与其镜像当「成对单元」，分支为
 *      「一次放整对（保对称）」/「只放一侧并把镜像格永久排除（打破对称，每个手性等价类
 *      只生成代表元一次）」/「拒绝整对」；对称阶段 Σx 恒为 0，打破对称后退化为普通
 *      枚举 + 力矩剪枝。统计量即答案 A(n)。
 *
 * 并行（仅引擎侧适配，算法语义与内容版 solution.kt 的方法 A 逐字一致）：
 *   RunEngine.RUN_TIMEOUT_MS = 10 s，单线程实测约 21–22 s 会被熔断（504），因此把搜索树
 *   中「剩余格数 ≥ R_SPLIT」的大子树连同当时的候选表/状态**整体复制**成独立任务，投进
 *   共享任务队列，由 `availableProcessors()` 个线程动态领活并求和；剪枝、候选表语义、
 *   成对单元三分支全部不变，只是把「在哪条线程上算」换了个地方。复制保证任务间不共享
 *   任何可变状态（各自一份数组，只读表 ctx 共享）；任务数与线程数都有界。
 *
 * 本文件相对内容版还有一处**纯常数级**改动：把 occ/forb/inU 三个布尔数组压成一个 Int
 *   数组的位标志（1=占用、2=否定、4=在候选表）、邻居表改扁平数组（注意扁平表必须按 id
 *   递增顺序构建：id = xi * n + y，与 (y, xi) 的双层循环次序不同）。语义逐条等价，
 *   已与顺序版在 n = 6/10/15/18 上对拍一致。
 *
 * 复杂度：搜索树节点随 n 约 ×3.6，n = 18 约 1.0×10^9 节点；本机（8 逻辑核 = 4P+4E，
 *   且当时负载较高）实测：冷启动首轮 4.23 s（含 JVM 启动与 JIT），JIT 预热后 3 轮最优
 *   4.17 s，远低于 10 s 熔断线（单线程 21.7 s 会被熔断，故必须并行）。
 *
 * 校验：n = 6 → 18、n = 10 → 964、n = 15 → 360505、n = 18 → 15030564，
 *   与顺序版（content/problems/0275/solution.kt 方法 A）及内容 meta.answer 完全一致。
 */
internal fun solve0275Impl(): Long = solveSculptures(18)

/**
 * 参数化核心：便于回归对照（n = 6/10/15/18 与顺序版逐一比对）。
 * 入口 [solve0275Impl] 固定 n = 18。
 */
internal fun solveSculptures(n: Int): Long {
    val ctx = SculptureCtx(n)
    val bag = TaskBag()
    bag.submit(SculptureSearch.make(ctx, bag))

    val threads = Runtime.getRuntime().availableProcessors()
    val partial = LongArray(threads)
    val failure = arrayOfNulls<Throwable>(1)
    val workers = Array(threads) { t ->
        Thread {
            try {
                var local = 0L
                while (true) {
                    val task = bag.take()
                    if (task == null) {
                        if (bag.outstanding() == 0) break     // 没有待领任务且没有在算任务
                        Thread.onSpinWait()
                        continue
                    }
                    local += task.run()
                    bag.done()
                }
                partial[t] = local
            } catch (e: Throwable) {
                failure[0] = e                              // 工作线程异常带回主线程
            }
        }
    }
    workers.forEach { it.start() }
    workers.forEach { it.join() }
    failure[0]?.let { throw it }
    var total = 0L
    for (p in partial) total += p
    return total
}

/**
 * 剩余格数 ≥ [R_SPLIT] 的子树才值得复制成任务（更小的直接原地算，避免复制/调度开销
 * 压过并行收益）。n=18 实测：11 最快（切出 992 个任务、8 线程，粒度 5–20 ms）。
 */
private const val R_SPLIT = 11

private const val MODE_SYM = 0
private const val MODE_ASYM = 1

/** 共享任务袋：无锁队列 + 未完成任务计数，「队列空 且 无在算任务」才收工。 */
private class TaskBag {
    private val queue = ConcurrentLinkedQueue<SculptureSearch>()
    private val pending = AtomicInteger(0)

    fun submit(task: SculptureSearch) {
        pending.incrementAndGet()          // 先计数再入队，避免别的线程误判「全部完成」
        queue.add(task)
    }

    fun take(): SculptureSearch? = queue.poll()

    fun done() = pending.decrementAndGet()

    fun outstanding(): Int = pending.get()
}

/** 只读的域/邻接/剪枝表：所有线程共享，不存可变状态。 */
private class SculptureCtx(val n: Int) {
    val w = 2 * n - 1
    val size = w * n
    val xOf = IntArray(size) { (it / n) - (n - 1) }
    val valid = BooleanArray(size) { i -> kotlin.math.abs(xOf[i]) + (i % n) <= n - 1 }
    val mirror = IntArray(size) { i -> (-xOf[i] + (n - 1)) * n + (i % n) }
    val nbrFlat = IntArray(4 * size + 4)
    val nbrStart = IntArray(size + 1)
    val maxCorr = IntArray(n + 1)

    init {
        // 扁平邻接表必须按 id 递增顺序构建：id = xi * n + y，与 (y, xi) 的双层循环次序不同
        var p = 0
        for (id in 0 until size) {
            nbrStart[id] = p
            if (!valid[id]) continue
            val xi = id / n
            val y = id % n
            if (xi > 0) { val v = (xi - 1) * n + y; if (valid[v]) nbrFlat[p++] = v }
            if (xi < w - 1) { val v = (xi + 1) * n + y; if (valid[v]) nbrFlat[p++] = v }
            if (y > 0) { val v = xi * n + (y - 1); if (valid[v]) nbrFlat[p++] = v }
            if (y < n - 1) { val v = xi * n + (y + 1); if (valid[v]) nbrFlat[p++] = v }
        }
        nbrStart[size] = p
        // maxCorr[r]：域内除根外最大的 r 个 |x| 之和（剩余 r 格的力矩修正上界）
        val xs = ArrayList<Int>()
        for (id in 0 until size) if (valid[id] && id != (n - 1) * n) xs.add(kotlin.math.abs(xOf[id]))
        xs.sortDescending()
        var pref = 0
        for (r in 1..minOf(n, xs.size)) { pref += xs[r - 1]; maxCorr[r] = pref }
        for (r in xs.size + 1..n) maxCorr[r] = pref
    }
}

/**
 * 一台「搜索器」= 一个任务：持有全部可变状态（位标志数组 st + 候选表 u + 证据缓存）。
 * 任务之间不共享可变状态；st 的位：1=占用，2=否定，4=在候选表，空闲格 st == 0。
 */
private class SculptureSearch(
    private val ctx: SculptureCtx,
    private val bag: TaskBag,
    private val st: IntArray = IntArray(ctx.size),
    private val u: IntArray = IntArray(ctx.size + 8),
) {
    private val n = ctx.n
    private val size = ctx.size
    private val xOf = ctx.xOf
    private val mirror = ctx.mirror
    private val nbrFlat = ctx.nbrFlat
    private val nbrStart = ctx.nbrStart
    private val maxCorr = ctx.maxCorr

    private val processed = Array(n + 2) { IntArray(size) }
    private var uCount = 0
    private var count = 0L
    private var startMode = MODE_SYM
    private var startFrom = 0
    private var startCnt = 1
    private var startSumx = 0

    companion object {
        /** 根状态任务：根格 (0,0) 入位（occ + inU = 5），进入对称阶段。 */
        fun make(ctx: SculptureCtx, bag: TaskBag): SculptureSearch {
            val t = SculptureSearch(ctx, bag)
            val root = (ctx.n - 1) * ctx.n
            t.st[root] = 5
            t.pushNeighbors(root)
            return t
        }
    }

    private fun pushNeighbors(c: Int) {
        for (j in nbrStart[c] until nbrStart[c + 1]) {
            val v = nbrFlat[j]
            if (st[v] == 0) { st[v] = 4; u[uCount++] = v }
        }
    }

    private fun popTo(addedStart: Int) {
        while (uCount > addedStart) { val v = u[--uCount]; st[v] = st[v] and 4.inv() }
    }

    /** 把当前状态复制成一个独立任务并投进任务袋（这棵子树搬到别的线程算）。 */
    private fun fork(mode: Int, from: Int, cnt: Int, sumx: Int) {
        val t = SculptureSearch(ctx, bag, st.copyOf(), u.copyOf())
        t.uCount = uCount
        t.startMode = mode
        t.startFrom = from
        t.startCnt = cnt
        t.startSumx = sumx
        bag.submit(t)
    }

    /** 任务入口：从快照参数继续（depth 从 0 起，用本实例自己的证据缓存）。 */
    fun run(): Long {
        if (startMode == MODE_SYM) solveSym(startFrom, startCnt, 0)
        else solveAsym(startFrom, startCnt, startSumx, 0)
        return count
    }

    /** 对称阶段：形状关于 y 轴对称，Σx 恒为 0。 */
    private fun solveSym(from: Int, cnt: Int, depth: Int) {
        if (cnt == n) { count++; return }
        val proc = processed[depth]
        var pc = 0
        var i = from
        while (i < uCount) {
            val c = u[i]; i++
            if (st[c] != 4) continue                 // 已占用或已否定
            if (xOf[c] == 0) {
                // 轴上的格：单独放入，保持对称
                st[c] = 5
                val added = uCount
                pushNeighbors(c)
                if (n - (cnt + 1) >= R_SPLIT) fork(MODE_SYM, i, cnt + 1, 0)
                else solveSym(i, cnt + 1, depth + 1)
                popTo(added)
                st[c] = 6                            // 本支否定（保留「在候选表」位）
                proc[pc++] = c
            } else {
                val m = mirror[c]
                // (1) 保持对称：一次放入整对
                if (cnt + 2 <= n) {
                    st[c] = 5; st[m] = 5
                    val added = uCount
                    pushNeighbors(c); pushNeighbors(m)
                    if (n - (cnt + 2) >= R_SPLIT) fork(MODE_SYM, i, cnt + 2, 0)
                    else solveSym(i, cnt + 2, depth + 1)
                    popTo(added)
                    st[m] = 4; st[c] = 4
                }
                // (2) 打破对称：只放 c，并临时排除镜像格 m（该支不可能再对称）
                st[c] = 5
                val added2 = uCount
                pushNeighbors(c)
                st[m] = st[m] or 2
                if (n - (cnt + 1) >= R_SPLIT) fork(MODE_ASYM, i, cnt + 1, xOf[c])
                else solveAsym(i, cnt + 1, xOf[c], depth + 1)
                st[m] = st[m] and 2.inv()
                popTo(added2)
                st[c] = 6
                // (3) 拒绝整对
                proc[pc++] = c
                st[m] = 6
                proc[pc++] = m
            }
        }
        for (j in pc - 1 downTo 0) st[proc[j]] = st[proc[j]] and 2.inv()
    }

    /** 非对称阶段：普通 Redelmeier 枚举 + 力矩剪枝（只统计 Σx = 0 的完成形状）。 */
    private fun solveAsym(from: Int, cnt: Int, sumx: Int, depth: Int) {
        val r = n - cnt
        if (sumx > maxCorr[r] || -sumx > maxCorr[r]) return
        if (cnt == n) { if (sumx == 0) count++; return }
        val proc = processed[depth]
        var pc = 0
        var i = from
        while (i < uCount) {
            val c = u[i]; i++
            if (st[c] != 4) continue
            st[c] = 5
            val added = uCount
            pushNeighbors(c)
            if (n - (cnt + 1) >= R_SPLIT) fork(MODE_ASYM, i, cnt + 1, sumx + xOf[c])
            else solveAsym(i, cnt + 1, sumx + xOf[c], depth + 1)
            popTo(added)
            st[c] = 6
            proc[pc++] = c
        }
        for (j in pc - 1 downTo 0) st[proc[j]] = st[proc[j]] and 2.inv()
    }
}
