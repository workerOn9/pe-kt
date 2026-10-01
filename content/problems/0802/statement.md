# 802 · 迭代复合

> 中文意译。英文原文见 [Project Euler Problem 802](https://projecteuler.net/problem=802)；抓取底稿见 `statement.en.md`。

设 $\Bbb R^2$ 为实数对 $(x, y)$ 的集合，$\pi = 3.14159\cdots$。

考虑由 $f(x, y) = (x^2 - x - y^2, 2xy - y + \pi)$ 定义的函数 $f:\Bbb R^2 \to \Bbb R^2$，及其 $n$ 次迭代复合 $f^{(n)}(x, y) = f(f(\cdots f(x, y)\cdots))$。例如 $f^{(3)}(x, y) = f(f(f(x, y)))$。若 $n$ 是使 $f^{(n)}(x, y) = (x, y)$ 成立的最小正整数，则称点对 $(x, y)$ 的周期为 $n$。

记 $P(n)$ 为所有周期不超过 $n$ 的点对的 $x$ 坐标之和。有趣的是，$P(n)$ 总是整数。例如 $P(1) = 2$，$P(2) = 2$，$P(3) = 4$。

求 $P(10^7)$，答案对 $1\,020\,340\,567$ 取模。
