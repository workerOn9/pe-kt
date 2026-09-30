# 316 · 小数展开中的数

> 中文意译。英文原文见 [Project Euler Problem 316](https://projecteuler.net/problem=316)；抓取底稿见 `statement.en.md`。

设 $p = p_1p_2p_3\cdots$ 是独立、等概率地从 $\{0,1,\dots,9\}$ 中抽取的无限数字序列，它对应实数 $0.p_1p_2p_3\cdots$。在 $[0,1)$ 上均匀随机取一个实数，等价于抽这样一串数字。

对任意有 $d$ 位十进制的正整数 $n$，记 $k$ 为最小的下标，使得 $p_k, p_{k+1}, \dots, p_{k+d-1}$ 依次正好是 $n$ 的各位数字。再记 $g(n)$ 为 $k$ 的期望值；可以证明 $g(n)$ 总是有限，而且总是整数。

例如取 $n=535$：

- 对 $p = 31415926\underline{535}897\cdots$，有 $k=9$；
- 对 $p = 35528714365004956000049084876408468\underline{535}4\cdots$，有 $k=36$；

依此类推，可算出 $g(535)=1008$。

已知
$$
\sum_{n=2}^{999} g\left(\left\lfloor \frac{10^6}{n} \right\rfloor\right) = 27280188 ,
$$
求
$$
\sum_{n=2}^{999999} g\left(\left\lfloor \frac{10^{16}}{n} \right\rfloor\right) .
$$

（注：$\lfloor x \rfloor$ 表示向下取整。）
