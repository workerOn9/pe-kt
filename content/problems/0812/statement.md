# 812 · 动力多项式

> 中文意译。英文原文见 [Project Euler Problem 812](https://projecteuler.net/problem=812)；抓取底稿见 `statement.en.md`。

动力多项式是指整系数首一多项式 $f(x)$，使得 $f(x)$ 整除 $f(x^2-2)$。

例如，$f(x) = x^2 - x - 2$ 是动力多项式，因为 $f(x^2-2) = x^4-5x^2+4 = (x^2 + x -2)f(x)$。

记 $S(n)$ 为 $n$ 次动力多项式的个数。
例如 $S(2)=6$，因为共有六个 2 次动力多项式：

$$
 x^2-4x+4 \quad,\quad x^2-x-2 \quad,\quad x^2-4 \quad,\quad x^2-1 \quad,\quad x^2+x-1 \quad,\quad x^2+2x+1
$$

此外，$S(5)=58$，$S(20)=122087$。

求 $S(10\,000)$，答案对 $998244353$ 取模。
