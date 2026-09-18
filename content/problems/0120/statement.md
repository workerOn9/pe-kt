# 120 · 平方剩余

> 中文意译。英文原文见 [Project Euler Problem 120](https://projecteuler.net/problem=120)；抓取底稿见 `statement.en.md`。

设 $r$ 为 $(a - 1)^n + (a + 1)^n$ 除以 $a^2$ 所得的余数。

例如 $a = 7$、$n = 3$ 时，$6^3 + 8^3 = 728 \equiv 42 \pmod{49}$，即 $r = 42$。固定 $a$ 而让 $n$ 变化时，$r$ 也随之变化；对 $a = 7$ 而言，$r$ 能取到的最大值就是 $r_{\mathrm{max}} = 42$。

求 $3 \le a \le 1000$ 范围内所有 $r_{\mathrm{max}}$ 之和：

$$
\sum_{a=3}^{1000} r_{\mathrm{max}}(a).
$$
