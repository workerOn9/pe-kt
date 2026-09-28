把不能约分的分数称为**韧性分数**（resilient fraction）。再把分母 $d$ 的**韧性** $R(d)$ 定义为它的真分数中韧性分数所占的比例；例如 $R(12) = \dfrac{4}{11}$。

于是数 $d \gt 1$ 的韧性就是 $\dfrac{\varphi(d)}{d - 1}$，其中 $\varphi$ 是欧拉函数。

类似地，定义 $n \gt 1$ 的**核心韧性**（coresilience）为

$$
C(n) = \frac{n - \varphi(n)}{n - 1}.
$$

素数 $p$ 的核心韧性为 $C(p) = \dfrac{1}{p - 1}$。

求所有满足「$C(n)$ 是单位分数（unit fraction，即分子为 $1$ 的分数）」的合数 $1 \lt n \le 2 \times 10^{11}$ 之和。
