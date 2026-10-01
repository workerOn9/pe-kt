# 801 · $x^y \equiv y^x$

> 中文意译。英文原文见 [Project Euler Problem 801](https://projecteuler.net/problem=801)；抓取底稿见 `statement.en.md`。

方程 $x^y=y^x$ 的正整数解为 $(2,4)$、$(4,2)$，以及所有 $k > 0$ 对应的 $(k,k)$。

对给定的正整数 $n$，记 $f(n)$ 为满足 $0 < x,y \leq n^2-n$ 且

$$
x^y\equiv y^x \pmod n
$$

的整数组 $(x,y)$ 的个数。

例如，$f(5)=104$，$f(97)=1614336$。

设 $S(M,N)=\sum f(p)$，其中求和遍历所有满足 $M\le p\le N$ 的素数 $p$。

已知 $S(1,10^2)=7381000$，且 $S(1,10^5) \equiv 701331986 \pmod{993353399}$。

求 $S(10^{16}, 10^{16}+10^6)$，答案对 $993353399$ 取模。
