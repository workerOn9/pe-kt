# 369 · Badugi 组合

> 中文意译。英文原文见 [Project Euler Problem 369](https://projecteuler.net/problem=369)；抓取底稿见 `statement.en.md`。

在一副标准的 $52$ 张扑克牌中，若 $4$ 张牌既没有对子、也没有两张花色相同，则称这 $4$ 张牌为一个 **Badugi**。

记 $f(n)$ 为选出 $n$ 张牌、且其中含有一个 $4$ 张 Badugi 子集的选法数。例如从标准 $52$ 张牌中选 $5$ 张共有 $2598960$ 种选法，其中 $514800$ 种含有 $4$ 张的 Badugi 子集，故 $f(5)=514800$。

求 $\displaystyle\sum_{n=4}^{13} f(n)$。
