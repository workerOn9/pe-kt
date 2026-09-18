# 123 · 素数平方剩余

> 中文意译。英文原文见 [Project Euler Problem 123](https://projecteuler.net/problem=123)；抓取底稿见 `statement.en.md`。

设 $p_n$ 为第 $n$ 个素数：$2, 3, 5, 7, 11, \dots$；设 $r$ 为 $(p_n - 1)^n + (p_n + 1)^n$ 除以 $p_n^2$ 所得的余数。

例如 $n = 3$ 时 $p_3 = 5$，$4^3 + 6^3 = 280 \equiv 5 \pmod{25}$。

使余数首次超过 $10^9$ 的最小 $n$ 是 $7037$。

求使余数首次超过 $10^{10}$ 的最小 $n$。
