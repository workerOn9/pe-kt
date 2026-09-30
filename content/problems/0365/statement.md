# 365 · 巨大的二项式系数

> 中文意译。英文原文见 [Project Euler Problem 365](https://projecteuler.net/problem=365)；抓取底稿见 `statement.en.md`。

二项式系数 $\displaystyle\binom{10^{18}}{10^9}$ 是一个超过 $90$ 亿（$9\times 10^9$）位的数。

记 $M(n,k,m)$ 为二项式系数 $\displaystyle\binom{n}{k}$ 对 $m$ 取模的结果。

计算 $\displaystyle\sum M(10^{18},10^9,p\cdot q\cdot r)$，其中 $1000 < p < q < r < 5000$ 且 $p, q, r$ 均为素数。
