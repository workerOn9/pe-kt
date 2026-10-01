# 956 · 超级双重和

> 中文意译。英文原文见 [Project Euler Problem 956](https://projecteuler.net/problem=956)；抓取底稿见 `statement.en.md`。

正整数 $n$ 的质因数总个数（计重数）记为 $\Omega(n)$。
例如 $\Omega(12) = 3$，因为因数 2 计两次，因数 3 计一次。

定义 $D(n, m)$ 为 $n$ 的所有满足 $\Omega(d)$ 能被 $m$ 整除的约数 $d$ 之和。
例如 $D(24, 3) = 1 + 8 + 12 = 21$（因为 $\Omega(1) = 0$、$\Omega(8) = 3$、$\Omega(12) = 3$ 均能被 3 整除）。

正整数 $n$ 的超阶乘（superfactorial）通常记为 $n\text{\textdollar}$，定义为前 $n$ 个阶乘的乘积：

$$
n\text{\textdollar} = 1! \times 2! \times \cdots \times n!
$$

正整数 $n$ 的超级双重阶乘（superduperfactorial）记为 $n\bigstar$，定义为前 $n$ 个超阶乘的乘积：

$$
n\bigstar = 1\text{\textdollar} \times 2\text{\textdollar} \times \cdots \times n\text{\textdollar}
$$

已知 $D(6\bigstar, 6) = 6368195719791280$。

求 $D(1\,000\bigstar, 1\,000) \bmod 999\,999\,001$。
