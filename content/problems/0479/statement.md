# 479 · 不断升起的根

> 中文意译。英文原文见 [Project Euler Problem 479](https://projecteuler.net/problem=479)；抓取底稿见 `statement.en.md`。

设 $a_k$、$b_k$ 和 $c_k$ 表示方程

$$
\frac{1}{x} = \left(\frac{k}{x}\right)^2 (k + x^2) - k x
$$

的三个解（实数或复数）。

例如当 $k = 5$ 时，$\{a_5, b_5, c_5 \}$ 约为 $\{5.727244, -0.363622 + 2.057397i, -0.363622 - 2.057397i\}$。

令

$$
S(n) = \sum_{p=1}^{n} \sum_{k=1}^{n} (a_k + b_k)^p (b_k + c_k)^p (c_k + a_k)^p
$$

有意思的是，$S(n)$ 总是整数。例如 $S(4) = 51160$。

求 $S(10^6) \bmod 1\,000\,000\,007$。
