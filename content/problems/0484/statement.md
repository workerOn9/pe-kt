# 484 · 算术导数

> 中文意译。英文原文见 [Project Euler Problem 484](https://projecteuler.net/problem=484)；抓取底稿见 `statement.en.md`。

**算术导数**定义如下：对于任意素数 $p$，

$$
p^{\prime} = 1
$$

而对于任意整数 $a, b$（莱布尼茨法则），

$$
(ab)^{\prime} = a^{\prime} b + ab^{\prime}
$$

例如 $20^{\prime} = 24$。

求

$$
\sum \gcd(k, k^{\prime}) \qquad (1 \lt k \le 5 \times 10^{15})
$$

注：$\gcd(x, y)$ 表示 $x$ 与 $y$ 的最大公约数。
