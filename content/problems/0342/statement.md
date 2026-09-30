# 342 · 平方的欧拉函数是完全立方

> 中文意译。英文原文见 [Project Euler Problem 342](https://projecteuler.net/problem=342)；抓取底稿见 `statement.en.md`。

考虑数 $50$。由于
$$
50^2 = 2500 = 2^2 \times 5^4 , \qquad \phi(2500) = 2 \times 4 \times 5^3 = 8 \times 5^3 = 2^3 \times 5^3 ,
$$
所以 $2500$ 是完全平方数，而 $\phi(2500)$ 是完全立方数。

求所有满足 $1 < n < 10^{10}$ 且 $\phi(n^2)$ 是完全立方数的 $n$ 之和。

（注：$\phi$ 表示欧拉函数。）
