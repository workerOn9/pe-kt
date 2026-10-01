# 792 · 太多 2

> 中文意译。英文原文见 [Project Euler Problem 792](https://projecteuler.net/problem=792)；抓取底稿见 `statement.en.md`。

定义 $\nu_2(n)$ 为使得 $2^r$ 整除 $n$ 的最大整数 $r$。例如，$\nu_2(24) = 3$。

定义 $\displaystyle S(n) = \sum_{k = 1}^n (-2)^k\binom{2k}k$，并令 $u(n) = \nu_2\Big(3S(n)+4\Big)$。

例如，当 $n = 4$ 时，$S(4) = 980$，$3S(4) + 4 = 2944 = 2^7 \cdot 23$，因此 $u(4) = 7$。
还已知 $u(20) = 24$。

再定义 $\displaystyle U(N) = \sum_{n = 1}^N u(n^3)$。已知 $U(5) = 241$。

求 $U(10^4)$。
