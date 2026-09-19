# 十进制倒数丢番图

> 中文意译。原文见 [Project Euler Problem 157](https://projecteuler.net/problem=157)，抓取底稿见 `statement.en.md`。

考虑形如 $\frac{1}{a} + \frac{1}{b} = \frac{p}{10^n}$ 的丢番图方程，其中 $a, b, p$ 为正整数，$n$ 为正整数且 $1 \le a \lt b \le 10^n$。当 $n = 1$ 时恰好有 20 个解，列于下表：

| a | b | p |
|---|---|---|
| 1 | 10000000000 | 1000000000... (省略) |
... (完整 20 行见题面) |

求 $1 \le n \le 9$ 时方程的所有解总数。