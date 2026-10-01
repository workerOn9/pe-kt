# 602 · 正面次数之积

> 中文意译。英文原文见 [Project Euler Problem 602](https://projecteuler.net/problem=602)；抓取底稿见 `statement.en.md`。

Alice 请了一些朋友帮忙，用一枚不均匀的硬币生成一个随机数。她和朋友们围坐在桌边，从 Alice 开始依次轮流抛硬币，每个人各自统计自己抛出的正面次数。一旦 Alice 抛出正面，过程立即结束。此时 Alice 把所有朋友的正面次数相乘，得到她的随机数。

举个例子，假设 Alice 有 Bob、Charlie、Dawn 三个助手，他们按这个顺序围坐，抛出的正反面序列为 THHH—TTTT—THHT—H，首尾都是 Alice。那么 Bob 和 Charlie 各得到 2 个正面，Dawn 得到 1 个正面，Alice 的随机数就是 $2\times 2\times 1 = 4$。

定义 $e(n, p)$ 为 Alice 随机数的期望值，其中 $n$ 是帮忙的朋友数（不含 Alice 本人），$p$ 是硬币抛出反面的概率。

可以证明，对任意固定的 $n$，$e(n, p)$ 总是 $p$ 的多项式。例如 $e(3, p) = p^3 + 4p^2 + p$。

定义 $c(n, k)$ 为多项式 $e(n, p)$ 中 $p^k$ 的系数。于是 $c(3, 1) = 1$，$c(3, 2) = 4$，$c(3, 3) = 1$。

已知 $c(100, 40) \equiv 986699437 \text{ } (\text{mod } 10^9+7)$。

求 $c(10000000, 4000000) \bmod 10^9+7$。
