# 403 · 抛物线与直线围成的区域内的格点

> 中文意译。英文原文见 [Project Euler Problem 403](https://projecteuler.net/problem=403)；抓取底稿见 `statement.en.md`。

对整数 $a$ 与 $b$，定义 $D(a, b)$ 为抛物线 $y = x^2$ 与直线 $y = a\cdot x + b$ 围成的区域：

$$
D(a, b) = \{(x, y) \mid x^2 \leq y \leq a\cdot x + b\}
$$

$L(a, b)$ 定义为 $D(a, b)$ 中所含格点的个数。例如 $L(1, 2) = 8$，$L(2, -1) = 1$。

再定义 $S(N)$ 为所有使 $D(a, b)$ 的面积为有理数、且 $|a|,|b| \leq N$ 的数对 $(a, b)$ 的 $L(a, b)$ 之和。
可以验证 $S(5) = 344$ 与 $S(100) = 26709528$。

求 $S(10^{12})$，答案取模 $10^8$。
