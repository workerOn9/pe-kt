对非负整数 $m$、$n$，阿克曼函数 $A(m,n)$ 的定义如下：

$$
A(m,n) = \begin{cases}
n+1 & \text{若 } m = 0, \\
A(m-1,\,1) & \text{若 } m > 0 \text{ 且 } n = 0, \\
A(m-1,\,A(m,n-1)) & \text{若 } m > 0 \text{ 且 } n > 0.
\end{cases}
$$

例如 $A(1,0) = 2$、$A(2,2) = 7$、$A(3,4) = 125$。

求 $\displaystyle\sum_{n=0}^{6} A(n,n)$，并给出它对 $14^8$ 取模后的结果。
