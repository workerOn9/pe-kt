# PE 226 — A Scoop of Blancmange（一勺奶冻）

奶冻曲线（blancmange curve）是满足 $0 \le x \le 1$ 且

$$
y = \sum_{n=0}^{\infty} \frac{s(2^n x)}{2^n}
$$

的点 $(x, y)$ 的集合，其中 $s(x)$ 表示 $x$ 到最近整数的距离。

该曲线下方的总面积恰好等于 $\frac{1}{2}$（下图中粉色区域）。

设 $C$ 为圆心 $\left(\frac{1}{4}, \frac{1}{2}\right)$、半径 $\frac{1}{4}$ 的圆（图中黑色）。

问：曲线下方的面积中，被圆 $C$ 围住的那一部分是多少？请四舍五入到八位小数，写成 `0.abcdefgh` 的形式。
