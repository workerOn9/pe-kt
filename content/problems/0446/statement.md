# 446 · 收缩映射 B

> 中文意译。英文原文见 [Project Euler Problem 446](https://projecteuler.net/problem=446)；抓取底稿见 `statement.en.md`。

对每个整数 $n>1$，函数族 $f_{n,a,b}$ 定义为
$f_{n,a,b}(x)\equiv a x + b \mod n$，
其中 $a,b,x$ 为整数，且 $0< a <n$，$0 \le b < n$，$0 \le x < n$。

若对每个 $0 \le x < n$ 都有 $f_{n,a,b}(f_{n,a,b}(x)) \equiv f_{n,a,b}(x) \mod n$，则称 $f_{n,a,b}$ 为一个收缩映射。
记 $R(n)$ 为 $n$ 的收缩映射个数。

$$
F(N)=\sum_{n=1}^N R(n^4+4) .
$$
$F(1024)=77532377300600$。

求 $F(10^7)$。
答案模 $1\,000\,000\,007$。
