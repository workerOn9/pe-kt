# 418 · 因数三元组

> 中文意译。英文原文见 [Project Euler Problem 418](https://projecteuler.net/problem=418)；抓取底稿见 `statement.en.md`。

设 $n$ 为正整数。若整数三元组 $(a, b, c)$ 满足

- $1 \leq a \leq b \leq c$，
- $a \cdot b \cdot c = n$，

则称其为 $n$ 的一个因数三元组。

定义 $f(n)$ 为 $n$ 的所有使 $c / a$ 最小的因数三元组 $(a, b, c)$ 所对应的 $a + b + c$。可以证明这样的三元组是唯一的。

例如 $f(165) = 19$，$f(100100) = 142$，$f(20!) = 4034872$。

求 $f(43!)$。
