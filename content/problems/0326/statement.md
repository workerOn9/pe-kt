# 326 · 模和配对

> 中文意译。英文原文见 [Project Euler Problem 326](https://projecteuler.net/problem=326)；抓取底稿见 `statement.en.md`。

数列 $a_n$ 由如下递推定义：
$$
a_1 = 1 , \qquad a_n = \left(\sum_{k=1}^{n-1} k \cdot a_k\right) \bmod n \quad (n > 1) .
$$

$a_n$ 的前 $10$ 项依次为：$1, 1, 0, 3, 0, 3, 5, 4, 1, 9$。

记 $f(N, M)$ 为满足如下条件的数对 $(p, q)$ 的个数：
$$
1 \le p \le q \le N \quad\text{且}\quad \left(\sum_{i=p}^q a_i\right) \bmod M = 0 .
$$

已知 $f(10, 10) = 4$，对应的数对为 $(3,3)$、$(5,5)$、$(7,9)$ 与 $(9,10)$。

又已知 $f(10^4, 10^3) = 97158$。

求 $f(10^{12}, 10^6)$。
