# 968 · 五维求和

> 中文意译。英文原文见 [Project Euler Problem 968](https://projecteuler.net/problem=968)；抓取底稿见 `statement.en.md`。

定义

$$
P(X_{a,b},X_{a,c},X_{a,d},X_{a,e},X_{b,c},X_{b,d},X_{b,e},X_{c,d},X_{c,e},X_{d,e})
$$

为所有满足五个变量中任意两个变量之和均受给定值限制的非负整数五元组 $(a, b, c, d, e)$ 对应的 $2^a3^b5^c7^d11^e$ 之和。换言之，满足 $a+b \le X_{a,b}$、$a+d \le X_{a,d}$、$b+e \le X_{b,e}$ 等条件。

例如，$P(2,2,2,2,2,2,2,2,2,2)=7120$ 且 $P(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) \equiv 799809376 \pmod{10^9 + 7}$。

定义序列 $A$ 如下：

- $A_0 = 1$，$A_1 = 7$；
- 当 $n \ge 2$ 时，$A_n =(7A_{n-1}+A_{n-2}^2) \bmod (10^9+7)$。

并定义 $Q(n) = P(A_{10n}, A_{10n+1}, A_{10n+2}, \dots , A_{10n+9})$。

求 $\displaystyle\sum_{0 \le n < 100}Q(n)$ 对 $10^9+7$ 取模的结果。
