# 755 · 非 Zeckendorf

> 中文意译。英文原文见 [Project Euler Problem 755](https://projecteuler.net/problem=755)；抓取底稿见 `statement.en.md`。

考虑斐波那契数列 $\{1,2,3,5,8,13,21,\ldots\}$。

设 $f(n)$ 为把整数 $n\ge 0$ 表示为不同斐波那契数之和的方法数。例如 $16 = 3+13 = 1+2+13 = 3+5+8 = 1+2+5+8$，因此 $f(16) = 4$。按约定 $f(0) = 1$。

进一步定义

$$
S(n) = \sum_{k=0}^n f(k).
$$

已知 $S(100) = 415$，$S(10^4) = 312807$。

求 $\displaystyle S(10^{13})$。
