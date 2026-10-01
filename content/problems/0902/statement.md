# 902 · 置换幂与字典序排名

> 中文意译。英文原文见 [Project Euler Problem 902](https://projecteuler.net/problem=902)；抓取底稿见 `statement.en.md`。

集合 $\{1, \dots, n\}$ 的一个置换 $\pi$ 可以用单行记法表示为 $\pi(1),\ldots,\pi(n)$。如果将全部 $n!$ 个置换按字典序排列，则 $\operatorname{rank}(\pi)$ 是 $\pi$ 在这个基于 1 开始的列表中的位置。

例如，$\operatorname{rank}(2,1,3) = 3$，因为 $\{1, 2, 3\}$ 的六个置换按字典序排列依次为：

$$
1, 2, 3\quad 1, 3, 2 \quad 2, 1, 3 \quad 2, 3, 1 \quad 3, 1, 2 \quad 3, 2, 1
$$

对于正整数 $m$，取 $n = \frac{m(m+1)}{2}$，我们定义集合 $\{1, \dots, n\}$ 上的置换如下：

$$
\begin{aligned}
\sigma(i) &= \begin{cases} \frac{k(k-1)}{2} + 1 & \text{若 } i = \frac{k(k + 1)}{2} \text{ 且 } k\in\{1, \dots, m\}；\\ i + 1 & \text{其他情况}；\end{cases}\\
\tau(i) &= ((10^9 + 7)i \bmod n) + 1\\
\pi(i) &= \tau^{-1}(\sigma(\tau(i)))
\end{aligned}
$$

其中 $\tau^{-1}$ 是 $\tau$ 的逆置换。

定义：

$$
P(m) = \sum_{k=1}^{m!} \operatorname{rank}(\pi^k)
$$

其中 $\pi^k$ 是将置换 $\pi$ 重复应用 $k$ 次所得到的置换。
已知 $P(2) = 4$，$P(3) = 780$ 以及 $P(4) = 38810300$。

求 $P(100)$，并将答案对 $10^9 + 7$ 取模。
