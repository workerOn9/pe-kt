# 672 · 再加一个一

> 中文意译。英文原文见 [Project Euler Problem 672](https://projecteuler.net/problem=672)；抓取底稿见 `statement.en.md`。

考虑以下可递归地施加于任意正整数 $n$ 的过程：

若 $n = 1$，什么也不做，过程停止，
若 $n$ 能被 $7$ 整除，则除以 $7$，
否则加 $1$。

定义 $g(n)$ 为过程结束前必须加 $1$ 的次数。例如：

$125\xrightarrow{\scriptsize{+1}} 126\xrightarrow{\scriptsize{\div 7}} 18\xrightarrow{\scriptsize{+1}} 19\xrightarrow{\scriptsize{+1}} 20\xrightarrow{\scriptsize{+1}} 21\xrightarrow{\scriptsize{\div 7}} 3\xrightarrow{\scriptsize{+1}} 4\xrightarrow{\scriptsize{+1}} 5\xrightarrow{\scriptsize{+1}} 6\xrightarrow{\scriptsize{+1}} 7\xrightarrow{\scriptsize{\div 7}} 1$。

共加了八个 $1$，所以 $g(125) = 8$。类似地 $g(1000) = 9$，$g(10000) = 21$。

定义 $S(N) = \sum_{n=1}^N g(n)$，$H(K) = S\left(\frac{7^K-1}{11}\right)$。已知 $H(10) = 690409338$。

求 $H(10^9)$ 模 $1\,117\,117\,717$。
