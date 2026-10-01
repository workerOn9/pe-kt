# 455 · 尾数幂

> 中文意译。英文原文见 [Project Euler Problem 455](https://projecteuler.net/problem=455)；抓取底稿见 `statement.en.md`。

设 $f(n)$ 为小于 $10^9$ 且使 $n^x$ 的后 $9$ 位数字恰好构成数 $x$（允许前导零）的最大正整数 $x$；若不存在这样的整数，则 $f(n)$ 取零。

例如：

$f(4) = 411728896$（$4^{411728896} = \cdots 490\underline{411728896}$）
$f(10) = 0$
$f(157) = 743757$（$157^{743757} = \cdots 567\underline{000743757}$）
$\sum_{2 \le n \le 10^3} f(n) = 442530011399$

求 $\sum_{2 \le n \le 10^6}f(n)$。
