# 884 · 连减完全立方数

> 中文意译。英文原文见 [Project Euler Problem 884](https://projecteuler.net/problem=884)；抓取底稿见 `statement.en.md`。

从一个正整数 $n$ 开始，在每一步中，我们从 $n$ 中减去不超过 $n$ 的最大完全立方数，直到 $n$ 变为 $0$。
例如，当 $n = 100$ 时，该过程在 $4$ 步内结束：

$$
100 \xrightarrow{-4^3} 36 \xrightarrow{-3^3} 9 \xrightarrow{-2^3} 1 \xrightarrow{-1^3} 0
$$

记 $D(n)$ 为该过程的步数。因此 $D(100) = 4$。

记 $S(N)$ 为所有严格小于 $N$ 的正整数 $n$ 的 $D(n)$ 之和。
例如，$S(100) = 512$。

求 $S(10^{17})$。
