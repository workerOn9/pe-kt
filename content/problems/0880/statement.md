# 880 · 嵌套根式

> 中文意译。英文原文见 [Project Euler Problem 880](https://projecteuler.net/problem=880)；抓取底稿见 `statement.en.md`。

若非零整数 $x$ 和 $y$ 满足 $\dfrac{x}{y}$ 不是有理数的立方，且存在整数 $a, b, c$ 满足：

$$
\sqrt{\sqrt[3]{x} + \sqrt[3]{y}} = \sqrt[3]{a} + \sqrt[3]{b} + \sqrt[3]{c}
$$

则称 $(x, y)$ 为一个**嵌套根式对**（nested radical pair）。

例如，$(-4, 125)$ 和 $(5, 5324)$ 都是嵌套根式对：

$$
\begin{aligned}
\sqrt{\sqrt[3]{-4} + \sqrt[3]{125}} &= \sqrt[3]{-1} + \sqrt[3]{2} + \sqrt[3]{4} \\
\sqrt{\sqrt[3]{5} + \sqrt[3]{5324}} &= \sqrt[3]{-2} + \sqrt[3]{20} + \sqrt[3]{25}
\end{aligned}
$$

设 $H(N)$ 为所有满足 $|x| \le |y| \le N$ 的嵌套根式对 $(x, y)$ 的 $|x| + |y|$ 之和。
已知 $H(10^3) = 2535$。

求 $H(10^{15}) \bmod (1031^3 + 2)$。
