# 526 · 连续数的最大素因子

> 中文意译。英文原文见 [Project Euler Problem 526](https://projecteuler.net/problem=526)；抓取底稿见 `statement.en.md`。

记 $f(n)$ 为 $n$ 的最大素因子。

记 $g(n) = f(n) + f(n + 1) + \cdots + f(n + 8)$，即从 $n$ 开始的九个连续数各自最大素因子之和。

记 $h(n)$ 为 $g(k)$ 在 $2 \le k \le n$ 上的最大值。

已知：

$f(100) = 5$
$f(101) = 101$
$g(100) = 409$
$h(100) = 417$
$h(10^9) = 4896292593$

求 $h(10^{16})$。
