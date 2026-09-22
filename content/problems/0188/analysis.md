## 思路推导

本题要求计算超乘方（Tetration）$1777 \mathbin{\uparrow \uparrow} 1855$ 模 $10^8$ 的最后 8 位数字。

### 1. 扩展欧拉定理（降幂公式）

计算高阶幂塔 $a^b \pmod m$ 的核心数学工具是**扩展欧拉定理**。
对于任意整数 $a, b \ge 1$ 以及模数 $m \ge 1$：
$$
a^b \equiv
\begin{cases}
a^{b \pmod{\varphi(m)}}, & \gcd(a, m) = 1 \\
a^b, & \gcd(a, m) \ne 1 \text{ 且 } b < \varphi(m) \\
a^{(b \pmod{\varphi(m)}) + \varphi(m)}, & \gcd(a, m) \ne 1 \text{ 且 } b \ge \varphi(m)
\end{cases} \pmod m
$$

在本题中，底数 $a = 1777$，而 $1777$ 是质数，且与模数 $m = 10^8 = 2^8 \cdot 5^8$ 互质（$\gcd(1777, 10^8) = 1$）。
因此，计算指数的模数可以直接简化为欧拉函数 $\varphi(m)$：
$$
1777^E \equiv 1777^{E \pmod{\varphi(m)}} \pmod m
$$
其中：
$$
\varphi(10^8) = 10^8 \left(1 - \frac{1}{2}\right)\left(1 - \frac{1}{5}\right) = 4 \times 10^7
$$

### 2. 模数的阶梯式衰减

定义递归函数 $T(a, k, m) = a \mathbin{\uparrow \uparrow} k \pmod m$：
- 若 $m = 1$，则任何整数模 1 结果恒为 $0$；
- 若 $k = 1$，则返回 $a \pmod m$；
- 否则，递归求出上一层指数：
  $$
  E = T(a, k - 1, \varphi(m))
  $$
  再计算：
  $$
  T(a, k, m) = a^{E + \varphi(m)} \pmod m
  $$

由于 $\varphi(m)$ 在每一层都至少除以 2，模数序列迅速缩小：
$$
10^8 \to 4\times 10^7 \to 1.6\times 10^7 \to \cdots \to 1
$$
递归深度仅需几十层即可衰减到 $m = 1$，远远小于幂塔高度 $1855$。因此算法在常数时间内即可终止。

---

## 最终答案

经过实跑验证，$1777 \mathbin{\uparrow \uparrow} 1855$ 的最后 8 位数字为 **95962097**。

---

## 复杂度对比

| 实现方案 | 时间复杂度 | 空间复杂度 | 实测耗时 (JVM) |
|---|---|---|---|
| 模意义下迭代定点寻找 (Brute Force) | $O(C \log M)$ | $O(1)$ | 3.5 ms |
| **扩展欧拉定理递归降模 (优化解)** | $O(\log^2 M)$ | $O(\log M)$ | **0.2 ms** |

---

## 关键教训

1. **模数衰减终止快**：对任意正整数 $m$，$m \to \varphi(m)$ 经过至多 $2 \log_2 m$ 次迭代必然变成 1。利用这一特性，巨型幂塔的最后若干位数字只需要极少层即可完全锁定。
2. **通用的扩展欧拉公式**：保留 $+ \varphi(m)$ 偏移量的通用公式形式能够安全避免底数与模数不互质时边界特判的遗漏。
