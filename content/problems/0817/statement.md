# 817 · 平方中的数字

> 中文意译。英文原文见 [Project Euler Problem 817](https://projecteuler.net/problem=817)；抓取底稿见 `statement.en.md`。

定义 $m = M(n, d)$ 为使 $m^2$ 写成 $n$ 进制时含有 $n$ 进制数字 $d$ 的最小正整数。例如 $M(10,7) = 24$，因为若把所有平方数写成十进制，数字 7 首次出现在 $24^2 = 576$ 中。又如 $M(11,10) = 19$，因为 $19^2 = 361=2A9_{11}$。

求 $\displaystyle \sum_{d = 1}^{10^5}M(p, p - d)$，其中 $p = 10^9 + 7$。
