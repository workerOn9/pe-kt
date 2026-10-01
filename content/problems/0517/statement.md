# 517 · 实数递推

> 中文意译。英文原文见 [Project Euler Problem 517](https://projecteuler.net/problem=517)；抓取底稿见 `statement.en.md`。

对每个实数 $a \gt 1$，数列 $g_a$ 定义为：

$$
g_{a}(x) = 1 \qquad (x \lt a)
$$

$$
g_{a}(x) = g_{a}(x-1) + g_a(x-a) \qquad (x \ge a)
$$

令 $G(n) = g_{\sqrt{n}}(n)$。

已知 $G(90) = 7564511$。

求所有满足 $10000000 \lt p \lt 10010000$ 的素数 $p$ 对应的 $G(p)$ 之和。
答案对 $1000000007$ 取模。
