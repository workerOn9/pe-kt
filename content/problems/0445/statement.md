# 445 · 收缩映射 A

> 中文意译。英文原文见 [Project Euler Problem 445](https://projecteuler.net/problem=445)；抓取底稿见 `statement.en.md`。

对每个整数 $n>1$，函数族 $f_{n,a,b}$ 定义为
$f_{n,a,b}(x)\equiv a x + b \mod n$，
其中 $a,b,x$ 为整数，且 $0< a <n$，$0 \le b < n$，$0 \le x < n$。

若对每个 $0 \le x < n$ 都有 $f_{n,a,b}(f_{n,a,b}(x)) \equiv f_{n,a,b}(x) \mod n$，则称 $f_{n,a,b}$ 为一个收缩映射。
记 $R(n)$ 为 $n$ 的收缩映射个数。

已知
$$
\sum_{k=1}^{99\,999} R(\binom {100\,000} k) \equiv 628701600 \mod 1\,000\,000\,007 .
$$

求
$$
\sum_{k=1}^{9\,999\,999} R(\binom {10\,000\,000} k) .
$$
答案模 $1\,000\,000\,007$。
