# 378 · 三角形数的因数三元组

> 中文意译。英文原文见 [Project Euler Problem 378](https://projecteuler.net/problem=378)；抓取底稿见 `statement.en.md`。

记 $T(n)$ 为第 $n$ 个三角形数，即 $T(n) = \dfrac{n(n+1)}{2}$。

记 $dT(n)$ 为 $T(n)$ 的因数个数。例如 $T(7)=28$，$dT(7)=6$。

记 $Tr(n)$ 为满足 $1 \le i < j < k \le n$ 且 $dT(i) > dT(j) > dT(k)$ 的三元组 $(i, j, k)$ 的个数。

已知 $Tr(20)=14$，$Tr(100)=5772$，$Tr(1000)=11174776$。

求 $Tr(60\,000\,000)$ 的最后 $18$ 位。
