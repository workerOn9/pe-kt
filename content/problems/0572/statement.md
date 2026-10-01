# 572 · 幂等矩阵

> 中文意译。英文原文见 [Project Euler Problem 572](https://projecteuler.net/problem=572)；抓取底稿见 `statement.en.md`。

若矩阵 $M$ 满足 $M^2 = M$，则称它是幂等的。
设 $M$ 为一个三阶矩阵：$M=\begin{pmatrix} a & b & c\\ d & e & f\\ g &h &i\\ \end{pmatrix}$。
设 $C(n)$ 为满足整数元素条件
$ -n \le a,b,c,d,e,f,g,h,i \le n$
的幂等三阶矩阵 $M$ 的数量。

$C(1)=164$，$C(2)=848$。

求 $C(200)$。
