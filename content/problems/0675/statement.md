# 675 · $2^{\omega(n)}$

> 中文意译。英文原文见 [Project Euler Problem 675](https://projecteuler.net/problem=675)；抓取底稿见 `statement.en.md`。

设 $\omega(n)$ 表示正整数 $n$ 的不同质因数个数。
所以 $\omega(1) = 0$，$\omega(360) = \omega(2^{3} \times 3^{2} \times 5) = 3$。

设 $S(n)$ 为 $ \sum_{d \mid n} 2^{\omega(d)} $。
例如 $S(6) = 2^{\omega(1)}+2^{\omega(2)}+2^{\omega(3)}+2^{\omega(6)} = 2^0+2^1+2^1+2^2 = 9$。

设 $F(n)=\sum_{i=2}^n S(i!)$。$F(10)=4821$。

求 $F(10\,000\,000)$，答案模 $1\,000\,000\,087$。
