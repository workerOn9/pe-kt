# 153 · 探究高斯整数 — 现实应用

**高斯整数论**是现代代数数论的基础构件，其概念推广到任意二次域后成为**代数数论**的核心。费马大定理的证明、椭圆曲线密码体制（ECC）的安全性质分析，都深度依赖这类环的因子分解理论。

**数论函数的 Dirichlet 卷积** 是本解的核心工具。在算法竞赛中，它常被用来计算形如 $\sum_{n\le X} \tau(n^2)$ 的累加和（$\tau$ 为除数函数），对应 LeetCode 类问题「统计因子个数」的优化版本。

**交换求和顺序** 这一技巧在并行计算中同样适用：将「每个作业枚举工序」改为「每个工序统计能执行的工作」，可转化为更适合 MapReduce 的模型。

参考来源：Gaussian Integer divisors on Wikipedia；Dirichlet convolution in analytic number theory.
