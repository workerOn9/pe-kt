/**
 * Project Euler 107 — Minimal Network（最小网络）
 *
 * 思路：题目要求「保持连通的最大节省」正是图论中的最小生成树问题——保留一组使所有
 * 顶点连通且总权重最小的边，节省 = 原总权重 − MST 权重。用 Kruskal：把全部边按权
 * 重升序排序，用并查集从小到大贪心加边，一条边的两端点已连通（find 同根）就跳过，
 * 否则并入并累加权重，直到选出 V−1 条边为止。矩阵是对称的，只取 i < j 的上三角。
 * 7 顶点样例矩阵（总权重 243、MST 93、节省 150）在 main 里做自检。
 *
 * 复杂度：E = 40 顶点网络的边数，排序 O(E log E)，并查集操作近乎 O(1)
 * （带路径压缩 + 按秩合并，反阿克曼量级），整体 O(E log E)。空间 O(V + E)。
 * 权重为两位数，Long 加和毫无溢出压力。
 *
 * 需从题目目录或仓库根目录运行（读取 network.txt）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 并查集：parent[x] = 父节点（根为自身），负值大小表示按秩合并的权重。 */
class UnionFind(size: Int) {
    private val parent = IntArray(size) { it }
    private val rank = IntArray(size)

    fun find(x: Int): Int {
        var root = x
        while (parent[root] != root) root = parent[root]
        // 路径压缩
        var cur = x
        while (cur != root) { val next = parent[cur]; parent[cur] = root; cur = next }
        return root
    }

    /** 合并成功返回 true，二者本就连通返回 false。 */
    fun union(a: Int, b: Int): Boolean {
        var ra = find(a); var rb = find(b)
        if (ra == rb) return false
        if (rank[ra] < rank[rb]) { val t = ra; ra = rb; rb = t }
        parent[rb] = ra
        if (rank[ra] == rank[rb]) rank[ra]++
        return true
    }
}

/** 从邻接矩阵文件读入 (u, v, weight) 边列表（只取 i < j 的上三角）。 */
fun loadEdges(file: java.io.File): List<Triple<Int, Int, Int>> {
    val edges = mutableListOf<Triple<Int, Int, Int>>()
    file.useLines { lines ->
        lines.forEachIndexed { i, line ->
            line.split(',').forEachIndexed { j, cell ->
                val w = cell.trim().toIntOrNull() ?: return@forEachIndexed
                if (i < j) edges.add(Triple(i, j, w))
            }
        }
    }
    return edges
}

fun findDataFile(): java.io.File =
    listOf("network.txt", "content/problems/0107/network.txt", "../content/problems/0107/network.txt")
        .map { java.io.File(it) }.firstOrNull { it.isFile }
        ?: error("找不到 network.txt，请在题目目录或仓库根目录运行")

fun solve(): Long {
    val file = findDataFile()
    val edges = loadEdges(file)
    check(edges.isNotEmpty()) { "network.txt 没有有效边" }
    val vertices = edges.flatMap { listOf(it.first, it.second) }.max() + 1
    val total = edges.sumOf { it.third.toLong() }
    val uf = UnionFind(vertices)
    var mstWeight = 0L
    var picked = 0
    for ((u, v, w) in edges.sortedBy { it.third }) {
        if (uf.union(u, v)) {
            mstWeight += w
            if (++picked == vertices - 1) break
        }
    }
    check(picked == vertices - 1) { "图不连通：只选出 $picked / ${vertices - 1} 条边" }
    return total - mstWeight
}

/** 题面 7 顶点样例的自检：总权重 243，MST 权重 93，节省 150。 */
fun sampleCheck() {
    val rows = listOf(
        "-,16,12,21,-,-,-",
        "16,-,-,17,20,-,-",
        "12,-,-,28,-,31,-",
        "21,17,28,-,18,19,23",
        "-,20,-,18,-,-,11",
        "-,-,31,19,-,-,27",
        "-,-,-,23,11,27,-",
    )
    val tmp = java.io.File.createTempFile("pe107-sample", ".txt")
    tmp.writeText(rows.joinToString("\n") + "\n")
    tmp.deleteOnExit()
    val edges = loadEdges(tmp)
    val total = edges.sumOf { it.third.toLong() }
    val uf = UnionFind(7)
    var mst = 0L
    for ((u, v, w) in edges.sortedBy { it.third }) {
        if (uf.union(u, v)) mst += w
    }
    check(total == 243L && mst == 93L) { "样例自检失败：total=$total, mst=$mst" }
}

fun main() {
    sampleCheck()
    println(solve())
}
