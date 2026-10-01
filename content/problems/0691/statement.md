# 691 · 多次重复的长子串

> 中文意译。英文原文见 [Project Euler Problem 691](https://projecteuler.net/problem=691)；抓取底稿见 `statement.en.md`。

给定字符串 $s$，定义 $L(k,s)$ 为 $s$ 中至少在 $s$ 里出现 $k$ 次的最长子串的长度，若这样的子串不存在则为 $0$。例如，$L(3,\text{“bbabcabcabcacba”})=4$，因为子串 $\text{“abca”}$ 出现了三次；$L(2,\text{“bbabcabcabcacba”})=7$，因为重复子串 $\text{“abcabca”}$。注意这些出现可以重叠。

设 $a_n$、$b_n$ 和 $c_n$ 为由下式定义的 $0/1$ 序列：

$a_0 = 0$
$a_{2n} = a_{n}$
$a_{2n+1} = 1-a_{n}$
$b_n = \lfloor\frac{n+1}{\varphi}\rfloor - \lfloor\frac{n}{\varphi}\rfloor$（其中 $\varphi$ 是黄金比）
$c_n = a_n + b_n - 2a_nb_n$

并设 $S_n$ 为字符串 $c_0\ldots c_{n-1}$。已知 $L(2,S_{10})=5$，$L(3,S_{10})=2$，$L(2,S_{100})=14$，$L(4,S_{100})=6$，$L(2,S_{1000})=86$，$L(3,S_{1000}) = 45$，$L(5,S_{1000}) = 31$，且对所有 $k\ge 1$，非零的 $L(k,S_{1000})$ 之和为 $2460$。

求对所有 $k\ge 1$，非零的 $L(k,S_{5000000})$ 之和。
