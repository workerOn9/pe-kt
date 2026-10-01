# 496 · 三角形的内心与外接圆

> 中文意译。英文原文见 [Project Euler Problem 496](https://projecteuler.net/problem=496)；抓取底稿见 `statement.en.md`。

给定一个边长均为整数的三角形 $ABC$：

令 $I$ 为 $ABC$ 的内心。

令 $D$ 为直线 $AI$ 与 $ABC$ 的外接圆的交点（$A \ne D$）。

我们定义 $F(L)$ 为所有满足 $AC = DI$ 且 $BC \le L$ 的三角形 $ABC$ 的 $BC$ 之和。

例如 $F(15) = 45$，因为满足条件的三角形 $ABC$ 的 $(BC,AC,AB)$ 分别为 $(6,4,5)$、$(12,8,10)$、$(12,9,7)$、$(15,9,16)$。

求 $F(10^9)$。
