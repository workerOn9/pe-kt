# 735 · 2n² 的因数

> 中文意译。英文原文见 [Project Euler Problem 735](https://projecteuler.net/problem=735)；抓取底稿见 `statement.en.md`。

设 $f(n)$ 为 $2n^2$ 的不超过 $n$ 的因数个数。例如 $f(15)=8$，因为有 8 个这样的因数：$1,2,3,5,6,9,10,15$。注意 18 也是 $2\times 15^2$ 的因数，但因它大于 15 而不计入。

设 $\displaystyle F(N) = \sum_{n=1}^N f(n)$。已知 $F(15)=63$，$F(1000)=15066$。

求 $F(10^{12})$。
