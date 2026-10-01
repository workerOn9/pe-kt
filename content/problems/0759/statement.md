# 759 · 平方递推关系

> 中文意译。英文原文见 [Project Euler Problem 759](https://projecteuler.net/problem=759)；抓取底稿见 `statement.en.md`。

函数 $f$ 对所有正整数定义如下：

$$
\begin{aligned}
f(1) &=  1\\
f(2n) &= 2f(n)\\
f(2n+1) &= 2n+1 + 2f(n)+\tfrac 1n f(n)
\end{aligned}
$$

可以证明 $f(n)$ 对所有 $n$ 都是整数。

函数 $S(n)$ 定义为 $S(n) = \displaystyle \sum_{i=1}^n f(i) ^2$。

例如 $S(10)=1530$，$S(10^2)=4798445$。

求 $S(10^{16})$，答案对 $1\,000\,000\,007$ 取模。
