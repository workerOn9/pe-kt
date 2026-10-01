# 947 · 斐波那契余数序列

> 中文意译。英文原文见 [Project Euler Problem 947](https://projecteuler.net/problem=947)；抓取底稿见 `statement.en.md`。

对于整数 $0 \le a, b < m$，$(a, b, m)$-序列定义为：

$$
\begin{aligned}
g(0) &= a \\
g(1) &= b \\
g(n) &= \big(g(n-1) + g(n-2)\big) \bmod m
\end{aligned}
$$

所有 $(a, b, m)$-序列均具有周期性，记其最小正周期为 $p(a, b, m)$。
例如，$(0, 1, 8)$-序列的前若干项为 $(0, 1, 1, 2, 3, 5, 0, 5, 5, 2, 7, 1, 0, 1, 1, 2, \dots)$，其周期为 $p(0, 1, 8) = 12$。

设：

$$
s(m) = \sum_{a=0}^{m-1} \sum_{b=0}^{m-1} p(a, b, m)^2
$$

例如，$s(3) = 513$ 且 $s(10) = 225820$。

定义：

$$
S(M) = \sum_{m=1}^M s(m)
$$

已知 $S(3) = 542$ 且 $S(10) = 310897$。

求 $S(10^6) \bmod 999\,999\,893$。
