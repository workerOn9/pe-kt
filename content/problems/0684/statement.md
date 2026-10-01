# 684 · 反数位和

> 中文意译。英文原文见 [Project Euler Problem 684](https://projecteuler.net/problem=684)；抓取底稿见 `statement.en.md`。

定义 $s(n)$ 为数字和为 $n$ 的最小数。例如 $s(10) = 19$。
记 $\displaystyle S(k) = \sum_{n=1}^k s(n)$。已知 $S(20) = 1074$。

再设 $f_i$ 为由 $f_0=0$、$f_1=1$ 及对所有 $i \ge 2$ 有 $f_i=f_{i-2}+f_{i-1}$ 定义的斐波那契数列。

求 $\displaystyle \sum_{i=2}^{90} S(f_i)$，答案对 $1\,000\,000\,007$ 取模。
