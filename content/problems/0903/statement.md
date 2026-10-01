# 903 · 全置换幂字典序之和

> 中文意译。英文原文见 [Project Euler Problem 903](https://projecteuler.net/problem=903)；抓取底稿见 `statement.en.md`。

集合 $\{1, \dots, n\}$ 的一个置换 $\pi$ 可以用单行记法表示为 $\pi(1),\ldots,\pi(n)$。如果将全部 $n!$ 个置换按字典序排列，则 $\operatorname{rank}(\pi)$ 是 $\pi$ 在这个基于 1 开始的列表中的位置。

例如，$\operatorname{rank}(2,1,3) = 3$，因为 $\{1, 2, 3\}$ 的六个置换按字典序排列依次为：

$$
1, 2, 3\quad 1, 3, 2 \quad 2, 1, 3 \quad 2, 3, 1 \quad 3, 1, 2 \quad 3, 2, 1
$$

设 $Q(n)$ 为：

$$
Q(n) = \sum_{\pi}\sum_{i = 1}^{n!} \operatorname{rank}(\pi^i)
$$

其中 $\pi$ 取遍集合 $\{1, \dots, n\}$ 的所有置换，$\pi^i$ 是将置换 $\pi$ 重复应用 $i$ 次得到的置换。

已知 $Q(2) = 5$，$Q(3) = 88$，$Q(6) = 133103808$ 以及 $Q(10) \equiv 468421536 \pmod{10^9 + 7}$。

求 $Q(10^6)$，并将答案对 $10^9 + 7$ 取模。
