# 521 · 最小素因子

> 中文意译。英文原文见 [Project Euler Problem 521](https://projecteuler.net/problem=521)；抓取底稿见 `statement.en.md`。

记 $\operatorname{smpf}(n)$ 为 $n$ 的最小素因子。
$\operatorname{smpf}(91)=7$，因为 $91=7\times 13$；$\operatorname{smpf}(45)=3$，因为 $45=3\times 3\times 5$。
记 $S(n)$ 为 $\operatorname{smpf}(i)$ 对 $2 \le i \le n$ 求和的结果。
例如 $S(100)=1257$。

求 $S(10^{12}) \bmod 10^9$。
