# 555 · 麦卡锡 91 函数

> 中文意译。英文原文见 [Project Euler Problem 555](https://projecteuler.net/problem=555)；抓取底稿见 `statement.en.md`。

麦卡锡 91 函数定义如下：

$$
M_{91}(n) =
\begin{cases}
n - 10 & \text{若 } n > 100 \\
M_{91}(M_{91}(n+11)) & \text{若 } 0 \leq n \leq 100
\end{cases}
$$

我们可以把其中的常数抽象为新的变量来推广这个定义：

$$
M_{m,k,s}(n) =
\begin{cases}
n - s & \text{若 } n > m \\
M_{m,k,s}(M_{m,k,s}(n+k)) & \text{若 } 0 \leq n \leq m
\end{cases}
$$

这样就有 $M_{91} = M_{100,11,10}$。

记 $F_{m,k,s}$ 为 $M_{m,k,s}$ 的不动点集合，即

$$
F_{m,k,s} = \left\{ n \in \mathbb{N} \, \middle| \, M_{m,k,s}(n) = n \right\}
$$

例如，$M_{91}$ 唯一的不动点是 $n = 91$，也就是说 $F_{100,11,10} = \{91\}$。

现在定义 $SF(m,k,s)$ 为 $F_{m,k,s}$ 中所有元素之和，并记 $S(p,m) = \displaystyle \sum_{1 \leq s < k \leq p}{SF(m,k,s)}$。

例如 $S(10, 10) = 225$，$S(1000, 1000) = 208724467$。

求 $S(10^6, 10^6)$。
