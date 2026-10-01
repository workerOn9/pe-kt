# 978 · 随机游走偏度

> 中文意译。英文原文见 [Project Euler Problem 978](https://projecteuler.net/problem=978)；抓取底稿见 `statement.en.md`。

在本题中，我们考虑整数集 $\mathbb{Z}$ 上的随机游走，其中在时间 $t$ 时的位置记为 $X_t$。

在时刻 $0$，我们从位置 $0$ 出发，即 $X_0=0$。
在时刻 $1$，我们跳到位置 $1$，即 $X_1=1$。
此后在时刻 $t=2,3,\dots$，我们向正方向或负方向跳跃大小为 $|X_{t-2}|$ 的步长，两个方向的概率各为 $1/2$。若 $X_{t-2}=0$，则在时刻 $t$ 保持原地不动。

在 $t=5$ 时，位置 $X_5$ 的分布如下：

$$
X_5=\begin{cases} -1\quad&\text{概率为 }3/8\\ 1\quad&\text{概率为 }3/8\\ 3\quad&\text{概率为 }1/8\\ 5\quad&\text{概率为 }1/8\end{cases}
$$

均值为 $\mu$ 的随机变量 $X$ 的标准差 $\sigma$ 定义为

$$
\sigma=\sqrt{\mathbb{E}[X^2]-\mu^2}
$$

进一步，$X$ 的偏度（skewness）定义为

$$
\text{Skew}(X)=\mathbb{E}\biggl[\Bigl(\frac{X-\mu}{\sigma}\Bigr)^3\biggr]
$$

对于均值为 $1$、标准差为 $2$ 的 $X_5$，其偏度为 $\text{Skew}(X_5)=0.75$。已知 $\text{Skew}(X_{10})\approx 2.50997097$。

求 $\text{Skew}(X_{50})$，四舍五入保留小数点后八位数字。
