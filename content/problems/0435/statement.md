# 435 · 斐波那契数多项式

> 中文意译。英文原文见 [Project Euler Problem 435](https://projecteuler.net/problem=435)；抓取底稿见 `statement.en.md`。

斐波那契数列 $\{f_n, n \ge 0\}$ 按 $f_n = f_{n-1} + f_{n-2}$ 递归定义，初始值为 $f_0 = 0$、$f_1 = 1$。

定义多项式 $\{F_n, n \ge 0\}$ 为
$$
F_n(x) = \sum_{i=0}^n f_i x^i .
$$

例如 $F_7(x) = x + x^2 + 2x^3 + 3x^4 + 5x^5 + 8x^6 + 13x^7$，且 $F_7(11) = 268\,357\,683$。

令 $n = 10^{15}$。求 $\displaystyle{\sum_{x=0}^{100} F_n(x)}$，答案对 $1\,307\,674\,368\,000$（$= 15!$）取模。
