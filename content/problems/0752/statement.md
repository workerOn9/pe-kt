# 752 · (1+√7) 的幂

> 中文意译。英文原文见 [Project Euler Problem 752](https://projecteuler.net/problem=752)；抓取底稿见 `statement.en.md`。

当 $(1+\sqrt 7)$ 被提升到整数次幂 $n$ 时，总得到一个形如 $(a+b\sqrt 7)$ 的数。记 $(1+\sqrt 7)^n = \alpha(n) + \beta(n)\sqrt 7$。

对给定的数 $x$，定义 $g(x)$ 为使下式成立的最小正整数 $n$：

$$
\alpha(n) \equiv 1 \pmod x\qquad \text{且}\qquad \beta(n) \equiv 0 \pmod x
$$

若不存在这样的 $n$，则 $g(x) = 0$。例如 $g(3) = 0$，$g(5) = 12$。

进一步定义

$$
G(N) = \sum_{x=2}^N g(x)
$$

已知 $G(10^2) = 28891$，$G(10^3) = 13131583$。

求 $G(10^6)$。
