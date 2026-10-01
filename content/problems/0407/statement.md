# 407 · 幂等元

> 中文意译。英文原文见 [Project Euler Problem 407](https://projecteuler.net/problem=407)；抓取底稿见 `statement.en.md`。

若对 $0 \leq a \leq 5$ 计算 $a^2 \bmod 6$，结果为 $0,1,4,3,4,1$。

使 $a^2 \equiv a \bmod 6$ 成立的最大的 $a$ 为 $4$。
记 $M(n)$ 为满足 $a \lt n$ 且 $a^2 \equiv a \pmod n$ 的最大的 $a$。
于是 $M(6) = 4$。

求 $1 \leq n \leq 10^7$ 时 $\sum M(n)$。
