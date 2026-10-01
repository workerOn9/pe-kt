# 586 · 二元二次型

> 中文意译。英文原文见 [Project Euler Problem 586](https://projecteuler.net/problem=586)；抓取底稿见 `statement.en.md`。

数 $209$ 可以有两种不同的方式表示为 $a^2 + 3ab + b^2$：

$$
209 = 8^2 + 3\cdot 8\cdot 5 + 5^2
$$

$$
209 = 13^2 + 3\cdot 13\cdot 1 + 1^2
$$

设 $f(n,r)$ 为不超过 $n$ 且能恰好以 $r$ 种不同方式表示为 $k=a^2 + 3ab + b^2$（其中 $a \gt b \gt 0$ 为整数）的整数 $k$ 的个数。

已知 $f(10^5, 4) = 237$，$f(10^8, 6) = 59517$。

求 $f(10^{15}, 40)$。
