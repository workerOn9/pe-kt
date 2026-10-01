# 663 · 子数组之和

> 中文意译。英文原文见 [Project Euler Problem 663](https://projecteuler.net/problem=663)；抓取底稿见 `statement.en.md`。

设 $t_k$ 为如下定义的三阶斐波那契数：
$\quad t_0 = t_1 = 0$；
$\quad t_2 = 1$；
$\quad t_k = t_{k-1} + t_{k-2} + t_{k-3} \quad \text{ for } k \ge 3$。

对给定整数 $n$，设 $A_n$ 为长度 $n$ 的数组（下标从 $0$ 到 $n-1$），初始全为零。
数组通过迭代改变：在每一步 $i$ 中，把 $A_n[(t_{2 i-2} \bmod n)]$ 替换为 $A_n[(t_{2 i-2} \bmod n)]+2 (t_{2 i-1} \bmod n)-n+1$。
每一步 $i$ 之后，定义 $M_n(i)$ 为 $\displaystyle \max\{\sum_{j=p}^q A_n[j]: 0\le p\le q \lt n\}$，即 $A_n$ 中任意连续子数组的最大和。

下图为 $n=5$ 的前 6 步：
初始状态： $\, A_5=\{0,0,0,0,0\}$
第 1 步：$\quad \Rightarrow A_5=\{-4,0,0,0,0\}$，$M_5(1)=0$
第 2 步：$\quad \Rightarrow A_5=\{-4, -2, 0, 0, 0\}$，$M_5(2)=0$
第 3 步：$\quad \Rightarrow A_5=\{-4, -2, 4, 0, 0\}$，$M_5(3)=4$
第 4 步：$\quad \Rightarrow A_5=\{-4, -2, 6, 0, 0\}$，$M_5(4)=6$
第 5 步：$\quad \Rightarrow A_5=\{-4, -2, 6, 0, 4\}$，$M_5(5)=10$
第 6 步：$\quad \Rightarrow A_5=\{-4, 2, 6, 0, 4\}$，$M_5(6)=12$

设 $\displaystyle S(n,l)=\sum_{i=1}^l M_n(i)$。于是 $S(5,6)=32$。
已知 $S(5,100)=2416$，$S(14,100)=3881$，$S(107,1000)=1618572$。

求 $S(10\,000\,003,10\,200\,000)-S(10\,000\,003,10\,000\,000)$。
