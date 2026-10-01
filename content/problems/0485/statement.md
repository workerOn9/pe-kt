# 485 · 最大因子个数

> 中文意译。英文原文见 [Project Euler Problem 485](https://projecteuler.net/problem=485)；抓取底稿见 `statement.en.md`。

记 $d(n)$ 为 $n$ 的因子个数。

记 $M(n,k)$ 为区间 $n \le j \le n+k-1$ 上 $d(j)$ 的最大值。

记 $S(u,k)$ 为所有 $1 \le n \le u-k+1$ 的 $M(n,k)$ 之和。

已知 $S(1000,10)=17176$。

求 $S(100\,000\,000, 100\,000)$。
