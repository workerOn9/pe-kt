# 528 · 受限求和

> 中文意译。英文原文见 [Project Euler Problem 528](https://projecteuler.net/problem=528)；抓取底稿见 `statement.en.md`。

记 $S(n, k, b)$ 为满足 $x_1 + x_2 + \cdots + x_k \le n$ 的合法解的个数，其中对一切 $1 \le m \le k$ 有 $0 \le x_m \le b^m$。

例如 $S(14,3,2) = 135$，$S(200,5,3) = 12949440$，以及 $S(1000,10,5) \bmod 1\,000\,000\,007 = 624839075$。

求 $(\sum_{10 \le k \le 15} S(10^k, k, k)) \bmod 1\,000\,000\,007$。
