# 699 · Triffle 数

> 中文意译。英文原文见 [Project Euler Problem 699](https://projecteuler.net/problem=699)；抓取底稿见 `statement.en.md`。

记 $\sigma(n)$ 为正整数 $n$ 的所有除数之和，例如：$\sigma(10) = 1+2+5+10 = 18$。

定义 $T(N)$ 为所有满足如下条件的数 $n \le N$ 之和：当分数 $\frac{\sigma(n)}{n}$ 写成最简形式 $\frac ab$ 时，分母是 3 的幂，即 $b = 3^k, k > 0$。

已知 $T(100) = 270$，$T(10^6) = 26089287$。

求 $T(10^{14})$。
