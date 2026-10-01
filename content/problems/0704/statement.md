# 704 · 二项式系数中的因子二

> 中文意译。英文原文见 [Project Euler Problem 704](https://projecteuler.net/problem=704)；抓取底稿见 `statement.en.md`。

定义 $g(n, m)$ 为使得 $2^k$ 整除 $\binom{n}m$ 的最大整数 $k$。例如 $\binom{12}5 = 792 = 2^3 \cdot 3^2 \cdot 11$，因此 $g(12, 5) = 3$。再定义 $F(n) = \max \{ g(n, m) : 0 \le m \le n \}$。$F(10) = 3$，$F(100) = 6$。

记 $S(N)$ = $\displaystyle\sum_{n=1}^N{F(n)}$。已知 $S(100) = 389$，$S(10^7) = 203222840$。

求 $S(10^{16})$。
