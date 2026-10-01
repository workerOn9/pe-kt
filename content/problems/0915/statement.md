# 915 · 庞大公约数

> 中文意译。英文原文见 [Project Euler Problem 915](https://projecteuler.net/problem=915)；抓取底稿见 `statement.en.md`。

函数 $s(n)$ 对正整数递归定义为：$s(1) = 1$，且对于 $n\ge 1$：

$$
s(n+1) = \big(s(n) - 1\big)^3 + 2
$$

该序列的前几项为：$s(1) = 1, s(2) = 2, s(3) = 3, s(4) = 10, \dots$。

对于正整数 $N$，定义：

$$
T(N) = \sum_{a=1}^N \sum_{b=1}^N \gcd\Big(s\big(s(a)\big), s\big(s(b)\big)\Big)
$$

已知 $T(3) = 12$，$T(4) \equiv 24881925 \pmod{123456789}$ 以及 $T(100) \equiv 14416749 \pmod{123456789}$。

求 $T(10^8)$，并将答案对 $123456789$ 取模。
