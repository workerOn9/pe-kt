# 324 · 搭一座塔

> 中文意译。英文原文见 [Project Euler Problem 324](https://projecteuler.net/problem=324)；抓取底稿见 `statement.en.md`。

记 $f(n)$ 为用 $2 \times 1 \times 1$ 的方块填满 $3 \times 3 \times n$ 高塔的方法数。方块可以任意旋转；塔自身的旋转、镜像等都视为不同的方案。

例如（取 $q = 100000007$）：
$$
f(2) = 229 , \qquad f(4) = 117805 ,
$$
$$
f(10) \bmod q = 96149360 , \quad f(10^3) \bmod q = 24806056 , \quad f(10^6) \bmod q = 30808124 .
$$

求 $f(10^{10000}) \bmod 100000007$。
