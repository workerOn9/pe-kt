一个数列 $g$ 定义如下：

$$
g_k =
\begin{cases}
1, & 0 \le k \le 1999, \\
g_{k-2000} + g_{k-1999}, & k \ge 2000.
\end{cases}
$$

求 $g_k \bmod 20092010$，其中 $k = 10^{18}$。
