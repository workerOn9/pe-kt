# 738 · 计数有序因数分解

> 中文意译。英文原文见 [Project Euler Problem 738](https://projecteuler.net/problem=738)；抓取底稿见 `statement.en.md`。

定义 $d(n,k)$ 为把 $n$ 写成 $k$ 个有序整数之积的方法数：

$$
n = x_1\times x_2\times x_3\times \ldots\times x_k\qquad 1\le x_1\le x_2\le\ldots\le x_k
$$

进一步定义 $D(N,K)$ 为所有 $1\le n\le N$ 与 $1\le k\le K$ 的 $d(n,k)$ 之和。

已知 $D(10, 10) = 153$，$D(100, 100) = 35384$。

求 $D(10^{10},10^{10})$，答案对 $1\,000\,000\,007$ 取模。
