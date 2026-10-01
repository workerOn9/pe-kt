# 769 · 二元二次型 II

> 中文意译。英文原文见 [Project Euler Problem 769](https://projecteuler.net/problem=769)；抓取底稿见 `statement.en.md`。

考虑如下二元二次型：

$$
\begin{aligned} f(x,y)=x^2+5xy+3y^2 \end{aligned}
$$

若存在正整数 $x$ 与 $y$ 使得 $q = f(x,y)$ 且 $\gcd(x,y)=1$，则称正整数 $q$ 有一个本原表示。

我们关注完全平方数的本原表示。例如：
$17^2=f(1,9)$
$87^2=f(13,40) = f(46,19)$

定义 $C(N)$ 为 $0 < z \leq N$ 时 $z^2$ 的本原表示总数。
多种表示分别计数，因此例如 $z=87$ 被计两次。

已知 $C(10^3)=142$，$C(10^{6})=142463$。

求 $C(10^{14})$。
