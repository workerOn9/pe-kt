# 457 · 模素数平方的多项式

> 中文意译。英文原文见 [Project Euler Problem 457](https://projecteuler.net/problem=457)；抓取底稿见 `statement.en.md`。

设 $f(n) = n^2 - 3n - 1$。
设 $p$ 为素数。
记 $R(p)$ 为满足 $f(n) \bmod p^2 = 0$ 的最小正整数 $n$；若这样的整数不存在，则 $R(p) = 0$。

记 $SR(L)$ 为所有不超过 $L$ 的素数的 $R(p)$ 之和。

求 $SR(10^7)$。
