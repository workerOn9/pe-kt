# 681 · 最大面积

> 中文意译。英文原文见 [Project Euler Problem 681](https://projecteuler.net/problem=681)；抓取底稿见 `statement.en.md`。

给定正整数 $a \le b \le c \le d$，有时可以用边长为 $a,b,c,d$（任意顺序）的四条边构成一个四边形。当这种情况发生时，记 $M(a,b,c,d)$ 为这样的四边形的最大面积。
例如，$M(2,2,3,3)=6$，可由一个 $2\times 3$ 的矩形达到。

记 $SP(n)$ 为所有满足 $a \le b \le c \le d$ 且 $M(a,b,c,d)$ 是不超过 $n$ 的正整数的选择中，$a+b+c+d$ 之和。
已知 $SP(10)=186$，$SP(100)=23238$。

求 $SP(1\,000\,000)$。
