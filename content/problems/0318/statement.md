# 318 · 2011 个 9

> 中文意译。英文原文见 [Project Euler Problem 318](https://projecteuler.net/problem=318)；抓取底稿见 `statement.en.md`。

考虑实数 $\sqrt 2 + \sqrt 3$，它的偶次幂是：

$$
\begin{aligned}
(\sqrt 2 + \sqrt 3)^2 &= 9.898979485566356\cdots \\
(\sqrt 2 + \sqrt 3)^4 &= 97.98979485566356\cdots \\
(\sqrt 2 + \sqrt 3)^6 &= 969.998969071069263\cdots \\
(\sqrt 2 + \sqrt 3)^8 &= 9601.99989585502907\cdots \\
(\sqrt 2 + \sqrt 3)^{10} &= 95049.999989479221\cdots \\
(\sqrt 2 + \sqrt 3)^{12} &= 940897.9999989371855\cdots \\
(\sqrt 2 + \sqrt 3)^{14} &= 9313929.99999989263\cdots \\
(\sqrt 2 + \sqrt 3)^{16} &= 92198401.99999998915\cdots
\end{aligned}
$$

看起来这些幂的小数部分开头连续 9 的个数单调不减。事实上可以证明：$n$ 很大时，$(\sqrt 2 + \sqrt 3)^{2n}$ 的小数部分趋于 $1$。

考虑所有形如 $\sqrt p + \sqrt q$（$p<q$ 为正整数）的实数，其中满足「$(\sqrt p + \sqrt q)^{2n}$ 的小数部分当 $n$ 很大时趋于 $1$」的那些数。

记 $C(p,q,n)$ 为 $(\sqrt p + \sqrt q)^{2n}$ 的小数部分开头连续 9 的个数，$N(p,q)$ 为使 $C(p,q,n) \ge 2011$ 的最小 $n$。

求 $\displaystyle\sum_{p+q \le 2011} N(p,q)$。
