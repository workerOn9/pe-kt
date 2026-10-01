# 447 · 收缩映射 C

> 中文意译。英文原文见 [Project Euler Problem 447](https://projecteuler.net/problem=447)；抓取底稿见 `statement.en.md`。

对每个整数 $n>1$，函数族 $f_{n,a,b}$ 定义为
$f_{n,a,b}(x)\equiv a x + b \mod n$，
其中 $a,b,x$ 为整数，且 $0< a <n$，$0 \le b < n$，$0 \le x < n$。

若对每个 $0 \le x < n$ 都有 $f_{n,a,b}(f_{n,a,b}(x)) \equiv f_{n,a,b}(x) \mod n$，则称 $f_{n,a,b}$ 为一个收缩映射。
记 $R(n)$ 为 $n$ 的收缩映射个数。

$$
F(N)=\sum_{n=2}^N R(n) .
$$
$F(10^7)\equiv 638042271 \mod 1\,000\,000\,007$。

求 $F(10^{14})$。
答案模 $1\,000\,000\,007$。
