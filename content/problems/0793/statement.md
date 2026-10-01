# 793 · 乘积中位数

> 中文意译。英文原文见 [Project Euler Problem 793](https://projecteuler.net/problem=793)；抓取底稿见 `statement.en.md`。

设 $S_i$ 为由如下伪随机数生成器产生的整数数列：

$S_0 = 290797$
$S_{i+1} = S_i ^2 \bmod 50515093$

记 $M(n)$ 为两两乘积 $ S_i S_j $（$0 \le i \lt j \lt n$）的中位数。

已知 $M(3) = 3878983057768$，$M(103) = 492700616748525$。

求 $M(1\,000\,003)$。
