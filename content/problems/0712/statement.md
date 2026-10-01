# 712 · 指数差

> 中文意译。英文原文见 [Project Euler Problem 712](https://projecteuler.net/problem=712)；抓取底稿见 `statement.en.md`。

对任意整数 $n>0$ 和质数 $p$，定义 $\nu_p(n)$ 为使得 $p^r$ 整除 $n$ 的最大整数 $r$。

定义

$$
D(n, m)  = \sum_{p \text{ prime}} \left| \nu_p(n) - \nu_p(m)\right|.
$$

例如，$D(14,24) = 4$。

再定义

$$
S(N) = \sum_{1 \le n, m \le N} D(n, m).
$$

已知 $S(10) = 210$，$S(10^2) = 37018$。

求 $S(10^{12})$，答案对 $1\,000\,000\,007$ 取模。
