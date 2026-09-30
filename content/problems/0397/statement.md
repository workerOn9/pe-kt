# 397 · 抛物线上的 45 度角

> 中文意译。英文原文见 [Project Euler Problem 397](https://projecteuler.net/problem=397)；抓取底稿见 `statement.en.md`。

在抛物线 $y = x^2/k$ 上选取三点 $A(a,\\, a^2/k)$、$B(b,\\, b^2/k)$、$C(c,\\, c^2/k)$。

记 $F(K, X)$ 为满足 $1 \\le k \\le K$、$-X \\le a < b < c \\le X$ 且三角形 $ABC$ 至少有一个角为 $45$ 度的整数四元组 $(k, a, b, c)$ 的个数。

例如 $F(1,10)=41$，$F(10,100)=12492$。

求 $F(10^6,\\, 10^9)$。
