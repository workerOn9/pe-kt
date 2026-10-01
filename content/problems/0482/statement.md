# 482 · 整数边三角形的内心距离

> 中文意译。英文原文见 [Project Euler Problem 482](https://projecteuler.net/problem=482)；抓取底稿见 `statement.en.md`。

$ABC$ 是一个边长均为整数的三角形，其内心为 $I$，周长为 $p$。

线段 $IA$、$IB$、$IC$ 的长度也都是整数。

令

$$
L = p + |IA| + |IB| + |IC| .
$$

记 $S(P) = \sum L$，其中求和遍历所有满足 $p \le P$ 的上述三角形。例如 $S(10^3) = 3619$。

求 $S(10^7)$。
