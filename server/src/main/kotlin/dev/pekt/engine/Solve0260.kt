package dev.pekt.engine

/**
 * PE 260 — Stone Game（取石子游戏）：三堆石子，每步选 N ≥ 1 与任意非空子集，被选中的堆各减 N，
 * 取走最后一颗者胜。求所有必败局面 x ≤ y ≤ z ≤ 1000 的 Σ(x+y+z)。
 *
 * 推导（详见 content/problems/0260/solution.kt 头部与 0260/analysis.md）：
 *   局面是排序后的多重集 (x ≤ y ≤ z)；必败局面 ⇔ 所有一步后继都是必胜局面。
 *   直接对每状态枚举「子集 × N」是 O(Z⁴)。三条唯一性引理把每个移法族压成 O(1) 查询——
 *   下列每种 key 至多对应一个必败局面（证明都是「两步并一步」：两个同 key 局面之间恰好一步可达）：
 *     L1 值对 {u,v} → 第三堆值 w（只减 w 那一堆）；
 *     L2 （未动值 u，被同减两堆之差 d）→ 基点 a（前两堆同减基点差）；
 *     L3 相邻间隔 (d1, d2) → 基点 x（三堆同减基点差）。
 *   于是 f1[u,v]、f2[u,d]、f3[d1,d2] 各存「唯一的那个坐标」或 −1，七族走法分别等价于：
 *     只减 x：f1[y,z] < x   只减 y：f1[x,z] < y   只减 z：f1[x,y] < z
 *     同减 xy：f2[z,y−x] < x   同减 yz：f2[x,z−y] < y   同减 xz：f2[y,z−x] < x
 *     三堆同减：f3[y−x,z−y] < x
 *   按 (x,y,z) 字典序递推（后继总和严格变小且字典序更小）。固定 (x,y) 至多一个必败局面：
 *   扫到第一个即停（更大的 z 一步可达它）；若值对 (x,y) 已在更早的行得到补全（补全 < y），
 *   整行都是必胜局面，可直接跳过——此时行内第 3 条查询恒假，每次只需 6 条查询。
 *
 * 复杂度：O(Z³) 个状态（Z=1000 实测仅扫 4.65×10⁷ 个）× 每状态至多 6 次 O(1) 查询，
 *   内存 O(Z²)（三张 (Z+1)² 的 IntArray，约 12 MB）。
 * 实测与校验：本机 JIT 预热后约 0.22 s（远低于 10 s 熔断线）；题面 Z=100 锚点得 173895，
 *   与 solution.kt 的方法 B（标记法）及直接暴力逐局面一致，完整答案 167542057。
 * 逻辑与 content/problems/0260/solution.kt 的最优路径（方法 A）一致。
 */
internal fun solve0260Impl(): Long {
    val zMax = 1000
    val sz = zMax + 1
    val f1 = IntArray(sz * sz) { -1 }   // 值对 {u,v} 的唯一补全（第三值）
    val f2 = IntArray(sz * sz) { -1 }   // （未动值 u，被同减两堆之差 d）的唯一基点
    val f3 = IntArray(sz * sz) { -1 }   // 相邻间隔 (d1,d2) 的唯一基点
    var sum = 0L
    for (x in 0..zMax) {
        for (y in x..zMax) {
            val yx = y - x
            if (f1[x * sz + y] != -1) continue          // 该值对的补全已在更早的行给出 → 整行必胜
            var z = y
            while (z <= zMax) {
                val zx = z - x
                val zy = z - y
                var t = f1[y * sz + z]; if (t >= 0 && t < x) { z++; continue }   // 只减 x 堆
                t = f1[x * sz + z]; if (t >= 0 && t < y) { z++; continue }       // 只减 y 堆
                t = f2[y * sz + zx]; if (t >= 0 && t < x) { z++; continue }      // 同减 x,z
                t = f2[x * sz + zy]; if (t >= 0 && t < y) { z++; continue }      // 同减 y,z
                t = f2[z * sz + yx]; if (t >= 0 && t < x) { z++; continue }      // 同减 x,y
                t = f3[yx * sz + zy]; if (t >= 0 && t < x) { z++; continue }     // 三堆同减
                // 必败局面：登记进三种结构（唯一性由三条引理保证）
                f1[y * sz + z] = x
                f1[x * sz + z] = y
                f1[x * sz + y] = z
                f2[z * sz + yx] = x
                f2[y * sz + zx] = x
                f2[x * sz + zy] = y
                f3[yx * sz + zy] = x
                sum += x + y + z
                break                                     // 本行唯一的必败局面；更大的 z 全部一步可达它
            }
        }
    }
    return sum
}
