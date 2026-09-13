> 本站的题库用 Kotlin 写参考实现。这份报告想回答一个具体的问题：**同一道题、同一个算法，
> 换一种语言写，到底差多少？** 全部数据是本机实测，不做理论推测；每个基准任务都给出
> 各语言的关键代码，读者可以自己复现。

## 缘起

本站第 10 题的解析里有一张复杂度对比表，表格在该题的详情页可以直接看到。写这份报告的动机来自
一个更朴素的疑问：题解站的读者经常问「哪种语言最快」，而这个问题在「Project Euler 前一百题」
这个语境下其实有三个不同的答案，取决于你把哪一层当作杠杆：

1. **算法与复杂度**——同一门语言里换算法，动辄十倍到数十倍；
2. **库与运行时**——同一条算法交给不同的库，比如自己写的大数乘法对 GMP 的分治乘法，差三个数量级；
3. **语言本身**——在算法和库都一致的前提下，源语言的影响通常在一到两倍之间，个别循环形态可以到五倍。

这份报告分别测量这三层，最后给出排序。**结论在文末，急着看可以直接跳过去。**

## 测量方法与口径

基准任务全部取自本站题库，算法照搬现有题解，不做「为了跑分而优化」的改写：

| 编号 | 任务 | 规模 | 对应题目 |
|------|------|------|----------|
| 基准一 | 埃氏筛求和 | n = 2×10⁶ | [第 10 题](/problem/10) |
| 基准二 | 记忆化 Collatz 最长链（长除法式记忆化） | 1..10⁶ | [第 14 题](/problem/14) |
| 基准三 | 无缓存 Collatz 暴力解 | 1..10⁶ | [第 14 题](/problem/14) |
| 基准四 | 20000! 的数位和（约 7.7 万位） | 逐项相乘 vs 分治 | [第 20 题](/problem/20) |
| 基准五 | 200000! 的数位和（约 97 万位） | 同上 | [第 20 题](/problem/20) |
| 基准六 | 10⁷ 次 64 位模乘 `(a × b) mod p` | p = 10⁹+7 | [第 48 题](/problem/48) 的技巧 |

口径与注意事项：

- 每个实现**预热后重复运行，取最小值**。JVM 与 JS 引擎都有即时编译，冷启动的数字没有意义；
  本站题目 `meta.json` 里的耗时基线同样是「JIT 预热后」的口径。
- 全部**单线程**，并行单独讨论（见「并行的杠杆」一节）。
- JavaScript 一律使用**类型化数组**（`Uint8Array` / `Int32Array` / `Float64Array`），
  用普通数组会再慢一截；这不是苛求，而是这类任务在 JS 里的正常写法。
- 所有实现都做了结果一致性校验：筛法求和均为 142913828922，Collatz 最长链起点均为 837799，
  20000! 数位和均为 325494，200000! 数位和均为 4154076。
- 本报告是**单机快照**，不是权威基准。同一段代码在不同机器、不同编译器版本、不同引擎版本上
  会漂移几个百分点到几十个百分点，请把它当作量级参考，而不是排行榜。

机器与工具链版本：

| 项目 | 版本 |
|------|------|
| 机器 | Apple M 系列 8 核（4 性能核 + 4 能效核）/ 16 GB |
| JDK | OpenJDK 21.0.11（HotSpot C2） |
| Kotlin | 2.1.21 |
| C++ | Apple clang 21，`-O2` |
| Rust | rustc 1.98，`-O` |
| Python | CPython 3.14.7（无 JIT） |
| Node | 22.22.3，V8 12.4 |
| Bun | 1.4.0（JavaScriptCore） |
| GMP | Homebrew `gmp`，`mpz_fac_ui` |

## 第一轮：同算法、不同语言

### 基准一：埃氏筛（第 10 题）

[[chart:sieve]]

| 语言 / 运行时 | 耗时 | 相对最快 |
|---------------|------|----------|
| Kotlin 2.1.21（JVM） | 4.74 ms | 1.0× |
| Java 21（JVM） | 4.91 ms | 1.04× |
| Python 3.14 + numpy（向量化） | 5.22 ms | 1.1× |
| C++（clang -O2） | 5.41 ms | 1.14× |
| Rust 1.98（rustc -O） | 6.49 ms | 1.37× |
| Bun 1.4（JSC） | 7.32 ms | 1.54× |
| Node 22（V8） | 7.34 ms | 1.55× |
| Python 3.14（纯 CPython 循环） | 143.3 ms | 30.2× |

这是整份报告里最「反常识」的一张表：**JVM 赢了 C++ 和 Rust，JavaScript 只慢了 1.5 倍。**
原因在任务特征——筛法每筛一个素数就往一个 2 MB 的字节数组里按步长写 1，几乎没有算术强度，
瓶颈是内存带宽而不是指令数。这种情况下编译器之间的差别被内存墙抹平，而且 JVM 的
`BooleanArray` 与 V8 的 `Uint8Array` 都是连续的原始类型数组，没有装箱。

CPython 是唯一的例外，而且不是「差一点」，是差了整整一个数量级：解释器每轮循环都要重新分派字节码，
这个开销无法被内存带宽掩盖。同一门语言里，把循环交给 numpy 之后（`comp[i*i::i] = True` 一次标记一整段），
Python 从 143.3 ms 变成 5.22 ms——**快了 27 倍，语言一个字没改。**

各语言的关键代码：

```kotlin
// Kotlin：BooleanArray + 内层步长标记
fun sieveSum(limit: Int): Long {
    val isComposite = BooleanArray(limit)
    var sum = 0L
    for (i in 2 until limit) {
        if (!isComposite[i]) {
            sum += i
            var j = i.toLong() * i
            while (j < limit) { isComposite[j.toInt()] = true; j += i }
        }
    }
    return sum
}
```

```cpp
// C++：vector<char>，其余同上
long long sieveSum(int limit) {
    std::vector<char> comp(limit, 0);
    long long sum = 0;
    for (int i = 2; i < limit; ++i) {
        if (!comp[i]) {
            sum += i;
            for (long long j = (long long)i * i; j < limit; j += i) comp[(int)j] = 1;
        }
    }
    return sum;
}
```

```rust
// Rust：Vec<bool>
fn sieve_sum(limit: usize) -> i64 {
    let mut comp = vec![false; limit];
    let mut sum = 0i64;
    for i in 2..limit {
        if !comp[i] {
            sum += i as i64;
            let mut j = i * i;
            while j < limit { comp[j] = true; j += i; }
        }
    }
    sum
}
```

```python
# Python：numpy 向量化版本（5.22 ms 那行）
def sieve_sum(limit=2_000_000):
    comp = np.zeros(limit, dtype=bool)
    for i in range(2, int(limit ** 0.5) + 1):
        if not comp[i]:
            comp[i * i::i] = True          # 一次标记一整段
    return int(np.arange(2, limit)[~comp[2:]].sum())
```

```typescript
// TypeScript / JavaScript：类型化数组是这类任务的前提
function sieveSum(limit: number): number {
  const comp = new Uint8Array(limit)
  let sum = 0
  for (let i = 2; i < limit; i++) {
    if (comp[i] === 0) {
      sum += i
      for (let j = i * i; j < limit; j += i) comp[j] = 1
    }
  }
  return sum
}
```

### 基准二：记忆化 Collatz（第 14 题）

[[chart:collatz-cached]]

| 语言 / 运行时 | 耗时 | 相对最快 |
|---------------|------|----------|
| C++（`vector<int>` + `vector<long long>` 栈） | 8.2 ms | 1.0× |
| Java 21（`int[]` + `long[]`） | 8.26 ms | 1.01× |
| Kotlin 2.1.21（`IntArray` + `LongArray`） | 8.27 ms | 1.01× |
| Bun 1.4（JSC） | 21.0 ms | 2.6× |
| Node 22（V8） | 79.0 ms | 9.6× |
| Python 3.14 | 327.7 ms | 40× |

在**原始类型数组 + 简单整数循环**这条最经典的路径上，JVM 与 native 编译基本没有区别——三个实现
落在 8.20 到 8.27 ms 之间，比测量噪声还小。这条结论对本站的题库是安慰性的：用 Kotlin 写题解
不会因为语言本身损失性能。

但这里有一个必须说的坑：本站在第 14 题的题解里，链栈用的是 `ArrayList<Long>`。
把它换成 `LongArray` 之后，同一个算法的耗时从 **21.9 ms 降到 8.27 ms**——三倍差距，与语言无关，
纯粹是每个元素都被装箱成堆对象的代价。顺带一提，把这段逻辑写成 Java 的 `ArrayList<Long>`
同样慢（约 17 ms），**装箱税是 JVM 的共性问题，不是 Kotlin 特有的。**

```kotlin
// Kotlin：IntArray 缓存 + LongArray 显式栈（8.27 ms 那行）
val cache = IntArray(limit); cache[1] = 1
val path = LongArray(512)                    // 关键：不要用 ArrayList<Long>
for (start in 2 until limit) {
    var top = 0
    var n = start.toLong()
    while (n >= limit || cache[n.toInt()] == 0) {
        path[top++] = n
        n = if (n % 2 == 0L) n / 2 else 3 * n + 1
    }
    var len = cache[n.toInt()]
    while (top > 0) {
        len++
        val v = path[--top]
        if (v < limit) cache[v.toInt()] = len
    }
    if (len > bestLen) { bestLen = len; bestStart = start }
}
```

```java
// Java：同样的形状（8.26 ms）
int[] cache = new int[limit]; cache[1] = 1;
long[] path = new long[512];
for (int start = 2; start < limit; start++) {
    int top = 0;
    long n = start;
    while (n >= limit || cache[(int) n] == 0) {
        path[top++] = n;
        n = (n % 2 == 0) ? n / 2 : 3 * n + 1;
    }
    int len = cache[(int) n];
    while (top > 0) {
        len++;
        long v = path[--top];
        if (v < limit) cache[(int) v] = len;
    }
    if (len > bestLen) { bestLen = len; bestStart = start; }
}
```

```typescript
// TypeScript：缓存用 Int32Array，路径用 Float64Array
// （链上中间值会超过 2^31，double 在 2^53 内可精确表示整数）
const cache = new Int32Array(limit); cache[1] = 1
const path = new Float64Array(512)
for (let start = 2; start < limit; start++) {
  let top = 0
  let n = start
  while (n >= limit || cache[n] === 0) {
    path[top++] = n
    n = n % 2 === 0 ? n / 2 : 3 * n + 1
  }
  let len = cache[n]
  while (top > 0) {
    len++
    const v = path[--top]
    if (v < limit) cache[v] = len
  }
  if (len > bestLen) { bestLen = len; bestStart = start }
}
```

### 基准三：无缓存 Collatz 暴力解（第 14 题）

[[chart:collatz-brute]]

| 语言 / 运行时 | 耗时 | 相对最快 |
|---------------|------|----------|
| C++（clang -O2） | 45 ms | 1.0× |
| Kotlin 2.1.21 | 206 ms | 4.6× |
| Java 21 | 217 ms | 4.8× |
| Bun 1.4（JSC） | 473 ms | 10.5× |
| Node 22（V8） | 1819 ms | 40× |

把缓存拿掉，代码变成纯标量循环，图景立刻翻转：**native 比 JVM 快将近 5 倍。**
这和基准二形成鲜明对照——同样一门语言，同样简单的循环，差距从 1.0 倍变成 4.8 倍，变量是循环的形态。

合理的解释是分支：Collatz 的下一步走向取决于 `n` 的奇偶性，而奇偶性在这条链上近乎随机，
分支预测器基本失效。C++ 编译器可以把「除以 2 或乘 3 加 1」这种二选一编译成条件选择指令而不用跳转，
而 JVM 字节码层面没有条件移动，HotSpot 只能发分支。（这条推测**没有**逐条反汇编验证，
仅作为解释方向给出。）基准确认的是现象本身：**「JIT 追平 native」不是普遍规律，
它高度依赖循环形态**；分支可预测、内存访问规整时 JIT 表现很好，反之 native 的优势会被放大。
另外，这段循环里 1 到 10⁶ 的起始值会在十几步内超过 2³¹，JVM 的 `long` 与 JS 的 double
都要额外处理，这也是 JS 引擎在这个基准确上明显吃力的原因之一。

```cpp
// C++：45 ms 那行
static long chainLen(long n) {
    long c = 1;
    while (n != 1) {
        n = (n % 2 == 0) ? n / 2 : 3 * n + 1;
        ++c;
    }
    return c;
}
```

```typescript
// TypeScript：同一个循环（V8 1819 ms / JSC 473 ms）
function collatzBrute(limit: number): number {
  let bestStart = 1, bestLen = 1
  for (let start = 2; start < limit; start++) {
    let n = start, len = 1
    while (n !== 1) {
      n = n % 2 === 0 ? n / 2 : 3 * n + 1
      len++
    }
    if (len > bestLen) { bestLen = len; bestStart = start }
  }
  return bestStart
}
```

## 第二轮：大整数——库的杠杆

[[chart:factorial-20000]]

[[chart:factorial-200000]]

| 实现 | 20000!（7.7 万位） | 200000!（97 万位） |
|------|--------------------|--------------------|
| C++ + GMP `mpz_fac_ui`（分治 + 快速乘法） | 1.82 ms | **10.86 ms** |
| Python 3.14 `math.factorial`（C 实现的分治） | 10.27 ms | 534.87 ms |
| C++ + GMP，同一库、天真逐项相乘 | 30.69 ms | 1556.3 ms |
| Bun 1.4 `BigInt` 天真 | 33.29 ms | RangeError：超出上限 |
| Node 22 `BigInt` 天真 | 42.68 ms | 8385 ms |
| Java 21 `BigInteger` 天真 | 134.28 ms | 13675 ms |

这张表想说明三件事：

**第一，算法与库的杠杆远大于语言。** 同样是 C++ 加 GMP，`mpz_fac_ui` 用 10.86 ms，
自己写一个逐项相乘的循环要 1556.3 ms——**143 倍**，语言和编译器一个字节都没变。前者用的是
分治乘法（把区间折半递归相乘）配合快速乘法内核，复杂度从平方级降到接近
$O(n \log^2 n)$；后者是 $O(n^2)$ 级的字操作。

**第二，「慢语言」可以反杀「快语言」。** Python 的 `math.factorial` 是 C 实现的分治算法，
20000! 用 10.27 ms，比 JVM 上 `BigInteger` 逐项相乘的 134.28 ms **快 13 倍**。
Python 慢的是它自己的解释器循环，不是它调用的那些 C 函数。

**第三，V8 的 `BigInt` 比 JVM 的 `BigInteger` 快。** 20000! 是 42.68 ms 对 134.28 ms，
200000! 是 8385 ms 对 13675 ms。所谓「JS 的大数一定慢」并不成立，V8 对小整数的 `BigInt`
有一条很快的路径。

```cpp
// C++ + GMP：分治版（10.86 ms）
mpz_fac_ui(a, 200000);          // GMP 自带的阶乘，内部用分治 + 快速乘法

// 同一个库、天真逐项相乘（1556 ms）
mpz_set_ui(b, 1);
for (unsigned long i = 2; i <= 200000; ++i) mpz_mul_ui(b, b, i);
```

```python
# Python：math.factorial 同样是分治（534.87 ms）
import math
f = math.factorial(200000)
digit_sum = sum(map(int, str(f)))
```

```java
// Java：BigInteger 没有分治阶乘，只能逐项相乘（13675 ms）
BigInteger f = BigInteger.ONE;
for (int i = 2; i <= 200000; i++) f = f.multiply(BigInteger.valueOf(i));
```

```typescript
// TypeScript：BigInt 同样只能逐项相乘（V8 8385 ms；JSC 直接抛错）
let f = 1n
for (let i = 2n; i <= 200000n; i++) f *= i
```

**Bun/JSC 在这里有一个硬上限。** 同一个程序，70000!（30.9 万位）能算，
80000!（约 39.6 万位）直接抛出：

```
RangeError: Out of memory: BigInt generated from this operation is too big
```

也就是说，用 JavaScriptCore 写大数题，规模一上去不是变慢，而是当场失败；
Node（V8）上 200000! 虽然要 8.4 秒，但至少算得出来。**选运行时也是选优化对象。**

## 第三轮：64 位模乘——JavaScript 的结构性短板

[[chart:modmul]]

| 实现 | 10⁷ 次 `(a × b) mod p` | 单次 |
|------|------------------------|------|
| C++ `uint64_t` | 25.12 ms | 2.5 ns |
| Java 21 `long` | 34.41 ms | 3.4 ns |
| Node 22 `BigInt` | 77.37 ms | 7.7 ns |
| Bun 1.4 `BigInt` | 293.19 ms | 29 ns |

模乘是数论题的地基——快速幂、Miller–Rabin、生日悖论式的哈希去重全靠它。这里的差距不是
「优化得好不好」，而是**类型系统里有没有 64 位整数**：

- C++ 的 `uint64_t`、Java/Kotlin 的 `long` 是语言原生类型，一条乘法指令加一条取模指令。
- JavaScript 的 `Number` 是双精度浮点，安全整数上限是 2⁵³ ≈ 9.007×10¹⁵。
  `(a × b)` 在 `p = 10⁹+7` 时最大约 10¹⁸，**超出精确范围，会静默算错**。
  要算对只有两条路：用 `BigInt`（上表第三、四行），或者用 `Math.imul` 手写 32 位乘法模拟。
  当 `p < √(2⁵³) ≈ 9.49×10⁷` 时，`a*b % p` 在 double 下才是安全的。
- Python 的 `int` 天然任意精度，没有这个坑，代价是不走原生机器字。

```cpp
// C++：一条乘法 + 一条取模，25.12 ms
uint64_t s = 0;
for (long i = 0; i < iters; i++) { s = (s + a * b) % 1000000007ULL; b ^= 1ULL; }
```

```typescript
// TypeScript：必须请出 BigInt，77.37 ms（V8）/ 293.19 ms（JSC）
let s = 0n
for (let i = 0; i < iters; i++) { s = (s + a * b) % 1000000007n; b ^= 1n }
```

## 引擎也是变量：Node/V8 与 Bun/JSC

同一段 JavaScript，换一个运行时，结果可以差四倍：

| 基准 | Node 22（V8） | Bun 1.4（JSC） | 谁快 |
|------|---------------|----------------|------|
| 埃氏筛 | 7.34 ms | 7.32 ms | 持平 |
| Collatz 记忆化 | 79.0 ms | 21.0 ms | JSC 快 3.8× |
| Collatz 暴力 | 1819 ms | 473 ms | JSC 快 3.8× |
| 20000! BigInt | 42.68 ms | 33.29 ms | JSC 快 1.3× |
| 10⁷ 次 BigInt 模乘 | 77.37 ms | 293.19 ms | **V8 快 3.8×** |
| 200000! BigInt | 8385 ms | RangeError | V8 可用 |

结论很清楚：**两个引擎各有一块软肋。** JSC 的数值循环（尤其是浮点与整型混合的密集分支循环）
明显更强，V8 的 `BigInt` 实现更好、且没有实际上的位数上限。这也是工程上值得记住的一条：
谈「JavaScript 的性能」时，运行时的选择可能比代码风格更重要。

## 并行的杠杆

前面所有数字都是单线程。把八个核心用起来，收益往往比换语言大得多（本机 4 性能核 + 4 能效核）：

| 任务 | 单线程 | 8 线程 | 加速 |
|------|--------|--------|------|
| 均匀负载的浮点循环（对照） | 229.7 ms | 35.3 ms | 6.5× |
| Kotlin 暴力 Collatz | 205.7 ms | 47.7 ms | 4.3× |
| C++ 暴力 Collatz | 45 ms | 27.1 ms | 1.9× |

两点观察：其一，**负载是否均匀决定成败**——Collatz 各起始值的链长差异极大，即使按
`start % 线程数` 交错分配也压不住拖尾；其二，**C++ 的并行收益看起来最小（1.9×），
恰恰因为它单线程本来就快**，剩下的时间被线程调度和能效核拖尾吃掉。在 4 性能核 + 4 能效核的
机器上，把能效核上的工作交还给性能核，比再加两个线程更划算。

## 结论

把三层杠杆一起看，排序是这样的（同一算法、单线程的前提下）：

| 梯队 | 阵营 | 相对最快 | 典型场景 |
|------|------|----------|----------|
| 1 | C / C++ / Rust / Fortran（+ SIMD、汇编内核、GPU） | 1.0× | 分支密集的标量循环、手工内存布局 |
| 2 | JVM（Java / Kotlin / Scala）、C#、Go、Julia | 1.0–2×，个别循环到 5× | 热循环经过 JIT 编译后与 native 相当；受装箱与分支形态影响 |
| 3 | CPython 裸写 | 15–40× | 解释器循环是硬开销 |
| 3′ | CPython + numpy / numba / gmpy2 / PyPy | 1–1.5× | 只要重活交给 C 扩展，差距基本消失 |
| 特例 | 任意语言 + GMP / FLINT 这类专用库 | — | 大整数与数论题，谁调用它们谁赢 |

四条可以直接记住的结论：

1. **算法 > 库 > 并行 > 语言。** 同一门语言里换算法可以差 10–35 倍（本站第 14 题 10 倍、
   第 58 题 35 倍），换库可以差 143 倍（分治阶乘对天真阶乘），换语言通常在 1–2 倍。
2. **没有「JIT 一定追得上 native」这回事。** 内存带宽受限的任务上 JVM 能赢 C++（基准一），
   分支密集的标量循环上 native 快 4.8 倍（基准三）。选语言之前先看循环的形态。
3. **实现细节经常比语言更重要。** `ArrayList<Long>` 换成 `LongArray` 就是 3 倍；
   纯 CPython 循环换成 numpy 就是 27 倍。这些改动比换语言便宜得多。
4. **对本站的题库而言，Kotlin 是合适的选择。** 前一百题里绝大多数是「数组 + 整数算术」，
   JVM 在这类任务上与 native 相当；真正吃亏的场景（超大整数、需要手工布局内存）
   在前一百题里几乎不出现。如果哪天真需要它们，瓶颈会在算法与库，而不是 JVM。

## 复现与局限

- 所有实现的**结果一致性**都已校验（见「测量方法与口径」一节列出的四个期望值），
  不同语言对同一任务给出相同答案，比较才算成立。
- 每项取预热后的最小值（Python 与 C++ 取 3–5 次，JVM 与 JS 取 10 次），
  但**只在一台机器上跑过**：四个性能核 + 四个能效核的异构 CPU 会让并行与调度相关的数字
  在不同机器上明显漂移。
- 代码片段是各基准的**核心循环**，省略了计时脚手架与结果校验；片段本身可以直接放进完整程序运行。
- 数值来自 2026 年 9 月的工具链版本（见上表）。编译器与引擎每年都在动，
  这类基准的绝对数字保质期很短，**相对关系**才是值得记住的部分。
