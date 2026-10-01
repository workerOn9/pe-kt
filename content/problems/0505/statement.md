# 505 · 双向递推

> 中文意译。英文原文见 [Project Euler Problem 505](https://projecteuler.net/problem=505)；抓取底稿见 `statement.en.md`。

定义如下，其中 $\lfloor \cdot \rfloor$ 是取整函数：

$$
\begin{aligned}
x(0) &= 0 \\
x(1) &= 1 \\
x(2k) &= (3x(k) + 2x(\lfloor k/2 \rfloor)) \bmod 2^{60} \quad \text{对 } k \ge 1 \\
x(2k+1) &= (2x(k) + 3x(\lfloor k/2 \rfloor)) \bmod 2^{60} \quad \text{对 } k \ge 1
\end{aligned}
$$

$$
y_n(k) =
\begin{cases}
x(k) & \text{若 } k \ge n \\
2^{60} - 1 - \max(y_n(2k), y_n(2k+1)) & \text{若 } k \lt n
\end{cases}
$$

$$
A(n) = y_n(1)
$$

已知：

$$
\begin{aligned}
x(2) &= 3 \\
x(3) &= 2 \\
x(4) &= 11 \\
y_4(4) &= 11 \\
y_4(3) &= 2^{60} - 9 \\
y_4(2) &= 2^{60} - 12 \\
y_4(1) &= A(4) = 8 \\
A(10) &= 2^{60} - 34 \\
A(10^3) &= 101881
\end{aligned}
$$

求 $A(10^{12})$。
