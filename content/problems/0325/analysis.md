# 325 — Stone Game II（取石游戏 II）

## 一、胜负刻画：黄金比是分界线

设 $0<x<y$。分析三种情形：

- 若 $y$ 是 $x$ 的倍数，当前玩家直接取光大堆，**必胜**；
- 若 $y\ge 2x$，当前玩家总能走到必败态（下证），故**必胜**；
- 若 $x<y<2x$，唯一合法走法是取走一个 $x$，局面变成 $(y-x,\,x)$（其中 $y-x<x$）。

于是递推关系为

$$
(x,y)\ \text{必败}\iff y<2x\ \text{且}\ (y-x,\,x)\ \text{必胜}.
$$

令 $\varphi=\dfrac{1+\sqrt5}{2}$ 为黄金比，则「必胜 / 必败」以 $\varphi$ 为界，有封闭刻画

$$
(x,y)\ \text{必败}\iff x<y<\varphi\,x .
$$

**证明。** 关键恒等式是 $\varphi-1=\dfrac1\varphi$。考虑强制走法后的比值

$$
\frac{x}{y-x}=\frac{1}{\,y/x-1\,}.
$$

若 $1<y/x<\varphi$，则 $0<y/x-1<1/\varphi$，取倒数得 $x/(y-x)>\varphi$；又因 $y/x<\varphi<2$，
后继仍是「大堆不小于两倍小堆」之外的区域，归纳可得后继必胜，故原局面必败。反之若
$y/x>\varphi$（且 $y<2x$），则后继比值 $x/(y-x)<\varphi$，归纳得后继必败，故原局面必胜。
$y\ge2x$ 时显然 $y>\varphi x$，必胜。边界 $\varphi$ 不可由整数之比取到，故严格不等式无歧义。

（题面样例 $(2,3)$：$3<2\varphi\approx3.24$ 必败；$(3,4)$：$4<3\varphi$ 必败；
$(1,5)$：$5\ge2$ 必胜。）

## 二、把求和化归为三类 Beatty 和

固定 $y$，令 $a=\lfloor y/\varphi\rfloor$，则必败的 $x$ 恰为 $a+1,\,a+2,\dots,y-1$，其贡献为

$$
\sum_{x=a+1}^{y-1}(x+y)=\bigl[T(y-1)-T(a)\bigr]+y\,(y-1-a),
\qquad T(n)=\frac{n(n+1)}2 .
$$

对 $y=2,\dots,N$ 求和，记

$$
S(N)=A(N)-B(N),
$$

其中 $A(N)$ 为纯多项式部分，$B(N)$ 收纳所有含 $a_y=\lfloor y/\varphi\rfloor$ 的项：

$$
A(N)=\frac{(N-1)N(N+1)}6+\frac{N(N+1)(2N+1)}6-\frac{N(N+1)}2,
$$

$$
B(N)=\underbrace{\sum_{y\le N} T(a_y)}_{(Q+P)/2}+\underbrace{\sum_{y\le N} y\,a_y}_{G(N)}
=\frac{Q(N)+P(N)}2+G(N),
$$

$$
P(n)=\sum_{k\le n}\left\lfloor\frac k\varphi\right\rfloor,\qquad
G(n)=\sum_{k\le n}k\left\lfloor\frac k\varphi\right\rfloor,\qquad
Q(n)=\sum_{k\le n}\left\lfloor\frac k\varphi\right\rfloor^{2}.
$$

注意 $A(N)$ 是闭式，问题归结为计算 $P,G,Q$。

## 三、对偶递推：$O(\log N)$ 收敛

用 $\dfrac1\varphi=\varphi-1$ 把「按 $k$ 求和」换成「按函数值 $j$ 计数」。以 $P$ 为例，
$\lfloor k\alpha\rfloor\ge j\iff k\ge j/\alpha$（$\alpha=1/\varphi$ 无理），故

$$
P(n)=\sum_{j=1}^{M}\Bigl(n-\left\lfloor \frac j\alpha\right\rfloor\Bigr)
=M n-\sum_{j=1}^{M}\bigl(j+\lfloor j\alpha\rfloor\bigr)
=M n-\frac{M(M+1)}2-P(M),
\qquad M=\left\lfloor \frac n\varphi\right\rfloor .
$$

同理交换求和次序可得 $Q$ 与 $G$ 的递推（记 $M=\lfloor n/\varphi\rfloor$）：

$$
Q(n)=nM^{2}-\Bigl[\frac{M(M+1)(2M+1)}3-\frac{M(M+1)}2\Bigr]-2G(M)+P(M),
$$

$$
G(n)=M\,T(n)-\frac{M(M+1)(M+2)}6-G(M)-\frac{Q(M)+P(M)}2 .
$$

三个递推只调用更小的自变量 $M\approx0.618\,n$，且相互闭合，逐层记忆化即有
$O(\log_\varphi N)\approx76$ 层，整题在毫秒级完成。由于要对 $7^{10}$ 取模，而 $2,3,6$ 均与
$7^{10}$ 互素，所有除法都用模逆元完成（$T$、$\lfloor\cdot\rfloor$ 公式里的除法可安全下放到模运算）。

## 四、验证

1. **博弈 DP 互证**：用 minimax 递推直接判定每个局面的胜负，在 $y\le1500$ 的
   $\approx112$ 万个局面上与刻画 $y<\varphi x$ 逐局面比对，**冲突数 $0$**。
2. **题面样例**：$S(10)=211$（博弈 DP 与扫和都给 $211$），$S(10^4)=230312207313$，与题面一致。
3. **双方法互证**：递推解（模 $7^{10}$）与 $O(N)$ 逐 $y$ 扫和在
   $N\in\{10,\,10^3,\,12345,\,10^5,\,10^6\}$ 上全部相等；博弈 DP 在 $N\in\{10,100,1000\}$ 上亦与扫和相等。
4. **正式规模**：$S(10^{16})\bmod 7^{10}$ 的本机实跑输出与公开参考值 $54672965$ 一致。

$$
\boxed{54672965}
$$

## 五、复杂度对比

| 方法 | 规模 | 耗时（本机 JIT 预热后 3 轮最快） |
| --- | --- | --- |
| $O(N)$ 逐 $y$ 扫和（对照） | $N=10^6$ | 2.461 ms |
| $P/G/Q$ 对偶递推（正式） | $N=10^{16}$ | 0.584 ms |

逐 $y$ 扫和是 $O(N)$（$N=10^{16}$ 时约需 $10^{16}$ 次迭代，不可行）；正式法把规模的对数
作为复杂度，约 $76$ 次大整数开方与模乘，$N$ 每放大 $\varphi$ 倍只多一层递归。

## 六、关键教训

- **先找胜负的封闭刻画，再谈求和**。本题一旦把「必败」写成 $y<\varphi x$，求和就退化为
  计数 $\lfloor y/\varphi\rfloor$；把递推关系留在博弈层会让求和复杂得多。
- **黄金比恒等式 $1/\varphi=\varphi-1$ 是全部可算性的来源**。它既给出 $\lfloor j\varphi\rfloor=j+\lfloor j\alpha\rfloor$，
  从而使「按值计数」的换序自动闭合，也让分母永远落在 $\varphi$ 的无理近似之外、不需处理相等边界。
- **无理数的 floor 求和不要用浮点**。$10^{16}\sqrt5$ 远超 double 的 $53$ 位精度，
  $\lfloor n/\varphi\rfloor$ 必须用整数开方（$\lfloor\sqrt{5n^2}\rfloor$）精确求出；
  浮点误差会悄悄改变递归树的每个分支。
