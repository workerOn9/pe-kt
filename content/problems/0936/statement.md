# 936 · 无偶度树

> 中文意译。英文原文见 [Project Euler Problem 936](https://projecteuler.net/problem=936)；抓取底稿见 `statement.en.md`。

无偶度树（peerless tree）是指任意两个相邻顶点度数均不相等的无向树。设 $P(n)$ 为具有 $n$ 个无标号顶点的无偶度树的数目。

在 7 个无标号顶点上共有 6 棵无偶度树，即 $P(7) = 6$，如下图所示：

![7个顶点的6棵无偶度树](0936_diagram.jpg)

定义：

$$
S(N) = \sum_{n=3}^N P(n)
$$

已知 $S(10) = 74$。

求 $S(50)$。
