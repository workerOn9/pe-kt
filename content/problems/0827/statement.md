# 827 · 勾股三元组出现次数

> 中文意译。英文原文见 [Project Euler Problem 827](https://projecteuler.net/problem=827)；抓取底稿见 `statement.en.md`。

定义 $Q(n)$ 为恰好出现在 $n$ 个勾股三元组 $(a,b,c)$（其中 $a \lt b \lt c$）中的最小数。

例如，$15$ 是恰好出现在 $5$ 个勾股三元组中的最小数：

$$
(9,12,\mathbf{15})\quad (8,\mathbf{15},17)\quad (\mathbf{15},20,25)\quad (\mathbf{15},36,39)\quad (\mathbf{15},112,113)
$$

因此 $Q(5) = 15$。

此外已知 $Q(10)=48$，$Q(10^3)=8064000$。

求 $\displaystyle \sum_{k=1}^{18} Q(10^k)$，答案对 $409120391$ 取模。
