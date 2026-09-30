# 296 — Angular Bisector and Tangent（角平分线与切线）

## 第 1 步：切线–弦的位置关系把 $BE$ 拉成一个有理式

取 $C$ 为原点、角平分线 $k$ 为 $x$ 轴，设 $\angle ACB = 2\theta$。记 $a = BC$、$b = CA$、$c = AB$，则

$$
A = (b\cos\theta,\ b\sin\theta), \qquad B = (a\cos\theta,\ -a\sin\theta).
$$

外接圆过 $C$，方程取 $x^2 + y^2 + Dx + Ey = 0$，代入 $A$、$B$ 解出

$$
D = -\frac{a+b}{2\cos\theta}, \qquad E = \frac{a-b}{2\sin\theta}.
$$

圆心在 $(-D/2,\,-E/2)$，故 $C$ 处半径方向是 $(D,E)$，切线 $m$ 就是 $Dx + Ey = 0$；过 $B$ 的平行线

$$
n:\quad Dx + Ey = D\,x_B + E\,y_B = -\frac{a(a+b)}{2} - \frac{a(a-b)}{2} = -a^2 .
$$

与 $k$（即 $y = 0$）联立得 $x_E = -a^2/D$。再由

$$
c^2 = |A-B|^2 = (b-a)^2\cos^2\theta + (a+b)^2\sin^2\theta
$$

算出

$$
|BE|^2 = \left(x_E - a\cos\theta\right)^2 + a^2\sin^2\theta = a^2\cos^2\theta\,\frac{(b-a)^2}{(a+b)^2} + a^2\sin^2\theta = \frac{a^2 c^2}{(a+b)^2},
$$

即

$$
BE = \frac{ac}{a+b}.
$$

$\theta$ 被整个消掉了：无论三角形怎么摆放，$BE$ 都只依赖三边，而且是**有理式**（不是平方根型）。于是

$$
BE \text{ 为整数} \iff (a+b) \mid ac .
$$

程序里用坐标法直接解交点算出 $|BE|$，与 $ac/(a+b)$ 随机对拍 3000 个三角形，最大相对误差 $2.6\times10^{-16}$，确认推导无误。

## 第 2 步：换成周长友好的参数 $(S, a, r)$

记 $S = a+b$、$r = a+b-c$（「周长盈余」）。三条边序条件恰好变成 $r$ 的线性约束：

$$
a \le b \iff a \le \left\lfloor \frac{S}{2}\right\rfloor, \qquad
c \ge b \iff r \le a, \qquad
c < a+b \iff r \ge 1 .
$$

又 $ac = a(S-r) \equiv -ar \pmod S$，所以 $(a+b)\mid ac \iff S \mid ar$；周长则是 $a+b+c = 2S - r$。计数写成

$$
\text{Answer}(L) = \sum_{S \ge 2} \#\Bigl\{\, r_0(S) \le r \le a \le \left\lfloor \frac{S}{2}\right\rfloor :\ S \mid ar \,\Bigr\},
\qquad r_0(S) = \max(1,\ 2S - L).
$$

两个范围事实：$r_0 > \lfloor S/2 \rfloor$ 时该 $S$ 无解，即 $S \le \lfloor 2L/3\rfloor$；而 $S \le \lfloor (L+1)/2\rfloor$ 时 $r_0 = 1$（周长约束自动满足，因为 $2S - r \le 2S - 1 \le L$）。

## 第 3 步：对每个 $S$ 做对称化，把二维计数折成一维

固定 $S$。集合

$$
X_S = \Bigl\{ (a,r) \in [r_0,\ \lfloor S/2\rfloor]^2 :\ S \mid ar \Bigr\}
$$

在交换 $a \leftrightarrow r$ 下不变（$S \mid ar \iff S \mid ra$）。把 $X_S$ 按 $a > r$、$a < r$、$a = r$ 分块，非对角的两块由对称性一样大，于是我们真正要的「上半块」（$r \le a$）是

$$
\text{count}_S = \#\Bigl\{ r_0 \le r \le a \le \left\lfloor \frac{S}{2}\right\rfloor : S \mid ar \Bigr\}
= \frac{|X_S| + \mathrm{diag}_S}{2},
$$

其中对角线项是干净的整数分解：$S \mid a^2$ 等价于 $S$ 的每个素因子幂 $p^e$ 都满足 $p^{\lceil e/2\rceil} \mid a$，即 $\sigma(S) \mid a$，$\sigma(S) = \prod_{p^e \parallel S} p^{\lceil e/2\rceil}$。所以

$$
\mathrm{diag}_S = \left\lfloor \frac{\lfloor S/2\rfloor}{\sigma(S)}\right\rfloor - \left\lfloor \frac{r_0 - 1}{\sigma(S)}\right\rfloor .
$$

$|X_S|$ 再逐行数：固定 $r$ 时，$S \mid ar$ 等价于 $a$ 是 $Q = S/\gcd(r,S)$ 的倍数，于是对 $d = \gcd(r,S)$

$$
\#\Bigl\{ a \in [r_0, \lfloor S/2\rfloor] : S \mid ar \Bigr\}
= \left\lfloor \frac{\lfloor S/2\rfloor \cdot d}{S}\right\rfloor - \left\lfloor \frac{(r_0-1)\,d}{S}\right\rfloor .
$$

把 $r$ 按 $d \mid S$ 分组：$r = d\,x$，其中 $x \in [\lceil r_0/d\rceil,\ \lfloor \lfloor S/2\rfloor/d\rfloor]$ 且 $\gcd(x, S/d) = 1$，组内只剩一个「区间里有多少个与 $S/d$ 互素的整数」——用莫比乌斯反演

$$
\#\{ x \le N : \gcd(x,m) = 1 \} = \sum_{e \mid m} \mu(e)\left\lfloor \frac{N}{e}\right\rfloor
$$

枚举 $m$ 的无平方因子除数即可（一共 $2^{\omega(m)}$ 个）。$r_0 = 1$ 时每行的计数退化成漂亮的 $\lfloor d/2 \rfloor$，于是

$$
|X_S| = \sum_{r \le \lfloor S/2\rfloor} \left\lfloor \frac{\gcd(r,S)}{2}\right\rfloor
\qquad (r_0 = 1).
$$

主路径的代码就按上面的分段统一写：把每行的计数一律取 $\lfloor \lfloor S/2\rfloor d/S\rfloor - \lfloor (r_0-1)d/S\rfloor$，$r_0$ 只在那一个地方出现，一份循环覆盖两种情形。

## 第 4 步（对照路径）：不做对称化，改在 $a$ 一侧分组

对每个 $a$ 数合法的 $r$：$r$ 必须是 $Q = S/\gcd(a,S)$ 的倍数且落在 $[r_0, a]$ 内，个数为

$$
\max\Bigl(0,\ \left\lfloor \frac{a}{Q}\right\rfloor - \left\lfloor \frac{r_0-1}{Q}\right\rfloor\Bigr).
$$

按 $d = \gcd(a,S)$ 分组（$a = d\,x$，$\gcd(x, S/d) = 1$），该最大值可化为对 $x$ 的区间求和，其中形如 $\sum_{x \le N}\lfloor d\,x/(S/d)\rfloor$ 的前缀和用欧几里得式递归的 `floor_sum`（$O(\log)$）求。这条路径与主路径消除二维的方式完全不同（一个靠 $a \leftrightarrow r$ 对称 + 对角线，一个靠对 $a$ 分组 + 区间和），是可以互相兜底的独立实现。

## 验证旁证

1. **几何引理数值验证**：坐标法（切线方程 $\to$ 平行线 $\to$ 与角平分线求交）直接算 $|BE|$，与 $ac/(a+b)$ 对拍 3000 个随机三角形（覆盖 $a=b$、$c=b$、钝角等情形），最大相对误差 $2.6\times10^{-16}$。
2. **小规模定义级对拍**：三重循环枚举 $(a,b,c)$（约束 $a\le b \le c < a+b$、周长 $\le L$、直接判 $(a+b)\mid ac$）与公式法完全一致：

   | $L$ | 100 | 500 | 1000 | 2000 | 5000 | 20000 | 33333 | 50000 |
   |:--|--:|--:|--:|--:|--:|--:|--:|--:|
   | 计数 | 376 | 13445 | 61339 | 276390 | 1988264 | 38124651 | 112382655 | 264469767 |

   其中 $L \le 5000$ 由三重循环给出（`solution.kt` 内对拍到 2000，`brute-force.kt` 扩到 5000，$L=5000$ 约 $2.6\times10^9$ 次判定），$L = 20000 / 33333 / 50000$ 由另一套「$(a,b)$ 对 $+$ gcd 直接数倍数」的 $O(L^2)$ 实现复核。
3. **全尺寸三条路径互证**：主路径（对称化闭式）＝对照路径（floor-sum 分组）＝全量逐三角形枚举（把每个合法 $(g,k)$ 逐个点数，共 $1.137\times10^9$ 次自增），三者都是 $1137208419$。
4. **量级自检**：周长 $\le 10^5$ 的整数三角形总计约 $6.94\times10^{12}$ 个（$\approx L^3/144$），符合条件的占 $1.64\times10^{-4}$（约每 6100 个里有 1 个），与「多了一个整除约束」的直觉相符。分段关键量：$S \le 50000$（$r_0 = 1$）贡献 $909753697$，$S > 50000$（$r_0 = 2S - 100000$ 逼近 $\lfloor S/2\rfloor$）贡献 $227454722$。

## 答案

周长不超过 $100\,000$ 且 $BE$ 为整数的三角形个数 = **1137208419**。

## 复杂度对比

| 方法 | 复杂度 | 本机实测（热身 1 轮后取 3 轮最优；标「1 轮」的是重活） |
|:--|:--|:--|
| 定义级三重循环 $L = 5000$（全量 $L = 10^5$ 需 $\sim L^3/48 \approx 2\times10^{13}$ 次判定，数小时量级，不可行） | $O(L^3)$ | $693.7$ ms（$L=5000$，1 轮） |
| $(a,b)$ 对 $+$ gcd 直接计数 $L = 50000$（全量需 $\sim 5.5\times10^8$ 次带 gcd 迭代，$\approx 20$ s 量级） | $O(L^2\log L)$ | $6886.4$ ms（$L=50000$，1 轮） |
| 全量逐三角形枚举（参数化 $(g,k)$ 逐点，$1.137\times10^9$ 次计数；**`bruteForceBaselineMs` 口径**） | $O(\text{答案})$ | $1928.3$ ms |
| 主路径 A：对称化 $+$ 闭式行计数（**`optimizedBaselineMs` 口径**） | $\sum_{S \le 2L/3}\sum_{d\mid S} 2^{\omega(S/d)} \approx 3.4\times10^6$ 次整数运算（外层 $7.5\times10^5$ 个 $(S,d)$ 组合） | $11.164$ ms |
| 对照路径 B：按 $a$ 分组 $+$ `floor_sum` | 同样的分块，$\sum_{S}\sum_{d\mid S} 2^{\omega(S/d)} \approx 3.4\times10^6$ 个 $(S,d,e)$ 组合各求一次前缀和（多数递归一两轮即退出） | $54.626$ ms |

内存：两条路径都只需 $O(L)$ 的筛表（最小质因子筛、带 $\mu$ 符号的无平方因子除数表，合计约 1 MB），没有随答案规模增长的存储。

（同一台机器上多次运行有约 $\pm 10\%$ 的抖动；表中取各程序「热身 1 轮后 3 轮最优」的最好值，与 `meta.json` 的两个 baseline 一致。）

## 关键教训

- **先把 $BE$ 算成闭式再谈计数**：切线–弦位置关系用坐标一摆，$\theta$ 就被完全消掉，$BE = ac/(a+b)$ 是个有理式。若停在「切线+角平分线交点」的几何层面做搜索，问题会难上几个数量级。
- **「周长盈余」是把三角形不等式线性化的常备手段**：三边序与 $c < a+b$ 全部化成 $1 \le r \le a \le \lfloor S/2\rfloor$，$(a+b)\mid ac$ 也随之变成 $S \mid ar$ —— 这一步同时干掉了「$a \le b \le c$ 的排序」和「三角形不等式」两类约束。
- **对称性是数点问题的免费午餐**：条件 $S \mid ar$ 在 $(a,r)$ 交换下不变，「上半块」计数 $=$（总量 $+$ 对角线）$/2$；对角线 $S \mid a^2$ 又有 $p^{\lceil e/2\rceil}$ 的干净判据。省掉一半结构，也省掉一半边界 bug。
- **分段点 $S = \lfloor (L+1)/2\rfloor$ 容易写错**：它之前 $r_0 = 1$（周长自动满足），之后 $r_0 = 2S - L$ 随 $S$ 变化。把两种情形统一成「行计数 $= \lfloor \lfloor S/2\rfloor d/S\rfloor - \lfloor (r_0-1)d/S\rfloor$」后，$r_0$ 只出现在一处，这类错误就无处藏身。
- **暴力要给一个全量可行口径**：定义级三重循环在 $L = 10^5$ 下要 $2\times10^{13}$ 次判定，只能缩规模做对拍；真正能在全量上跑的「暴力」是参数化逐三角形枚举（$1.14\times10^9$ 次计数，实测约 1.9 s），把它作为基线才是诚实的性能对照。
- **全尺寸三条独立路径互证 > 只在小区间对拍**：小规模全对也可能栽在「边界差一」「分段点取整」这类错误上——本例正是靠全尺寸互证才发现对照路径多除了一次 2。
