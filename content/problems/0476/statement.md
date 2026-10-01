# 476 · 圆填充 II

> 中文意译。英文原文见 [Project Euler Problem 476](https://projecteuler.net/problem=476)；抓取底稿见 `statement.en.md`。

记 $R(a, b, c)$ 为在边长分别为 $a$、$b$、$c$ 的三角形内部放置三个互不重叠的圆所能覆盖的最大面积。

记 $S(n)$ 为在所有满足 $1 \le a \le b \le c \lt a + b \le n$ 的整数三元组 $(a, b, c)$ 上，$R(a, b, c)$ 的平均值。

已知 $S(2) = R(1, 1, 1) \approx 0.31998$，$S(5) \approx 1.25899$。

求 $S(1803)$，四舍五入到小数点后 $5$ 位。
