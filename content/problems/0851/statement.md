# 851 · 积之和与和之积

> 中文意译。英文原文见 [Project Euler Problem 851](https://projecteuler.net/problem=851)；抓取底稿见 `statement.en.md`。

设 $n$ 为正整数，令 $E_n$ 为所有由严格正整数组成的 $n$ 元组的集合。

对于 $E_n$ 中的两个元素 $u = (u_1, \dots, u_n)$ 和 $v = (v_1, \dots, v_n)$，我们定义：

- $u$ 和 $v$ 的**积之和**（Sum Of Products），记作 $\langle u, v\rangle$，定义为和式 $\displaystyle\sum_{i = 1}^n u_i v_i$；
- $u$ 和 $v$ 的**和之积**（Product Of Sums），记作 $u \star v$，定义为乘积 $\displaystyle\prod_{i = 1}^n (u_i + v_i)$。

设 $R_n(M)$ 为所有满足 $\langle u, v\rangle = M$ 的有序对 $(u, v) \in E_n \times E_n$ 的 $u \star v$ 之和。

例如：$R_1(10) = 36$，$R_2(100) = 1873044$，$R_2(100!) \equiv 446575636 \pmod{10^9 + 7}$。

求 $R_6(10000!)$ 对 $10^9 + 7$ 取模的值。
