# 362 · 无平方因子分解

> 中文意译。英文原文见 [Project Euler Problem 362](https://projecteuler.net/problem=362)；抓取底稿见 `statement.en.md`。

考虑数 $54$。

$54$ 可以分解成若干个大于 $1$ 的因子，共有 $7$ 种互不相同的分解方式：$54$、$2\times 27$、$3\times 18$、$6\times 9$、$3\times 3\times 6$、$2\times 3\times 9$、$2\times 3\times 3\times 3$。

若要求所有因子都**无平方因子**，则只剩两种：$3\times 3\times 6$ 与 $2\times 3\times 3\times 3$。

记 $\operatorname{Fsf}(n)$ 为 $n$ 分解成若干个大于 $1$ 的无平方因子因子的方式数，故 $\operatorname{Fsf}(54)=2$。

记 $S(n) = \displaystyle\sum_{k=2}^{n} \operatorname{Fsf}(k)$。

已知 $S(100)=193$。

求 $S(10\,000\,000\,000)$。
