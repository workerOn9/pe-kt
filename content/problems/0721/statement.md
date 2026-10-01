# 721 · 无理数的高次幂

> 中文意译。英文原文见 [Project Euler Problem 721](https://projecteuler.net/problem=721)；抓取底稿见 `statement.en.md`。

给定函数 $f(a,n)=\lfloor (\lceil \sqrt a \rceil + \sqrt a)^n \rfloor$，其中 $\lfloor \cdot \rfloor$ 表示下取整函数，$\lceil \cdot \rceil$ 表示上取整函数。已知 $f(5,2)=27$，$f(5,5)=3935$。

定义

$$
G(n) = \sum_{a=1}^n f(a, a^2).
$$

已知 $G(1000) \bmod 999\,999\,937=163861845$。

求 $G(5\,000\,000)$，答案对 $999\,999\,937$ 取模。
