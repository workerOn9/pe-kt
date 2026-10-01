# 934 · 不吉素数

> 中文意译。英文原文见 [Project Euler Problem 934](https://projecteuler.net/problem=934)；抓取底稿见 `statement.en.md`。

对于正整数 $n$，定义其**不吉素数**（unlucky prime）$u(n)$ 为使得 $n$ 除以 $p$ 的余数（即 $n \bmod p$）不是 7 的倍数的最小素数 $p$。
例如：
- $u(14) = 3$（因为 $14 \bmod 2 = 0 = 0 \times 7$，而 $14 \bmod 3 = 2$ 不是 7 的倍数）；
- $u(147) = 2$（因为 $147 \bmod 2 = 1$ 不是 7 的倍数）；
- $u(1470) = 13$。

设：

$$
U(N) = \sum_{n = 1}^N u(n)
$$

已知 $U(1470) = 4293$。

求 $U(10^{17})$。
