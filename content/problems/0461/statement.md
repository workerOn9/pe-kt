# 461 · 近似圆周率

> 中文意译。英文原文见 [Project Euler Problem 461](https://projecteuler.net/problem=461)；抓取底稿见 `statement.en.md`。

对所有非负整数 $k$，令 $f_n(k) = e^{k/n} - 1$。

值得注意的是，

$$
f_{200}(6) + f_{200}(75) + f_{200}(89) + f_{200}(226) = \underline{3.1415926}44529\cdots \approx \pi
$$

事实上，在 $n=200$ 时，它是形如 $f_n(a) + f_n(b) + f_n(c) + f_n(d)$ 的所有数中对 $\pi$ 的最佳逼近。

令 $g(n) = a^2 + b^2 + c^2 + d^2$，其中 $a, b, c, d$ 使误差

$$
\left| f_n(a) + f_n(b) + f_n(c) + f_n(d) - \pi \right|
$$

最小（$|x|$ 表示 $x$ 的绝对值）。

已知 $g(200) = 6^2 + 75^2 + 89^2 + 226^2 = 64658$。

求 $g(10000)$。
