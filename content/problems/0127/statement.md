# abc 三元组

> 中文意译。英文原文见 [Project Euler Problem 127](https://projecteuler.net/problem=127)；抓取底稿见 `statement.en.md`。

$n$ 的**基根** $\operatorname{rad}(n)$ 定义为 $n$ 的全体不同素因子之积。例如 $504 = 2^3 \times 3^2 \times 7$，于是 $\operatorname{rad}(504) = 2 \times 3 \times 7 = 42$。

我们称正整数三元组 $(a, b, c)$ 是一个 **abc 命中**（abc-hit），若它满足

$$
\gcd(a,b) = \gcd(a,c) = \gcd(b,c) = 1,\qquad a < b,\qquad a + b = c,\qquad \operatorname{rad}(abc) < c.
$$

例如 $(5, 27, 32)$ 是一个 abc 命中，因为

$$
\gcd(5,27) = \gcd(5,32) = \gcd(27,32) = 1,\qquad 5 < 27,\qquad 5 + 27 = 32,\qquad \operatorname{rad}(4320) = 30 < 32.
$$

事实证明 abc 命中相当稀少：当 $c < 1000$ 时一共只有三十一个，它们的 $c$ 之和为 $12523$。

求 $c < 120000$ 时所有 abc 命中的 $c$ 之和。
