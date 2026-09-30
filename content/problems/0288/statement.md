对任意素数 $p$，数 $N(p,q)$ 定义为

$$
N(p,q)=\sum_{n=0}^{q}T_n\cdot p^n,
$$

其中 $T_n$ 由如下随机数生成器产生：

$$
S_0=290797,\qquad S_{n+1}=S_n^2\bmod 50515093,\qquad T_n=S_n\bmod p .
$$

记 $\operatorname{Nfac}(p,q)$ 为 $N(p,q)$ 的阶乘，$\operatorname{NF}(p,q)$ 为 $\operatorname{Nfac}(p,q)$ 中因子 $p$ 的个数。

已知

$$
\operatorname{NF}(3,10000)\bmod 3^{20}=624955285 .
$$

求 $\operatorname{NF}(61,10^7)\bmod 61^{10}$。
