# 850 · 幂的分数部分

> 中文意译。英文原文见 [Project Euler Problem 850](https://projecteuler.net/problem=850)；抓取底稿见 `statement.en.md`。

任何正实数 $x$ 都可以唯一分解为整数部分与小数部分 $\lfloor x \rfloor + \{x\}$，其中 $\lfloor x \rfloor$ 为向下取整函数，且 $0 \le \{x\} < 1$。

对于正整数 $k$ 和 $n$，定义函数：

$$
f_k(n) = \sum_{i=1}^{n}\left\{ \frac{i^k}{n} \right\}
$$

例如，$f_5(10) = 4.5$ 且 $f_7(1234) = 616.5$。

定义：

$$
S(N) = \sum_{\substack{k=1 \\ k\text{ 为奇数}}}^{N} \sum_{n=1}^{N} f_k(n)
$$

已知 $S(10) = 100.5$ 且 $S(10^3) = 123687804$。

求 $\lfloor S(33557799775533) \rfloor$ 对 $977676779$ 取模的值。
