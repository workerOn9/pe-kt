设 $S_m = (x_1, x_2, \dots , x_m)$ 为由正实数构成的 $m$ 元组，满足约束条件：
$$x_1 + x_2 + \cdots + x_m = m$$
使得加权乘积：
$$P_m = x_1 \cdot x_2^2 \cdot \cdots \cdot x_m^m = \prod_{i=1}^m x_i^i$$
取得最大值。

例如，可以验证当 $m = 10$ 时，$\lfloor P_{10}\rfloor = 4112$（其中 $\lfloor \, \rfloor$ 表示下取整函数，即不超过该数的最大整数）。

请求出：
$$\sum_{m = 2}^{15} \lfloor P_m \rfloor$$
