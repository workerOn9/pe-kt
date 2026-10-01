# 425 · 素数相连

> 中文意译。英文原文见 [Project Euler Problem 425](https://projecteuler.net/problem=425)；抓取底稿见 `statement.en.md`。

若两个正数 $A$ 与 $B$ 满足下列条件之一，则称它们**相连**（记作 $A \leftrightarrow B$）：

1. $A$ 与 $B$ 位数相同，且恰好有一位数字不同；例如 $123 \leftrightarrow 173$。
2. 在 $A$（或 $B$）左侧添上一位数字后得到 $B$（或 $A$）；例如 $23 \leftrightarrow 223$ 以及 $123 \leftrightarrow 23$。

称素数 $P$ 是 $2$ 的**亲戚**，如果存在一条由 $2$ 到 $P$ 的相连素数链，且链中没有任何素数超过 $P$。

例如 $127$ 是 $2$ 的亲戚，下面给出一条可能的链：

$$
2 \leftrightarrow 3 \leftrightarrow 13 \leftrightarrow 113 \leftrightarrow 103 \leftrightarrow 107 \leftrightarrow 127
$$

但 $11$ 与 $103$ 都不是 $2$ 的亲戚。

设 $F(N)$ 为所有不超过 $N$、且不是 $2$ 的亲戚的素数之和。

可以验证 $F(10^3) = 431$，$F(10^4) = 78728$。

求 $F(10^7)$。
