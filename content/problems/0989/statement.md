# 989 · 斐波那契求和

> 中文意译。英文原文见 [Project Euler Problem 989](https://projecteuler.net/problem=989)；抓取底稿见 `statement.en.md`。

记 $F_n$ 为第 $n$ 个斐波那契数，其中 $F_1 = F_2 = 1$ 且 $F_{n+1} = F_n + F_{n-1}$。

已知 $F_n$ 可以被 $\varphi^n / \sqrt{5}$ 极好地近似，其中黄金分割比 $\varphi$ 是方程 $x^2 = x+1$ 的正根。

设 $G(n)$ 为满足 $x^2 \equiv x+1 \pmod n$ 的不同整数 $0 \leq x < n$ 的个数。

已知 $\displaystyle\sum_{n=1}^{10^3}F_nG(n)\equiv 190950976\bmod (10^9+9)$。

求 $\displaystyle\sum_{n=1}^{10^{14}}F_nG(n)$ 对 $10^9+9$ 取模的结果。
