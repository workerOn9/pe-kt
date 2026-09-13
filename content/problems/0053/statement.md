# 组合数选择

> 中文意译。英文原文见 [Project Euler Problem 53](https://projecteuler.net/problem=53)；抓取底稿见 `statement.en.md`。

从 $1,2,3,4,5$ 这五个数中选三个，恰好有十种选法：$123, 124, 125, 134, 135, 145, 234, 235, 245, 345$。

组合数学中记作

$$\binom 5 3 = 10 .$$

一般地，对 $r\le n$ 有

$$\binom n r = \frac{n!}{r!\,(n-r)!},$$

其中 $n! = n\times(n-1)\times\cdots\times3\times2\times1$，且 $0!=1$。

直到 $n=23$ 才出现超过一百万的值：$\displaystyle\binom{23}{10}=1144066$。

对于 $1\le n\le100$，有多少个（不必互不相同的）$\displaystyle\binom n r$ 的值大于一百万？
