# PE 212 — Combined Volume of Cuboids

## 题面

轴对齐长方体由参数 $\{(x_0,y_0,z_0),(dx,dy,dz)\}$ 指定，体积为 $dx\times dy\times dz$。多个长方体的合计体积是它们并集的体积；发生重叠时，合计体积小于各长方体体积之和。

按题面给出的 Lagged Fibonacci Generator 产生 $S_1,\ldots,S_{300000}$，并据此构造 $C_1,\ldots,C_{50000}$。求这 50000 个长方体的并集体积。

题面规定：

$$
\begin{aligned}
x_0 &= S_{6n-5}\bmod 10000\\
y_0 &= S_{6n-4}\bmod 10000\\
z_0 &= S_{6n-3}\bmod 10000\\
dx &= 1+(S_{6n-2}\bmod 399)\\
dy &= 1+(S_{6n-1}\bmod 399)\\
dz &= 1+(S_{6n}\bmod 399)
\end{aligned}
$$

初始值满足

$$
S_k=(100003-200003k+300007k^3)\bmod 1000000,
$$

而 $k\ge56$ 时满足 $S_k=(S_{k-24}+S_{k-55})\bmod1000000$。

前 100 个长方体的合计体积为 $723581599$。求全部 50000 个长方体的合计体积。

## 原文链接

https://projecteuler.net/problem=212
