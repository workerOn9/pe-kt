package dev.pekt.visualize

/**
 * 可视化生成器注册表：题号 → 生成器。
 * 必须与 content/problems/00XX/meta.json 的 hasVisualization 保持一致。
 */
object Visualizations {
    val visualizations: Map<Int, () -> Visualization> = mapOf(
        7 to ::sieve007,
        11 to ::gridMaxProduct011,
        14 to ::collatz014,
        15 to ::latticePaths015,
        18 to ::pathDp018,
    )
}
