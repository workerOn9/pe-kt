# 025 · 1000 位斐波那契数 — 解析

## 思路推导

**直接迭代**：用 `BigInteger` 从 $F_1, F_2$ 逐项递推，与阈值 $10^{999}$（最小的 1000 位数）比较，首个不小于阈值的项即答案。第 4782 项约 1000 位十进制 ≈ 3322 位二进制，每次加法 $O(\text{位数})$，整体毫秒级出结果。

**通项公式可以先估出答案**（数学旁证）：比内公式

$$F_n = \frac{\varphi^n - \psi^n}{\sqrt{5}}, \qquad \varphi = \frac{1+\sqrt5}{2} \approx 1.618,\quad \psi = \frac{1-\sqrt5}{2} \approx -0.618$$

由于 $|\psi| < 1$，$\psi^n$ 项迅速可忽略，$F_n \approx \varphi^n / \sqrt{5}$。位数为

$$\text{digits}(F_n) = \lfloor \log_{10} F_n \rfloor + 1 \approx \left\lfloor n \log_{10} \varphi - \log_{10} \sqrt5 \right\rfloor + 1$$

要求 $\ge 1000$：

$$n \ge \frac{999 + \log_{10}\sqrt5}{\log_{10}\varphi} = \frac{999 + 0.34949}{0.20899} \approx 4781.9$$

即 $n = 4782$——与精确迭代结果一致。双精度浮点在该规模下的误差远小于 1，估算可靠；但 PE 的正确姿势仍是**估算指路、精确验证**。

答案：**4782**。

## 复杂度对比

| 方案 | 时间 | 实测（JIT 预热后） |
|------|------|------|
| BigInteger 迭代 + 阈值比较 | $O(n^2)$ 位运算 | ~0.33 ms |
| BigInteger 迭代 + 转字符串数位数 | 每步多一次 $O(\text{位数})$ 转换 | ~64.9 ms |

约 196 倍差距全部来自 `toString()`：十进制字符串转换是逐位取余的除法链，比一次大数比较贵得多。**数位数永远不要用字符串**，用阈值比较（或 $\log_{10}$ / `bitLength` 估算）。

## 实现要点

- 阈值 `BigInteger.TEN.pow(999)` 一次算好，循环内只做比较
- 滚动变量 `prev`/`curr` 迭代，$O(1)$ 额外空间
- 答案从 `index = 2`（对应 $F_2$）开始计，循环不变量：`curr` 始终是 $F_{index}$
- 题目定义 $F_1 = F_2 = 1$（部分教材从 $F_0 = 0$ 起算，注意偏移）

## 代码

参考实现：[solution.kt](solution.kt)（BigInteger + 阈值比较）｜对比：[brute-force.kt](brute-force.kt)（字符串数位数）
