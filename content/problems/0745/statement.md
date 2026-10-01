# 745 · 平方和 II

> 中文意译。英文原文见 [Project Euler Problem 745](https://projecteuler.net/problem=745)；抓取底稿见 `statement.en.md`。

对正整数 $n$，定义 $g(n)$ 为整除 $n$ 的最大完全平方数。例如 $g(18) = 9$，$g(19) = 1$。

再定义

$$
\displaystyle S(N) = \sum_{n=1}^N g(n)
$$

例如 $S(10) = 24$，$S(100) = 767$。

求 $S(10^{14})$，答案对 $1\,000\,000\,007$ 取模。
