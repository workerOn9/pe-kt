# 299 — Three Similar Triangles（三个相似三角形）

## 建模：两个固定的 $135^\circ$ 角

记 $u=b-a>0$、$v=d-a>0$、$P=(p,q)$，其中 $p+q=a$（$P$ 是直线 $AC$ 上的整点；$P$ 在线段外的情形见后文）。

$A\to B$ 沿 $+x$ 方向，$A\to P$ 沿 $(-1,1)$ 方向，因此 $\angle BAP=135^\circ$ 恒定；同理 $C\to D$ 沿 $+y$、$C\to P$ 沿 $(1,-1)$，$\angle DCP=135^\circ$ 恒定。相似必须把唯一的钝角对上唯一的钝角，故 $A\leftrightarrow C$。

**$P$ 处的方向序。** 从 $P$ 出发的四条射线按方向角（自 $+x$ 轴逆时针）升序为

$$
PB\in(-45^\circ,0^\circ),\qquad PA=-45^\circ,\qquad PD\in(90^\circ,135^\circ),\qquad PC=135^\circ ,
$$

所以 $\angle BPD\in(90^\circ,180^\circ)$ 是 $BDP$ 的唯一钝角。要与钝角为 $135^\circ$ 的 $ABP$ 相似，必须 $\angle BPD=135^\circ$，取正切即

$$
\tan\angle BPD=\frac{(v+p)(u+q)-pq}{p(u+q)+q(v+p)}=1
\qquad\Longleftrightarrow\qquad
u\,v=2\,p\,q .\tag{$\star$}
$$

等价地：$ABP\to CDP$ 的相似变换（$A\mapsto C$、$B\mapsto P$、$P\mapsto D$）是旋转 $-45^\circ$、缩放 $p\sqrt2/u$ 的（保向）螺旋相似，由 $P$ 的像直接算出 $2pq=uv$，与 $(\star)$ 一致。

## ABP ~ CDP：两种角度匹配，只剩一种

钝角固定 $A\leftrightarrow C$ 后，其余两角有两种配对：

- $B\leftrightarrow D$：$\tan\angle ABP=\dfrac{q}{u+q}$ 与 $\tan\angle CDP=\dfrac{p}{v+p}$ 相等，交叉相乘得 $p\,u=q\,v$；
- $B\leftrightarrow P$、$P\leftrightarrow D$：由于两组角都满足「两角之和 $=45^\circ$」，一对相等自动蕴含另一对，条件恰好就是 $(\star)$。

第一种与 $(\star)$ 联立给出 $u=vq/p$ 且 $u^2=2q^2$，即 $u/q=\sqrt2$，整数无解，舍去。故 $ABP\sim CDP$ 的充要条件就是 $(\star)$。

## BDP ~ ABP：两条岔路

设 $\alpha'=\angle PBD$、$\gamma'=\angle BDP$，由叉积/点积（或坐标正切）可得

$$
\tan\alpha'=\frac{up+uv+qv}{u^2+2uq+up+qv+2pq+2q^2}.
$$

在 $(\star)$ 之下要求 $\{\alpha',\gamma'\}=\{\alpha,\beta\}$，其中

$$
\tan\alpha=\frac{q}{u+q},\qquad \tan\beta=\frac{u}{u+2q}\qquad(\alpha+\beta=45^\circ).
$$

两条岔路分别化简为（代入 $uv=2pq$ 后交叉相乘、再因式分解）：

- $\alpha'=\alpha \iff (p-q)\cdot u\cdot(u^2+2qu+2q^2)=0 \iff p=q$ —— **情形 A**（$P$ 是 $AC$ 中点）；
- $\alpha'=\beta \iff u^4+2qu^3+2q^2u^2-2pqu^2-4pq^2u-4pq^3=0$，令 $x=u/q$、$t=p/q$：

$$
(x^2+2x+2)\,(x^2-2t)=0
\qquad\Longleftrightarrow\qquad
2\,p\,q=u^2,
$$

再与 $(\star)$ 联立立得 $u=v$ —— **情形 B**（$BDP$ 等腰，$B,D$ 的角与 $ABP$ 的 $P,B$ 两角互换对应）。

两种情形互斥：$p=q$ 与 $u=v$ 同时成立会逼出 $u^2=2p^2$，无整数解。

## 参数化：「乘积 $=2\times$ 平方」

**引理** 对正整数 $x,y,z$，

$$
x\,y=2\,z^2
\iff
\{x,y\}=\{C\,r^2,\ 2C\,s^2\},\quad z=C\,r\,s,
$$

其中 $C$ 为奇数且无平方因子，且这组 $(C,r,s)$ 被 $\{x,y\}$ 唯一确定。

理由：$v_2(x)+v_2(y)=1+2v_2(z)$ 为奇数，故两个因子中恰有一个的 2 指数为奇数。取 2 指数为偶的那个因子，其无平方因子部分 $C$ 必为奇数；写成 $C\cdot r^2$ 后由 $xy=2z^2$ 可知另一因子必为 $2C\cdot s^2$、$z=Crs$。

**情形 A**（$p=q$，$a=2p$，$uv=2p^2$）：取 $\{u,v\}=\{Cx^2,2Cy^2\}$，则 $p=Cxy$；$(u,v)$ 的两种次序给出两个不同的三元组

$$
(a,b,d)=\bigl(2Cxy,\ 2Cxy+Cx^2,\ 2Cxy+2Cy^2\bigr)
\quad\text{与}\quad
\bigl(2Cxy,\ 2Cxy+2Cy^2,\ 2Cxy+Cx^2\bigr),
\qquad
b+d=C\,(x^2+4xy+2y^2).
$$

**情形 B**（$u=v=2w$，$pq=2w^2$）：取 $\{p,q\}=\{Cx^2,2Cy^2\}$，则 $w=Cxy$；$(p,q)$ 互换给出同一个三元组，

$$
(a,b,d)=\bigl(C(x^2+2y^2),\ C(x^2+2xy+2y^2),\ C(x^2+2xy+2y^2)\bigr),
\qquad
b+d=2C\,(x^2+2xy+2y^2).
$$

记 $S(L)=\#\{C\le L:\ C\ \text{为奇数且无平方因子}\}$、$f_A=x^2+4xy+2y^2$、$f_B=x^2+2xy+2y^2$，则

$$
\#\{b+d<N\}=2\sum_{\substack{x,y\ge1\\ f_A(x,y)<N}} S\Bigl(\Bigl\lfloor\frac{N-1}{f_A(x,y)}\Bigr\rfloor\Bigr)
\;+\;
\sum_{\substack{x,y\ge1\\ 2f_B(x,y)<N}} S\Bigl(\Bigl\lfloor\frac{N-1}{2f_B(x,y)}\Bigr\rfloor\Bigr).
$$

## 两条独立实现

**路径 1（$(x,y)$ 遍历 + 分箱 + Möbius）**：直接照上式遍历约 $4.5\times10^7$ 个 $(x,y)$；因为商 $\lfloor (N-1)/f\rfloor$ 只有 $O(\sqrt N)$ 种取值，把 $f\le 20000$ 的项按键 $f$ 分箱、其余项按键 $\lfloor(N-1)/f\rfloor<5000$ 分箱，把 $S(L)$ 的求值次数压到 $2\times10^4$ 量级。$S(L)$ 用容斥/Möbius：

$$
S(L)=\sum_{\substack{d\ \text{奇}\\ d^2\le L}}\mu(d)\left\lfloor\frac{\lfloor L/d^2\rfloor+1}{2}\right\rfloor
$$

（$d^2$ 的奇数倍只有 $d^2\times$ 奇数，个数正是 $\lfloor(\lfloor L/d^2\rfloor+1)/2\rfloor$）。

**路径 2（对 $C$ 求和 + 二次型网点计数）**：交换求和次序，

$$
\#\{b+d<N\}=\sum_{C\ \text{奇、无平方}} \left[\,2N_A\Bigl(\Bigl\lfloor\frac{N-1}{C}\Bigr\rfloor\Bigr)+N_B\Bigl(\Bigl\lfloor\frac{N-1}{2C}\Bigr\rfloor\Bigr)\right],
$$

其中 $N_A(M)=\#\{(x,y)\ge1:\ x^2+4xy+2y^2\le M\}$（不定型，正象限内是双曲形带域）、$N_B(M)=\#\{(x,y)\ge1:\ x^2+2xy+2y^2\le M\}$（正定型椭圆域）。每个 $N_A,N_B$ 按 $y$ 逐行用整数开方求出该行的 $x$ 上界，代价 $O(\sqrt M)$；$C\le 20000$ 直接逐项算，$C>20000$ 时 $\lfloor(N-1)/C\rfloor<5000$，同样用直方图归并。两条路径在遍历结构、平方因子计数方式、复杂度来源上完全不同。

## 验证与旁证

1. **题面样例**：$b+d<100\Rightarrow 92$；$b+d<100000\Rightarrow 320471$——两条路径都命中（`solution.kt` 里 `check` 强断言）。
2. **定义级纯暴力**（`brute-force.kt` ①）：四重枚举 $(a,b,d,p)$ 后按三边平方比的交叉相乘直接判定相似，完全不含推导结论。$b+d<100$ 得 $92$，$(2,3,4)$、$(2,4,3)$、$(3,5,5)$ 都在解集里；$b+d<150\Rightarrow157$、$b+d<200\Rightarrow226$。把 $p$ 的枚举范围放宽到 $[-100,200]$（远超内点范围 $[1,a-1]$，覆盖 $P$ 落在线段外的情形）结果仍为 $92$，印证 $P$ 只可能在线段 $AC$ 内部。
3. **剪枝定义级暴力**（②）：只枚举 $(a,p,u)$，$v$ 取「$v=2pq/u$」与「$v=pu/q$」两个角度候选，再逐个用 ① 的直接相似判定复核。$b+d<1000\Rightarrow1677$、$b+d<2000\Rightarrow3804$，与参数化计数逐点一致。
4. **除数枚举**（③）：按「$uv=2p^2$」/「$pq=2w^2$」逐个数分解 $2p^2$、枚举因数对计数——第三套独立实现，$10^5\Rightarrow320471$、$10^6\Rightarrow3969774$、$10^7\Rightarrow47345573$，与 `solution.kt` 两条路径全部一致。
5. **跨语言**：另写的 Python 版本（同样的参数化、但用「$C$ 求和 + 网点计数」路线）在 $10^8$ 上给出同一个数，作为独立实现的旁证。
6. **量级自检**：计数密度（每单位 $N$ 的三元组数）$N=10^5$ 约 $3.2$、$10^7$ 约 $4.7$、$10^8$ 约 $5.5$，缓慢增长，与 $\sum_C (N/C)$ 型的 $\log$ 增长结构相符。

**答案：$b+d<100\,000\,000$ 时共有 $\mathbf{549936643}$ 个三元组 $(a,b,d)$。**

## 复杂度对比

| 方法 | 理论复杂度 | 本机实测（JIT 预热后 3 轮最优；注明者除外） |
|:--|:--|:--|
| 定义级纯四重枚举（`brute-force.kt` ①，$b+d<200$） | $O(N_0^4/96)\approx1.7\times10^7$ 次判定；$10^8$ 需约 $10^{30}$ 次（$\sim4\times10^{14}$ 年），物理不可行 | $233.5$ ms |
| 剪枝定义级暴力（②，角度候选 + 直接相似复核，$b+d<2000$；**meta 的 `bruteForceBaselineMs` 口径**） | $O(N_0^3/24)\approx3.3\times10^8$ 次候选生成 | $1849.6$ ms |
| 除数枚举（③，独立全程，$b+d<10^6$） | $\sum_{p\le N/4} d(2p^2)=O\!\left(\frac{N}{4}\ln^2 N\right)$，$10^6$ 时约 $4.6\times10^7$ 次除法 | $139.4$ ms（$10^7$ 单轮 $2389$ ms） |
| 路径 1（$(x,y)$ 遍历 + 直方图 + Möbius） | $O(N)$ 量级：$\approx4.5\times10^7$ 次分箱 + $2\times10^4$ 次 $O(\sqrt{N/f})$ 的 Möbius 求和 | $116.9$ ms |
| 路径 2（对 $C$ 求和 + 网点计数 + 分商归并；**meta 的 `optimizedBaselineMs` 口径**） | $O(\sqrt N\cdot B + N/B + N/7)$，$B=2\times10^4$：$\approx3\times10^7$ 次运算 | $28.7$ ms |

同一实现在不同时段重跑有 $\pm30\%$ 的抖动（共享机器负载），上表取同一次完整运行里程序内置 best-of-3 的值；$10^7$ 的除数枚举与 $10^8$ 的两个路径都重复确认过数量级稳定。

路径 1 只开两张长度 $\le 2\times10^4$ 的 `LongArray` 直方图；路径 2 需要一张 $1.4\times10^7$ 的布尔筛（$14$ MB）。相较之下定义级暴力连 $b+d<3000$ 都跑不完。

## 关键教训

- **钝角是「锚点」**：$135^\circ$ 角的唯一性把相似对应压缩到「两种角度匹配 $\times$ 两种岔路」，几何条件一次性化成 $uv=2pq$ 这类对称多项式方程——先找不变量（钝角、方向角排序），再写代数，是解这类「图形+整数」题的通用套路。
- **不能漏第二种匹配**：$p u=q v$ 分支必须显式排除（它与钝角条件联立出 $u^2=2q^2$ 的无理矛盾），否则参数化会朝错误方向走；同理 $P$ 在线段外的情形也要用宽范围定义级枚举兜住，别靠直觉。
- **「乘积 $=2\times$ 平方」有标准参数化**：无平方因子部分加上 2 的指数奇偶，把二次条件翻译成 $(C,r,s)$ 三元参数，枚举维度立刻降到 $O(N)$ 量级；$C$ 必须限定为奇数无平方因子，否则会重复计数。
- **数的是三元组，不是 $(a,b,d,P)$ 四元组**：情形 B 下同一个 $(a,b,d)$ 有两个 $P$（$p,q$ 互换），$b+d<100$ 的暴力若不去重会数出 $110$ 而不是 $92$——对拍前先确认「解」的计数口径。
- **商只有 $O(\sqrt N)$ 种取值**：两条路径都把 $\lfloor N/x\rfloor$ 分箱（一次按 $f$、一次按 $C$），这是把 $O(N)$ 级枚举压到 $10^8$ 只需几十毫秒的关键；另外，整数开方的调整循环（`isqrt` 后微调）比浮点直接截断稳妥得多。
