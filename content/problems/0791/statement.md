# 791 · 平均值与方差

> 中文意译。英文原文见 [Project Euler Problem 791](https://projecteuler.net/problem=791)；抓取底稿见 `statement.en.md`。

记 $k$ 个数 $x_1, ..., x_k$ 的平均值为 $\bar{x} = \frac{1}{k} \sum_i x_i$。它们的方差定义为 $\frac{1}{k} \sum_i \left( x_i - \bar{x} \right) ^ 2$。

记 $S(n)$ 为满足 $1 \leq a \leq b \leq c \leq d \leq n$ 且其平均值恰好等于其方差两倍的所有整数四元组 $(a,b,c,d)$ 之和。

对于 $n=5$，有 $5$ 个这样的四元组，即：$(1, 1, 1, 3), (1, 1, 3, 3), (1, 2, 3, 4), (1, 3, 4, 4), (2, 2, 3, 5)$。

因此 $S(5)=48$。还已知 $S(10^3)=37048340$。

求 $S(10^8)$，答案对 $433494437$ 取模。
