# 656 · 回文序列

> 中文意译。英文原文见 [Project Euler Problem 656](https://projecteuler.net/problem=656)；抓取底稿见 `statement.en.md`。

给定无理数 $\alpha$，设 $S_\alpha(n)$ 为序列 $S_\alpha(n)=\lfloor {\alpha \cdot n} \rfloor - \lfloor {\alpha \cdot (n-1)} \rfloor$（$n \ge 1$）。
（$\lfloor \cdots \rfloor$ 为下取整函数。）

可以证明，对任意无理数 $\alpha$，存在无穷多个 $n$ 使得子序列 $\{S_\alpha(1),S_\alpha(2)...S_\alpha(n) \}$ 是回文的。

对 $\alpha = \sqrt{31}$，给出回文子序列的前 $20$ 个 $n$ 值为：$1$，$3$，$5$，$7$，$44$，$81$，$118$，$273$，$3158$，$9201$，$15244$，$21287$，$133765$，$246243$，$358721$，$829920$，$9600319$，$27971037$，$46341755$，$64712473$。

设 $H_g(\alpha)$ 为对应子序列是回文的前 $g$ 个 $n$ 值之和。
所以 $H_{20}(\sqrt{31})=150243655$。

设 $T=\{2,3,5,6,7,8,10,\dots,1000\}$ 为不超过 $1000$ 且不含完全平方数的正整数集合。
计算 $\beta \in T$ 时 $H_{100}(\sqrt \beta)$ 之和。给出答案的最后 $15$ 位数字。
