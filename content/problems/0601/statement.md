# 601 · 整除连续段

> 中文意译。英文原文见 [Project Euler Problem 601](https://projecteuler.net/problem=601)；抓取底稿见 `statement.en.md`。

对每个正整数 $n$，定义函数 $\mathop{streak}(n)=k$ 为最小的正整数 $k$，使得 $n+k$ 不能被 $k+1$ 整除。

例如：

$13$ 能被 $1$ 整除，

$14$ 能被 $2$ 整除，

$15$ 能被 $3$ 整除，

$16$ 能被 $4$ 整除，

$17$ 不能被 $5$ 整除，

所以 $\mathop{streak}(13) = 4$。

类似地：

$120$ 能被 $1$ 整除，

$121$ 不能被 $2$ 整除，

所以 $\mathop{streak}(120) = 1$。

定义 $P(s, N)$ 为满足 $1 \lt n \lt N$ 且 $\mathop{streak}(n) = s$ 的整数 $n$ 的个数。

于是 $P(3, 14) = 1$，$P(6, 10^6) = 14286$。

求 $\displaystyle\sum_{i=1}^{31} P(i, 4^i)$。
