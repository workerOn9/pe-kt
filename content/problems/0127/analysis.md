# 127 · abc 三元组 — 解析

## 思路推导

**先把判据化简。** rad 是乘性但不是完全乘性的函数，$rad(xy)=rad(x)rad(y)$ 只在 $\gcd(x,y)=1$ 时成立。
本题的互素条件恰好保证了这一点：若 $a,b,c$ 两两互素，则 $a,b,c$ 之间没有公共素因子，于是

$$
\operatorname{rad}(abc) = \operatorname{rad}(a)\operatorname{rad}(b)\operatorname{rad}(c).
$$

**三个 gcd 条件其实只剩一个。** 因为 $c=a+b$，有

$$
\gcd(a,c)=\gcd(a,a+b)=\gcd(a,b),\qquad \gcd(b,c)=\gcd(b,a+b)=\gcd(b,a).
$$

也就是说 $\gcd(a,b)=1$ 自动蕴含另外两个。判定式因此可以写成完全对称的形式：

$$
\gcd(a,b)=1,\qquad a<b,\qquad a+b=c,\qquad \operatorname{rad}(a)\operatorname{rad}(b)\operatorname{rad}(c) < a+b.
$$

**直接从定义枚举的代价。** 对每个 $c<\text{limit}$ 枚举 $a<c/2$，总对数是

$$
\sum_{c=3}^{L-1}\left\lfloor\frac{c-1}{2}\right\rfloor=\frac{L^2}{4}-O(L),
$$

取 $L=120000$ 约为 $3.6\times10^9$ 对——太慢。需要一条能把候选对数量砍下来的必要条件。

**关键必要条件。** $c\ge3$ 故 $\operatorname{rad}(c)\ge2$；又 $c=a+b\le L-1$。于是 $\operatorname{rad}(abc)<c$ 迫使

$$
2\operatorname{rad}(a)\operatorname{rad}(b)\le \operatorname{rad}(a)\operatorname{rad}(b)\operatorname{rad}(c)\le c-1\le L-2 .
$$

这条条件**关于 $a,b$ 对称**，只涉及两个可以直接预计算的量 $\operatorname{rad}(a),\operatorname{rad}(b)$。
把它改写成「固定 $a$ 时 $b$ 的 rad 上界」：

$$
\operatorname{rad}(b)\le\left\lfloor\frac{L-2}{2\operatorname{rad}(a)}\right\rfloor=T(\operatorname{rad}(a)).
$$

$T$ 只依赖 $\operatorname{rad}(a)$；在按 rad 升序排列的表里位置越靠后 $T$ 就越小——这正是可以批量处理的窗口。

**按 rad 分桶排序，把窗口变成连续前缀。** 把 $1\ldots L-1$ 按 $\operatorname{rad}$ 做**计数排序**：
桶下标就是 rad 值，桶内顺序无关紧要。排序后的两个等长数组 `valueList`（数值）与
`radList`（对应 rad）满足 `radList` 单调不减，于是「$\operatorname{rad}\le T$ 的全体候选」
恰好是下标 $0\ldots e-1$ 这一段前缀。代码里不需要二分，直接在扫描时用 `if (ry > bound) break` 截断。

**每个无序对只数一次。** 扫描规则是：位置 $i$ 只考察 $j>i$。取一个满足必要条件的无序对 $\{x,y\}$，
不妨设 $\operatorname{rad}(x)\le\operatorname{rad}(y)$。计数排序让 `radList` 单调不减，
故 $\operatorname{pos}(x)<\operatorname{pos}(y)$（两者 rad 相等时谁在前就由谁扫到对方，结论不变）。
必要条件对 $x$ 成立直接推出 $\operatorname{rad}(y)\le T(\operatorname{rad}(x))$，
即 $y$ 落在 $x$ 的窗口内，$x$ 那一轮必定扫到它；而 $y$ 那一轮只考察下标更大的元素，
$\operatorname{pos}(x)<\operatorname{pos}(y)$ 决定了不会再重复一次。于是**每个无序对恰好被处理一次**，
计数既不重复也不遗漏。又因判据对 $a,b$ 完全对称（$\gcd(a,b)$、$c=a+b$、rad 乘积都与顺序无关），
代码里不需要比较大小：把两者中较小的那个命名为 $a$ 即可满足 $a<b$，而 $c$ 的取值不受影响。

**循环的终止。** rad 升序 ⇒ $T(\operatorname{rad}(x))$ 递减。当 $T<2$ 时，任何候选都需要
$\operatorname{rad}(b)\le1$，即 $b=1$：而 $b=1$ 位于表首、早被 $i=0$ 那一轮扫过，之后不可能再有新对。
$T<2$ 等价于 $\operatorname{rad}(x)>(L-2)/4=29999.5$，实测在第一个满足 $\operatorname{rad}(x)\ge30000$
的元素处（排序后位置 $57\,697$，$x=30001=19\times1579$）结束外循环，只需遍历这 $57\,697$ 个位置。

**判定顺序也是剪枝。** 乘积比较极便宜，gcd 相对昂贵；只有乘积已经成立的极少数对才做 gcd 检查。
实测 $1.58\times10^7$ 次候选里只有 $23\,665$ 对通过乘积条件（其中 $456$ 对满足 $\gcd(a,b)=1$），
最终命中 $456$ 个。

## 独立验证与答案

第二条路径**刻意不化简判据**，按题面字面执行：

1. **枚举方向相反**——从 $c$ 出发，对每个 $c$ 枚举全部 $a<c/2$，不使用任何必要条件，
   候选对数量就是完整的 $3.6\times10^9$；
2. **三对 gcd 分别检查**（`gcd(a,b)`、`gcd(a,c)`、`gcd(b,c)`），不使用「等价于 $\gcd(a,b)=1$」这条化简；
3. **不做计数排序、不做前缀窗口**，只有筛法生成 rad 这一步与优化解共用。

两条路径都复现了题面样例：$c<1000$ 时恰有 **31** 个 abc 命中、$\Sigma c=12523$；
$c<10$ 时恰有 1 个（即 $(1,8,9)$）；$c<4$ 时为 0（$c=a+b$ 至少是 $1+2=3$）。
另外单独断言了题面给出的两个中间量：$\operatorname{rad}(504)=42$，
以及 $(5,27,32)$ 满足全部条件且 $\operatorname{rad}(5\cdot27\cdot32)=\operatorname{rad}(4320)=30<32$。

完整范围内两条路径给出相同的 **456** 个命中，$c<\text{limit}$ 的全部 abc 命中的 $c$ 之和为

$$
\boxed{18407904}
$$

第三重交叉验证用 Python 独立实现（同样的分桶窗口剪枝但完全另行编码），得到同样的
$456$ 个命中与 $18407904$。

## 复杂度对比

| 方案 | 时间 | 空间 | 实测（JIT 预热后） |
|---|---|---|---|
| 优化：rad 筛 + 计数排序 + 必要条件前缀窗口 | $O(L\log\log L+W)$，$W=1.58\times10^7$ | $O(L)$ | 22.7455 ms |
| 对照：按定义枚举全部 $c$、$a<c/2$ 对 | $O(L^2)$，$3.6\times10^9$ 对 | $O(L)$ | 3315.7432 ms |

$W$ 的精确含义是 $\sum_i\#\{j>i:2\operatorname{rad}_i\operatorname{rad}_j\le L-2\}$，
由实跑计数得到 $15\,847\,083$；同一份计数也确认了外循环只需 $57\,697$ 个位置。
两解在同规模下的实测倍速约 $145.8$ 倍，比「$3.6\times10^9$ 对 vs $1.6\times10^7$ 对」的
候选规模比（约 $228$ 倍）略小：对照解每对的判定更廉价（一次 Int 乘法，
仅乘积成立者才做三次 gcd），而优化解多出一次 $O(L)$ 的计数排序、两份窗口数组，
并在 $1.58\times10^7$ 次候选里做了一次随机访存 `rad[c]`。

两者的渐近阶差异才是主因：优化解的候选检查次数随 $L$ 近线性增长（实跑 $W(30000)=2\,347\,525$、
$W(60000)=6\,123\,542$、$W(120000)=15\,847\,083$、$W(240000)=40\,777\,168$，$L$ 每翻倍约增至
$2.6$ 倍），对照解却是严格的 $O(L^2)$（每翻倍增至四倍）。因此 $145.8$ 倍是「近线性量级 vs 平方量级」
在 $L=120000$ 处的实际表现，而非两解实现细节上的常数级差异——规模再放大，这个差距只会继续拉开。

计时口径：两个解都在程序内 JIT 预热后自报耗时，由主 Agent 在空闲机器上各编译一次、
独立进程重复 5 次取最小值；数字已同步进 `meta.json` 的两个 baseline 字段。
计时覆盖 rad 筛、计数排序/数组分配、完整扫描与求和，不含样例断言、编译与输出。

## 实现要点

- **互素条件是 $rad(abc)$ 可拆的前提。** 若允许 $\gcd>1$，$\operatorname{rad}(a)\operatorname{rad}(b)\operatorname{rad}(c)$
  会把公共素因子重复计入而偏大，判据就不等价了。代码里两者是同时成立的，不能只取其一。
- **必要条件的系数 2 来自 $\operatorname{rad}(c)\ge2$，不是随手写的。** 把它误写成
  $\operatorname{rad}(a)\operatorname{rad}(b)\le L-2$（漏掉 $\operatorname{rad}(c)$ 的下界）得到的条件仍然**成立**，
  只是更弱——窗口宽了一倍，白做几千万次检查。反方向才是危险的：任何比
  $2\operatorname{rad}(a)\operatorname{rad}(b)\le L-2$ 更强的条件都会把真实命中挡在窗口之外。
  剪枝用的必要条件宁可取弱也不可取强。
- **窗口扫描的 $j>i$ 不能改成 $j\ge0$。** 那样每个无序对会被两轮各处理一次（$456\to912$），
  计数翻倍；正确性依赖「$i$ 与 $j$ 在同一份按 rad 升序的表里、rad 小者下标也小」这一事实。
- **乘积必须用 `Long`。** 后缀判定里 `rx*ry ≤ (L−2)/2`，用 `Int` 安全；但再乘上 $rad(c)$
  就可能越界：$\operatorname{rad}(c)$ 最大到 $119999$（$c=119999=11\times10909$ 时为自身），
  三元积最坏约 $6\times10^4\times1.2\times10^5\approx7.2\times10^9>2^{31}-1$。
  代码把二元积留在 `Int`、与 `rad(c)` 相乘时升 `Long`。
  累加和 $18\,407\,904$ 本身仍在 `Int` 范围内，但按项目惯例统一用 `Long` 返回。
- **rad 筛法的写法。** 只对素数 $p$ 遍历其倍数并乘一次 $p$，而不是对每个数做质因数分解；
  用 `composite` 数组而非试除来保证每个素因子恰好贡献一次。
- 桶大小取 `limit` 足够：$\operatorname{rad}(n)\le n<\text{limit}$，`rad[0]` 恒为 1 且从不使用。

参考实现：[solution.kt](solution.kt)；独立对照：[brute-force.kt](brute-force.kt)。
