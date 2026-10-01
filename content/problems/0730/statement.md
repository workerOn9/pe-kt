# 730 · 平移毕达哥拉斯三元组

> 中文意译。英文原文见 [Project Euler Problem 730](https://projecteuler.net/problem=730)；抓取底稿见 `statement.en.md`。

对非负整数 $k$，若正整数三元组 $(p,q,r)$ 满足

$$
p^2 + q^2 + k = r^2
$$

则称其为 $k$-平移毕达哥拉斯三元组。

若 $\gcd(p, q, r)=1$，则称 $(p, q, r)$ 是本原的。

设 $P_k(n)$ 为满足 $1 \le p \le q \le r$ 且 $p + q + r \le n$ 的本原 $k$-平移毕达哥拉斯三元组的个数。
例如 $P_0(10^4) = 703$，$P_{20}(10^4) = 1979$。

定义

$$
\displaystyle S(m,n)=\sum_{k=0}^{m}P_k(n).
$$

已知 $S(10,10^4) = 10956$。

求 $S(10^2,10^8)$。
