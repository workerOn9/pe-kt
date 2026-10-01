# 999 · 交替递推

> 中文意译。英文原文见 [Project Euler Problem 999](https://projecteuler.net/problem=999)；抓取底稿见 `statement.en.md`。

存在唯一的整数序列 $a_n$ 满足：

- $a_1=a_2=a_3=1$，$a_4=2$；
- $a_n^2 = a_{n+2}a_{n-2} + u\cdot a_{n+1}a_{n-1}$，其中当 $n$ 为偶数时 $u=1$，当 $n$ 为奇数时 $u=2$。

例如，$a_{13} = 23321$ 以及 $a_{1003} \equiv 231906014 \pmod{1234567891}$。

对于 $n = 10^{18} + 3$，求 $a_n \bmod 1234567891$。
