/**
 * Project Euler 107 — Minimal Network（最小网络，对比实现）
 *
 * 思路：对照 solution.kt 的 Kruskal，这里用 Prim 算法——「长树」式贪心：从顶点 0
 * 出发维护一棵树，每轮在「树内顶点 ↔ 树外顶点」的横切边里选权重最小的一条把它连进
 * 树中，直到覆盖全部 40 个顶点。横切边用朴素的数组扫描找最小值（O(V) 每轮，
 * 不用堆），是稠密图上最教科书的写法，与 Kruskal 的「全局排序 + 并查集」在
 * 完全不同的代码路径上求解，二者结果一致即构成互证。节省 = 总权重 − MST 权重。
 *
 * 复杂度：O(V²) 时间（每轮线性扫描横切边）、O(V²) 空间（邻接矩阵），
 * V = 40 时约 1600 次比较，微秒级。结论应与 solution.kt（Kruskal）完全一致。
 *
 * 需从题目目录或仓库根目录运行（读取 network.txt）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    val file = listOf("network.txt", "content/problems/0107/network.txt", "../content/problems/0107/network.txt")
        .map { java.io.File(it) }.firstOrNull { it.isFile }
        ?: error("找不到 network.txt，请在题目目录或仓库根目录运行")

    // 邻接矩阵：weight[i][j] = 边权，无连接为 -1
    lateinit var weight: Array<IntArray>
    file.useLines { lines ->
        weight = lines.map { line ->
            line.split(',').map { cell -> cell.trim().toIntOrNull() ?: -1 }.toIntArray()
        }.toList().toTypedArray()
    }
    val n = weight.size
    check(weight.all { it.size == n }) { "network.txt 不是方阵" }

    val total = (0 until n).sumOf { i ->
        (i + 1 until n).sumOf { j -> if (weight[i][j] > 0) weight[i][j].toLong() else 0L }
    }

    // Prim：inTree[i] 标记顶点 i 是否在树中；minEdge[i] = 当前 i 到树的最小边权
    val inTree = BooleanArray(n)
    val minEdge = IntArray(n) { Int.MAX_VALUE }
    inTree[0] = true
    for (j in 1 until n) if (weight[0][j] >= 0) minEdge[j] = weight[0][j]

    var mstWeight = 0L
    repeat(n - 1) {
        // 朴素扫描：找树外 minEdge 最小的顶点
        var next = -1
        for (v in 0 until n) {
            if (!inTree[v] && (next == -1 || minEdge[v] < minEdge[next])) next = v
        }
        check(next >= 0 && minEdge[next] < Int.MAX_VALUE) { "图不连通，Prim 中断" }
        inTree[next] = true
        mstWeight += minEdge[next]
        // 用新入树顶点的边刷新所有树外顶点的横切边
        for (v in 0 until n) {
            if (!inTree[v] && weight[next][v] >= 0 && weight[next][v] < minEdge[v]) {
                minEdge[v] = weight[next][v]
            }
        }
    }
    check(inTree.all { it }) { "Prim 未覆盖全部顶点" }

    return total - mstWeight
}

fun main() { println(solveBruteForce()) }
