# 867 · 密铺十二边形

> 中文意译。英文原文见 [Project Euler Problem 867](https://projecteuler.net/problem=867)；抓取底稿见 `statement.en.md`。

用边长为 $1$ 的正多边形密铺边长为 $1$ 的正十二边形共有 $5$ 种方法：

![边长为 1 的正十二边形的 5 种密铺方案](0867_DodecaDiagram.jpg)

设 $T(n)$ 为用边长为 $1$ 的正多边形密铺边长为 $n$ 的正十二边形的方法数。则 $T(1) = 5$。又已知 $T(2) = 48$。

求 $T(10) \bmod (10^9 + 7)$。
