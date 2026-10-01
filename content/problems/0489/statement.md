# 489 · 两序列的最大公因数

> 中文意译。英文原文见 [Project Euler Problem 489](https://projecteuler.net/problem=489)；抓取底稿见 `statement.en.md`。

记 $G(a, b)$ 为使 $\gcd(n^3 + b, (n + a)^3 + b)$ 取得最大值的最小非负整数 $n$。

例如 $G(1, 1) = 5$，因为当 $n = 5$ 时 $\gcd(n^3 + 1, (n + 1)^3 + 1)$ 达到最大值 $7$，而在 $0 \le n \lt 5$ 时它都更小。

记 $H(m, n) = \sum G(a, b)$，其中 $1 \le a \le m$、$1 \le b \le n$。

已知 $H(5, 5) = 128878$，$H(10, 10) = 32936544$。

求 $H(18, 1900)$。
