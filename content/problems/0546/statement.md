# 546 · 向下取整的复仇

> 中文意译。英文原文见 [Project Euler Problem 546](https://projecteuler.net/problem=546)；抓取底稿见 `statement.en.md`。

定义 $f_k(n) = \sum_{i=0}^n f_k(\lfloor\frac i k \rfloor)$，其中 $f_k(0) = 1$，$\lfloor x \rfloor$ 表示向下取整函数。

例如 $f_5(10) = 18$，$f_7(100) = 1003$，$f_2(10^3) = 264830889564$。

求 $(\sum_{k=2}^{10} f_k(10^{14})) \bmod (10^9+7)$。
