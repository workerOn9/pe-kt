An axis-aligned cuboid, specified by parameters $\{(x_0, y_0, z_0), (dx, dy, dz)\}$, consists of all points $(X,Y,Z)$ such that $x_0\le X\le x_0+dx$, $y_0\le Y\le y_0+dy$ and $z_0\le Z\le z_0+dz$.

Let $C_1,\dots,C_{50000}$ be a collection of 50000 axis-aligned cuboids with parameters

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

where $S_1,\dots,S_{300000}$ come from the Lagged Fibonacci Generator. For $1\le k\le55$ use $S_k=[100003-200003k+300007k^3]\pmod{1000000}$; for $56\le k$, use $S_k=[S_{k-24}+S_{k-55}]\pmod{1000000}$.

The combined volume of the first 100 cuboids is 723581599. What is the combined volume of all 50000 cuboids?

## Source

https://projecteuler.net/problem=212
