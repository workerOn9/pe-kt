# 381 · 素数阶乘之和模 p

> 中文意译。英文原文见 [Project Euler Problem 381](https://projecteuler.net/problem=381)；抓取底稿见 `statement.en.md`。

对素数 $p$，定义
$$
S(p) = \left(\sum_{k=1}^5 (p-k)!\right) \bmod p .
$$

例如 $p=7$ 时：
$$
(7-1)! + (7-2)! + (7-3)! + (7-4)! + (7-5)! = 6! + 5! + 4! + 3! + 2! = 720+120+24+6+2 = 872 .
$$
因为 $872 \bmod 7 = 4$，所以 $S(7)=4$。

可以验证 $5 \le p < 100$ 时 $\sum S(p) = 480$。

求 $5 \le p < 10^8$ 时 $\sum S(p)$。
