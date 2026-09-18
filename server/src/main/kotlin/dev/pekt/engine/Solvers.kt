package dev.pekt.engine

import dev.pekt.content.ContentIndex
import dev.pekt.math.binomial
import dev.pekt.math.digitSum
import dev.pekt.math.factorial
import dev.pekt.math.gcd
import dev.pekt.math.isPrime
import dev.pekt.math.lcm
import dev.pekt.math.modPow
import dev.pekt.math.nthPrime
import dev.pekt.math.primesUpTo
import dev.pekt.math.sieve
import java.io.File
import java.math.BigInteger

/**
 * 解法注册表（D-04 v1：进程内直接调用可信代码）。
 *
 * key 为 PE 题号，value 为无参求解函数；
 * 每个函数的逻辑与 `content/problems/XXXX/solution.kt` 完全一致，
 * 素数/ gcd-lcm / 组合数 / BigInteger 扩展等通用步骤改用 dev.pekt.math 工具库。
 * 需要读数据文件的题目（如 0022 的 names.txt、0107 的 network.txt）内部通过
 * [ContentIndex.resolveContentDir] 定位 content 根，求解器对外仍保持无参。
 * 未注册的题号由执行引擎返回 501 no_solver。
 */
val solvers: Map<Int, () -> Long> = mapOf(
    1 to ::solve001,
    2 to ::solve002,
    3 to ::solve003,
    4 to ::solve004,
    5 to ::solve005,
    6 to ::solve006,
    7 to ::solve007,
    8 to ::solve008,
    9 to ::solve009,
    10 to ::solve010,
    11 to ::solve011,
    12 to ::solve012,
    13 to ::solve013,
    14 to ::solve014,
    15 to ::solve015,
    16 to ::solve016,
    17 to ::solve017,
    18 to ::solve018,
    19 to ::solve019,
    20 to ::solve020,
    21 to ::solve021,
    22 to ::solve022,
    23 to ::solve023,
    24 to ::solve024,
    25 to ::solve025,
    26 to ::solve026,
    27 to ::solve027,
    28 to ::solve028,
    29 to ::solve029,
    30 to ::solve030,
    31 to ::solve031,
    32 to ::solve032,
    33 to ::solve033,
    34 to ::solve034,
    35 to ::solve035,
    36 to ::solve036,
    37 to ::solve037,
    38 to ::solve038,
    39 to ::solve039,
    40 to ::solve040,
    41 to ::solve041,
    42 to ::solve042,
    43 to ::solve043,
    44 to ::solve044,
    45 to ::solve045,
    46 to ::solve046,
    47 to ::solve047,
    48 to ::solve048,
    49 to ::solve049,
    50 to ::solve050,
    51 to ::solve051,
    52 to ::solve052,
    53 to ::solve053,
    54 to ::solve054,
    55 to ::solve055,
    56 to ::solve056,
    57 to ::solve057,
    58 to ::solve058,
    59 to ::solve059,
    60 to ::solve060,
    61 to ::solve061,
    62 to ::solve062,
    63 to ::solve063,
    64 to ::solve064,
    65 to ::solve065,
    66 to ::solve066,
    67 to ::solve067,
    68 to ::solve068,
    69 to ::solve069,
    70 to ::solve070,
    71 to ::solve071,
    72 to ::solve072,
    73 to ::solve073,
    74 to ::solve074,
    75 to ::solve075,
    76 to ::solve076,
    77 to ::solve077,
    78 to ::solve078,
    79 to ::solve079,
    80 to ::solve080,
    81 to ::solve081,
    82 to ::solve082,
    83 to ::solve083,
    84 to ::solve084,
    85 to ::solve085,
    86 to ::solve086,
    87 to ::solve087,
    88 to ::solve088,
    89 to ::solve089,
    90 to ::solve090,
    91 to ::solve091,
    92 to ::solve092,
    93 to ::solve093,
    94 to ::solve094,
    95 to ::solve095,
    96 to ::solve096,
    97 to ::solve097,
    98 to ::solve098,
    99 to ::solve099,
    100 to ::solve100,
    101 to ::solve101,
    102 to ::solve102,
    103 to ::solve103,
    104 to ::solve104,
    105 to ::solve105,
    106 to ::solve106,
    107 to ::solve107,
    108 to ::solve108,
    109 to ::solve109,
    110 to ::solve110,
    111 to ::solve111,
    112 to ::solve112,
    113 to ::solve113,
    114 to ::solve114,
    115 to ::solve115,
    116 to ::solve116,
    117 to ::solve117,
    118 to ::solve118,
    119 to ::solve119,
    120 to ::solve120,
    121 to ::solve121,
    122 to ::solve122,
    123 to ::solve123,
    124 to ::solve124,
    125 to ::solve125,
)

/** PE 001 — 容斥原理 + 等差数列求和，1000 以内 3 或 5 的倍数之和 = 233168。O(1)。 */
private fun solve001(): Long {
    fun sumOfMultiples(k: Long, below: Long): Long {
        val n = (below - 1) / k          // 倍数个数：k, 2k, ..., nk < below
        return k * n * (n + 1) / 2
    }
    return sumOfMultiples(3, 1000) + sumOfMultiples(5, 1000) - sumOfMultiples(15, 1000)
}

/** PE 002 — 只枚举偶数项：E(n) = 4·E(n-1) + E(n-2)，上限 4,000,000，答案 = 4613732。 */
private fun solve002(): Long {
    val limit = 4_000_000L
    var sum = 0L
    var prev = 2L   // E(1) = 2
    var curr = 8L   // E(2) = 8
    while (prev <= limit) {
        sum += prev
        val next = 4 * curr + prev
        prev = curr
        curr = next
    }
    return sum
}

/** PE 003 — 试除法求 600851475143 的最大质因子 = 6857。O(sqrt(n))。 */
private fun solve003(): Long {
    var n = 600_851_475_143L
    var candidate = 2L
    var largest = 1L
    while (candidate * candidate <= n) {
        while (n % candidate == 0L) {
            largest = candidate
            n /= candidate
        }
        candidate += if (candidate == 2L) 1L else 2L   // 2 之后只试奇数
    }
    return if (n > 1) n else largest
}

/** PE 004 — 降序搜索 + 剪枝（乘积上界、因子 11）求最大回文乘积 = 906609。 */
private fun solve004(): Long {
    fun isPalindrome(n: Long): Boolean {
        val s = n.toString()
        return s == s.reversed()
    }
    var best = 0L
    for (a in 999 downTo 100) {
        if (a.toLong() * a <= best) break
        val step: Int
        var b: Int
        if (a % 11 == 0) {
            step = 1
            b = a
        } else {
            step = 11
            b = a - a % 11
        }
        while (b >= 100) {
            val product = a.toLong() * b
            if (product <= best) break
            if (isPalindrome(product)) best = product
            b -= step
        }
    }
    return best
}

/** PE 005 — gcd 累乘求 lcm(1..20) = 232792560（gcd/lcm 用 dev.pekt.math 工具库）。 */
private fun solve005(): Long = (1L..20L).reduce(::lcm)

/** PE 006 — 平方和公式：S1 = n(n+1)/2，S2 = n(n+1)(2n+1)/6，答案 = S1² − S2 = 25164150。O(1)。 */
private fun solve006(): Long {
    val n = 100L
    val s1 = n * (n + 1) / 2
    val s2 = n * (n + 1) * (2 * n + 1) / 6
    return s1 * s1 - s2
}

/** PE 007 — 第 10001 个素数 = 104743（筛法 + Rosser 上界，直接用 nthPrime）。 */
private fun solve007(): Long = nthPrime(10_001)

private const val PE008_DIGITS =
    "73167176531330624919225119674426574742355349194934" +
        "96983520312774506326239578318016984801869478851843" +
        "85861560789112949495459501737958331952853208805511" +
        "12540698747158523863050715693290963295227443043557" +
        "66896648950445244523161731856403098711121722383113" +
        "62229893423380308135336276614282806444486645238749" +
        "30358907296290491560440772390713810515859307960866" +
        "70172427121883998797908792274921901699720888093776" +
        "65727333001053367881220235421809751254540594752243" +
        "52584907711670556013604839586446706324415722155397" +
        "53697817977846174064955149290862569321978468622482" +
        "83972241375657056057490261407972968652414535100474" +
        "82166370484403199890008895243450658541227588666881" +
        "16427171479924442928230863465674813919123162824586" +
        "17866458359124566529476545682848912883142607690042" +
        "24219022671055626321111109370544217506941658960408" +
        "07198403850962455444362981230987879927244284909188" +
        "84580156166097919133875499200524063689912560717606" +
        "05886116467109405077541002256983155200055935729725" +
        "71636269561882670428252483600823257530420752963450"

/** PE 008 — 滑动窗口 + 零跳过求 1000 位数字中 13 连位最大乘积 = 23514624000。O(n)。 */
private fun solve008(): Long {
    val window = 13
    val digits = PE008_DIGITS
    var best = 0L
    var product = 1L   // 当前连续非零段（至多 window 位）的乘积
    var count = 0      // 当前连续非零段长度
    for (i in digits.indices) {
        val d = digits[i] - '0'
        if (d == 0) {                       // 含 0 的窗口乘积为 0，重启
            product = 1L
            count = 0
        } else {
            product *= d
            count++
            if (count > window) {           // 窗口已满：除掉滑出的一位
                product /= digits[i - window] - '0'
                count--
            }
            if (count == window && product > best) best = product
        }
    }
    return best
}

/** PE 009 — 欧几里得公式参数化勾股数，a+b+c=1000 的乘积 = 31875000（gcd 用工具库）。 */
private fun solve009(): Long {
    val sum = 1000
    val half = sum / 2                       // km(m+n) = sum/2
    var m = 2
    while (m * (m + 1) <= half) {            // n ≥ 1 ⇒ m(m+n) ≥ m(m+1)
        for (n in 1 until m) {
            if (gcd(m.toLong(), n.toLong()) != 1L || (m - n) % 2 == 0) continue   // 本原条件
            val mmn = m * (m + n)
            if (half % mmn != 0) continue
            val k = half / mmn
            val a = k * (m * m - n * n)
            val b = k * 2 * m * n
            val c = k * (m * m + n * n)
            return a.toLong() * b * c
        }
        m++
    }
    error("无解")
}

/** PE 010 — 200 万以内素数之和 = 142913828922（埃氏筛，直接用 primesUpTo）。 */
private fun solve010(): Long = primesUpTo(2_000_000 - 1).sum()

private const val PE011_GRID = """
08 02 22 97 38 15 00 40 00 75 04 05 07 78 52 12 50 77 91 08
49 49 99 40 17 81 18 57 60 87 17 40 98 43 69 48 04 56 62 00
81 49 31 73 55 79 14 29 93 71 40 67 53 88 30 03 49 13 36 65
52 70 95 23 04 60 11 42 69 24 68 56 01 32 56 71 37 02 36 91
22 31 16 71 51 67 63 89 41 92 36 54 22 40 40 28 66 33 13 80
24 47 32 60 99 03 45 02 44 75 33 53 78 36 84 20 35 17 12 50
32 98 81 28 64 23 67 10 26 38 40 67 59 54 70 66 18 38 64 70
67 26 20 68 02 62 12 20 95 63 94 39 63 08 40 91 66 49 94 21
24 55 58 05 66 73 99 26 97 17 78 78 96 83 14 88 34 89 63 72
21 36 23 09 75 00 76 44 20 45 35 14 00 61 33 97 34 31 33 95
78 17 53 28 22 75 31 67 15 94 03 80 04 62 16 14 09 53 56 92
16 39 05 42 96 35 31 47 55 58 88 24 00 17 54 24 36 29 85 57
86 56 00 48 35 71 89 07 05 44 44 37 44 60 21 58 51 54 17 58
19 80 81 68 05 94 47 69 28 73 92 13 86 52 17 77 04 89 55 40
04 52 08 83 97 35 99 16 07 97 57 32 16 26 26 79 33 27 98 66
88 36 68 87 57 62 20 72 03 46 33 67 46 55 12 32 63 93 53 69
04 42 16 73 38 25 39 11 24 94 72 18 08 46 29 32 40 62 76 36
20 69 36 41 72 30 23 88 34 62 99 69 82 67 59 85 74 04 36 16
20 73 35 29 78 31 90 01 74 31 49 71 48 86 81 16 23 57 05 54
01 70 54 71 83 51 54 69 16 92 33 48 61 43 52 01 89 19 67 48"""

/** PE 011 — 网格四方向（右/下/右下/左下）四连乘积最大值 = 70600674。O(n²)。 */
private fun solve011(): Long {
    val g = PE011_GRID.trim().lines()
        .map { line -> line.trim().split(Regex("\\s+")).map { it.toInt() }.toIntArray() }
        .toTypedArray()
    val n = g.size
    val dirs = arrayOf(intArrayOf(0, 1), intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(1, -1))
    var best = 0L
    for (r in 0 until n) {
        for (c in 0 until n) {
            for ((dr, dc) in dirs) {
                val endR = r + 3 * dr
                val endC = c + 3 * dc
                if (endR !in 0 until n || endC !in 0 until n) continue
                var product = 1L
                for (k in 0 until 4) product *= g[r + k * dr][c + k * dc]
                if (product > best) best = product
            }
        }
    }
    return best
}

/** 最小质因子表（素数枚举用 dev.pekt.math.sieve）：spf[x] = x 的最小质因子（x >= 2）。 */
private fun buildSpf(limit: Int): IntArray {
    val isPrime = sieve(limit)
    val spf = IntArray(limit + 1)
    for (i in 2..limit) {
        if (isPrime[i]) {
            spf[i] = i
            if (i.toLong() * i <= limit) {
                var j = i * i
                while (j <= limit) {
                    if (spf[j] == 0) spf[j] = i
                    j += i
                }
            }
        }
    }
    return spf
}

/** 用 SPF 表求 [x0] 的因子个数。 */
private fun divisorCount(x0: Int, spf: IntArray): Int {
    var x = x0
    var count = 1
    while (x > 1) {
        val p = spf[x]
        var e = 0
        while (x % p == 0) { x /= p; e++ }
        count *= e + 1
    }
    return count
}

/** PE 012 — 首个因子数超过 500 的三角数 = 76576500（互素分解 d(n/2)·d(n+1) + SPF 加速）。 */
private fun solve012(): Long {
    val target = 500
    val spf = buildSpf(1_000_000)
    var n = 1
    while (true) {
        val divisors = if (n % 2 == 0) {
            divisorCount(n / 2, spf) * divisorCount(n + 1, spf)
        } else {
            divisorCount(n, spf) * divisorCount((n + 1) / 2, spf)
        }
        if (divisors > target) return n.toLong() * (n + 1) / 2
        n++
    }
}

private const val PE013_NUMBERS = """
37107287533902102798797998220837590246510135740250
46376937677490009712648124896970078050417018260538
74324986199524741059474233309513058123726617309629
91942213363574161572522430563301811072406154908250
23067588207539346171171980310421047513778063246676
89261670696623633820136378418383684178734361726757
28112879812849979408065481931592621691275889832738
44274228917432520321923589422876796487670272189318
47451445736001306439091167216856844588711603153276
70386486105843025439939619828917593665686757934951
62176457141856560629502157223196586755079324193331
64906352462741904929101432445813822663347944758178
92575867718337217661963751590579239728245598838407
58203565325359399008402633568948830189458628227828
80181199384826282014278194139940567587151170094390
35398664372827112653829987240784473053190104293586
86515506006295864861532075273371959191420517255829
71693888707715466499115593487603532921714970056938
54370070576826684624621495650076471787294438377604
53282654108756828443191190634694037855217779295145
36123272525000296071075082563815656710885258350721
45876576172410976447339110607218265236877223636045
17423706905851860660448207621209813287860733969412
81142660418086830619328460811191061556940512689692
51934325451728388641918047049293215058642563049483
62467221648435076201727918039944693004732956340691
15732444386908125794514089057706229429197107928209
55037687525678773091862540744969844508330393682126
18336384825330154686196124348767681297534375946515
80386287592878490201521685554828717201219257766954
78182833757993103614740356856449095527097864797581
16726320100436897842553539920931837441497806860984
48403098129077791799088218795327364475675590848030
87086987551392711854517078544161852424320693150332
59959406895756536782107074926966537676326235447210
69793950679652694742597709739166693763042633987085
41052684708299085211399427365734116182760315001271
65378607361501080857009149939512557028198746004375
35829035317434717326932123578154982629742552737307
94953759765105305946966067683156574377167401875275
88902802571733229619176668713819931811048770190271
25267680276078003013678680992525463401061632866526
36270218540497705585629946580636237993140746255962
24074486908231174977792365466257246923322810917141
91430288197103288597806669760892938638285025333403
34413065578016127815921815005561868836468420090470
23053081172816430487623791969842487255036638784583
11487696932154902810424020138335124462181441773470
63783299490636259666498587618221225225512486764533
67720186971698544312419572409913959008952310058822
95548255300263520781532296796249481641953868218774
76085327132285723110424803456124867697064507995236
37774242535411291684276865538926205024910326572967
23701913275725675285653248258265463092207058596522
29798860272258331913126375147341994889534765745501
18495701454879288984856827726077713721403798879715
38298203783031473527721580348144513491373226651381
34829543829199918180278916522431027392251122869539
40957953066405232632538044100059654939159879593635
29746152185502371307642255121183693803580388584903
41698116222072977186158236678424689157993532961922
62467957194401269043877107275048102390895523597457
23189706772547915061505504953922979530901129967519
86188088225875314529584099251203829009407770775672
11306739708304724483816533873502340845647058077308
82959174767140363198008187129011875491310547126581
97623331044818386269515456334926366572897563400500
42846280183517070527831839425882145521227251250327
55121603546981200581762165212827652751691296897789
32238195734329339946437501907836945765883352399886
75506164965184775180738168837861091527357929701337
62177842752192623401942399639168044983993173312731
32924185707147349566916674687634660915035914677504
99518671430235219628894890102423325116913619626622
73267460800591547471830798392868535206946944540724
76841822524674417161514036427982273348055556214818
97142617910342598647204516893989422179826088076852
87783646182799346313767754307809363333018982642090
10848802521674670883215120185883543223812876952786
71329612474782464538636993009049310363619763878039
62184073572399794223406235393808339651327408011116
66627891981488087797941876876144230030984490851411
60661826293682836764744779239180335110989069790714
85786944089552990653640447425576083659976645795096
66024396409905389607120198219976047599490197230297
64913982680032973156037120041377903785566085089252
16730939319872750275468906903707539413042652315011
94809377245048795150954100921645863754710598436791
78639167021187492431995700641917969777599028300699
15368713711936614952811305876380278410754449733078
40789923115535562561142322423255033685442488917353
44889911501440648020369068063960672322193204149535
41503128880339536053299340368006977710650566631954
81234880673210146739058568557934581403627822703280
82616570773948327592232845941706525094512325230608
22918802058777319719839450180888072429661980811197
77158542502016545090413245809786882778948721859617
72107838435069186155435662884062257473692284509516
20849603980134001723930671666823555245252804609722
53503534226472524250874054075591789781264330331690"""

/** PE 013 — 高位截取：每个 50 位数只保留前 15 位求和，前 10 位 = 5537376230。O(n)。 */
private fun solve013(): Long {
    val keep = 15                            // 多留 5 位余量吸收进位误差
    val sum = PE013_NUMBERS.trim().lines()
        .map { it.trim().substring(0, keep).toLong() }
        .sum()
    return sum.toString().substring(0, 10).toLong()
}

/** PE 014 — 记忆化 Collatz 链长，百万以内最长链起点 = 837799。均摊 O(n)。 */
private fun solve014(): Long {
    val limit = 1_000_000
    val cache = IntArray(limit)          // 0 = 未知；cache[n] = 从 n 到 1 的链长
    cache[1] = 1
    val path = ArrayList<Long>()         // 本次链上尚未知道长度的节点
    var bestStart = 1
    var bestLen = 1
    for (start in 2 until limit) {
        path.clear()
        var n = start.toLong()
        while (n >= limit || cache[n.toInt()] == 0) {
            path.add(n)
            n = if (n % 2 == 0L) n / 2 else 3 * n + 1
        }
        var len = cache[n.toInt()]       // 撞上了已知长度的节点
        for (i in path.size - 1 downTo 0) {
            len++
            val v = path[i]
            if (v < limit) cache[v.toInt()] = len
        }
        if (len > bestLen) {
            bestLen = len
            bestStart = start
        }
    }
    return bestStart.toLong()
}

/** PE 015 — 20×20 网格路径数 = C(40, 20) = 137846528820（组合数用 dev.pekt.math.binomial）。 */
private fun solve015(): Long = binomial(40, 20).toLong()

/** PE 016 — 2^1000 的十进制数字和 = 1366（BigInteger.pow + digitSum 扩展）。 */
private fun solve016(): Long = BigInteger.TWO.pow(1000).digitSum().toLong()

/** PE 017 — 1..1000 英文拼写字母总数 = 21124（闭式统计，O(1)）。 */
private fun solve017(): Long {
    val ones = intArrayOf(3, 3, 5, 4, 4, 3, 5, 5, 4)              // one..nine
    val teens = intArrayOf(3, 6, 6, 8, 8, 7, 7, 9, 8, 8)          // ten..nineteen
    val tens = intArrayOf(6, 6, 5, 5, 5, 7, 6, 6)                 // twenty..ninety
    val hundred = 7
    val and = 3
    val oneThousand = 11                                           // "onethousand"

    val onesSum = ones.sum()     // 36
    val teensSum = teens.sum()   // 70
    val tensSum = tens.sum()     // 46

    // 1..99：1..19 各一次；十位词各 10 次；个位词作为后缀各出现 8 次
    val sum1to99 = onesSum + teensSum + 10 * tensSum + 8 * onesSum   // 854

    // 100..999：9 个百段，每段 = 百位词×100 + "hundred"×100 + "and"×99 + 1..99 整段
    val sum100to999 = 9L * sum1to99 + 100L * onesSum + 900L * hundred + 9L * 99 * and

    return sum1to99 + sum100to999 + oneThousand
}

private const val PE018_TRIANGLE = """
75
95 64
17 47 82
18 35 87 10
20 04 82 47 65
19 01 23 75 03 34
88 02 77 73 07 63 67
99 65 04 28 06 16 70 92
41 41 26 56 83 40 80 70 33
41 48 72 33 47 32 37 16 94 29
53 71 44 65 25 43 91 52 97 51 14
70 11 33 28 77 73 17 78 39 68 17 57
91 71 52 38 17 14 91 43 58 50 27 29 48
63 66 04 68 89 53 67 30 73 16 69 87 40 31
04 62 98 27 23 09 70 98 73 93 38 53 60 04 23
"""

/** PE 018 — 自底向上 DP 求三角最大路径和 = 1074。O(n²) 时间、O(n) 空间。 */
private fun solve018(): Long {
    val triangle = PE018_TRIANGLE.trim().lines()
        .map { line -> line.trim().split(" ").map { it.toInt() }.toIntArray() }
    val dp = triangle.last().map { it.toLong() }.toLongArray()      // 从底行出发
    for (i in triangle.size - 2 downTo 0) {
        val row = triangle[i]
        for (j in row.indices) {
            dp[j] = row[j] + maxOf(dp[j], dp[j + 1])
        }
    }
    return dp[0]
}

/** PE 019 — 从 1900-01-01（周一）推进星期，20 世纪每月 1 日为周日的次数 = 171。 */
private fun solve019(): Long {
    fun isLeap(year: Int): Boolean =
        year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    var dow = 1                    // 1900-01-01 是周一
    var count = 0L
    for (year in 1900..2000) {
        val daysInMonth = intArrayOf(31, if (isLeap(year)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        for (days in daysInMonth) {
            if (year >= 1901 && dow == 0) count++                  // 每月 1 日是周日
            dow = (dow + days) % 7                                 // 推进到下月 1 日
        }
    }
    return count
}

/** PE 020 — 100! 的十进制数字和 = 648（阶乘用 factorial，求和用 digitSum 扩展）。 */
private fun solve020(): Long = factorial(100).digitSum().toLong()

/** 筛法求 1..limit 内所有数的真因子和：枚举因子 f，累加到它的所有真倍数上。 */
private fun properDivisorSums(limit: Int): IntArray {
    val d = IntArray(limit + 1)
    for (f in 1..limit / 2) {
        var m = 2 * f                       // 只加给 ≥2f 的倍数，保证 f 是真因子
        while (m <= limit) {
            d[m] += f
            m += f
        }
    }
    return d
}

/** PE 021 — 10000 以内所有亲和数之和 = 31626（因子和筛 + 线性扫描配对）。 */
private fun solve021(): Long {
    val limit = 10_000
    val d = properDivisorSums(limit)
    var sum = 0L
    for (a in 2 until limit) {
        val b = d[a]
        // b > a 去重（每对只计一次），同时天然排除 a == b 的完全数
        if (b > a && b < limit && d[b] == a) sum += a + b
    }
    return sum
}

/** 姓名的字母值：A=1, B=2, ..., Z=26 之和。 */
private fun alphabeticalValue(name: String): Int = name.sumOf { it - 'A' + 1 }

/** PE 022 — 姓名排序后 字母值 × 排位 求和 = 871198282。names.txt 经 ContentIndex 定位。 */
private fun solve022(): Long {
    val text = File(ContentIndex.resolveContentDir(), "problems/0022/names.txt").readText()
    val names = text.split(',').map { it.trim('"') }.sorted()
    var total = 0L
    names.forEachIndexed { i, name ->
        total += (i + 1).toLong() * alphabeticalValue(name)
    }
    return total
}

/** PE 023 — 不能表示为两个盈数之和的正整数之和 = 4179871（上界 28123，三步打表）。 */
private fun solve023(): Long {
    val limit = 28123                     // 数学上界：大于它的整数都能写成两个盈数之和
    // 1) 筛法求真因子和
    val d = properDivisorSums(limit)
    val abundants = (12..limit).filter { d[it] > it }

    // 2) 标记所有两个盈数之和
    val expressible = BooleanArray(limit + 1)
    for (i in abundants.indices) {
        for (j in i until abundants.size) {
            val s = abundants[i] + abundants[j]
            if (s > limit) break
            expressible[s] = true
        }
    }

    // 3) 求不能表示者的和
    var sum = 0L
    for (n in 1..limit) {
        if (!expressible[n]) sum += n
    }
    return sum
}

/** PE 024 — 因子进制（factoradic）逐位确定，第 100 万个字典序排列 = 2783915460。O(n²)。 */
private fun solve024(): Long {
    var index = 1_000_000L
    val remaining = (0..9).toMutableList()
    var k = index - 1                         // 转成 0 起始的序号
    var fact = 1L
    for (i in 2..9) fact *= i                 // 9!，首位的块大小
    val sb = StringBuilder()
    while (remaining.isNotEmpty()) {
        val pick = (k / fact).toInt()         // 越过 pick 个块，落在第 pick 个剩余数字上
        sb.append(remaining.removeAt(pick))
        k %= fact
        if (remaining.isNotEmpty()) fact /= remaining.size
    }
    index = sb.toString().toLong()
    return index
}

/** PE 025 — 首个 1000 位斐波那契数的下标 = 4782（BigInteger 迭代 + 阈值比较）。 */
private fun solve025(): Long {
    val digits = 1000
    val threshold = BigInteger.TEN.pow(digits - 1)  // 1000 位数的下界
    var prev = BigInteger.ONE                       // F_1
    var curr = BigInteger.ONE                       // F_2
    var index = 2
    while (curr < threshold) {
        val next = prev + curr
        prev = curr
        curr = next
        index++
    }
    return index.toLong()
}

/** 把 [n0] 分解为不同质因数的列表（试除法）。 */
private fun distinctPrimeFactors(n0: Long): List<Long> {
    var n = n0; val out = ArrayList<Long>(); var d = 2L
    while (d * d <= n) {
        if (n % d == 0L) { out.add(d); while (n % d == 0L) n /= d }
        d++
    }
    if (n > 1) out.add(n)
    return out
}

/** n! 的 Int 值（n ≤ 12 不溢出）。 */
private fun factorialInt(n: Int): Int { var r = 1; for (i in 2..n) r *= i; return r }

/** 判断字符串是否为 1–9 全数字（恰含 1..9 各一次，长度 9）。 */
private fun isPandigital19(s: String): Boolean {
    if (s.length != 9) return false
    val seen = BooleanArray(10)
    for (ch in s) { val d = ch - '0'; if (d == 0 || seen[d]) return false; seen[d] = true }
    return true
}

/** 把 a 写成 p^k（p 不是完全幂）：返回 (p, k)。 */
private fun minBaseExp(a: Int): Pair<Int, Int> {
    var n = a; val exps = ArrayList<Int>()
    var d = 2
    while (d * d <= n) { if (n % d == 0) { var e = 0; while (n % d == 0) { n /= d; e++ }; exps.add(e) } ; d++ }
    if (n > 1) exps.add(1)
    var g = exps[0]; for (e in exps) g = gcd(g.toLong(), e.toLong()).toInt()
    var p = Math.round(Math.pow(a.toDouble(), 1.0 / g)).toInt()
    while (Math.pow(p.toDouble(), g.toDouble()) < a - 1e-6) p++
    while (Math.pow(p.toDouble(), g.toDouble()) > a + 1e-6) p--
    return p to g
}

/** n 的每种数字旋转都是素数时返回 true。 */
private fun isCircularPrime(n: Int, isP: BooleanArray): Boolean {
    if (!isP[n]) return false
    val s = n.toString()
    for (i in 1 until s.length) {
        val r = s.substring(i) + s.substring(0, i)
        if (!isP[r.toInt()]) return false
    }
    return true
}

/** 字符串是否为回文。 */
private fun isPalStr(s: String): Boolean { return s == s.reversed() }

/** n 是否为「1..位数」全数字（0 与重复数字均不允许）。 */
private fun isPandigitalN(n: Int): Boolean {
    val s = n.toString()
    val k = s.length
    val seen = BooleanArray(10)
    for (ch in s) { val d = ch - '0'; if (d == 0 || d > k || seen[d]) return false; seen[d] = true }
    return true
}

/** 按逗号切分 PE 单词表文本并去掉引号。 */
private fun readWords(text: String): List<String> {
    return text.split(',').map { it.trim().trim('"') }.filter { it.isNotEmpty() }
}

/** n 是否为三角数：8n+1 为奇完全平方。 */
private fun isTriangleNumber(n: Int): Boolean {
    val d = 1L + 8L * n
    val s = Math.sqrt(d.toDouble()).toLong()
    for (k in (s - 1)..(s + 1)) if (k * k == d && k % 2L == 1L) return true
    return false
}

/** n 是否为五边形数：24n+1 为完全平方且开方 ≡ 5 (mod 6)。 */
private fun isPentagonal(n: Long): Boolean {
    val d = 1 + 24L * n
    val s = Math.sqrt(d.toDouble()).toLong()
    for (k in (s - 2)..(s + 2)) if (k * k == d && (k + 1) % 6 == 0L) return true
    return false
}

/** 第 n 个五边形数 P_n = n(3n−1)/2。 */
private fun pentagonal(n: Int): Long { return n.toLong() * (3L * n - 1) / 2 }

/** 用 SPF 表数出 x0 的不同质因数个数。 */
private fun distinctFactorCount(n0: Int, spf: IntArray): Int {
    var n = n0; var c = 0
    while (n > 1) { val p = spf[n]; c++; while (n % p == 0) n /= p }
    return c
}

/** 把 n 的十进制数字升序排序后的字符串（排列的规范键）。 */
private fun sortedDigits(n: Int): String { return n.toString().toCharArray().sorted().joinToString("") }



/** PE 026 — 26 题优化解（与 content/problems/0026/solution.kt 一致）。 */
private fun solve026(): Long {
    val isP = sieve(1000)
    var bestD = 0; var bestLen = 0
    for (d in 999 downTo 2) {
        if (!isP[d]) continue
        if (d - 1 <= bestLen) break
        var order = d - 1L
        for (q in distinctPrimeFactors(order)) {
            while (order % q == 0L && modPow(10L, order / q, d.toLong()) == 1L) order /= q
        }
        if (order > bestLen) { bestLen = order.toInt(); bestD = d }
    }
    return bestD.toLong()
}

/** PE 027 — 27 题优化解（与 content/problems/0027/solution.kt 一致）。 */
private fun solve027(): Long {
    val N = 2_000_000
    val isP = sieve(N)
    var bestA = 0; var bestB = 0; var bestN = 0
    for (a in -999..999) {
        for (b in 2..1000) {
            if (!isP[b]) continue
            var n = 0
            while (n * n + a * n + b > 1 && n * n + a * n + b < N && isP[n * n + a * n + b]) n++
            if (n > bestN) { bestN = n; bestA = a; bestB = b }
        }
    }
    return (bestA * bestB).toLong()
}

/** PE 028 — 28 题优化解（与 content/problems/0028/solution.kt 一致）。 */
private fun solve028(): Long {
    var sum = 1L
    for (k in 1..500) sum += 16L * k * k + 4L * k + 4L
    return sum
}

/** PE 029 — 29 题优化解（与 content/problems/0029/solution.kt 一致）。 */
private fun solve029(): Long {
    val seen = HashSet<Long>()
    for (a in 2..100) {
        val (p, g) = minBaseExp(a)
        for (b in 2..100) seen.add(p * 1000L + g.toLong() * b)
    }
    return seen.size.toLong()
}

/** PE 030 — 30 题优化解（与 content/problems/0030/solution.kt 一致）。 */
private fun solve030(): Long {
    val p5 = IntArray(10) { i -> var v = 1; repeat(5) { v *= i }; v }
    var sum = 0L
    for (n in 2..354294) {
        var x = n; var s = 0
        while (x > 0) { s += p5[x % 10]; x /= 10 }
        if (s == n) sum += n
    }
    return sum
}

/** PE 031 — 31 题优化解（与 content/problems/0031/solution.kt 一致）。 */
private fun solve031(): Long {
    val coins = intArrayOf(1, 2, 5, 10, 20, 50, 100, 200)
    val dp = LongArray(201); dp[0] = 1L
    for (c in coins) for (i in c..200) dp[i] += dp[i - c]
    return dp[200]
}

/** PE 032 — 32 题优化解（与 content/problems/0032/solution.kt 一致）。 */
private fun solve032(): Long {
    val products = HashSet<Int>()
    for (a in 1..9) for (b in 1000..9999) {
        val p = a * b
        if (p in 1000..9999 && isPandigital19("$a$b$p")) products.add(p)
    }
    for (a in 10..99) for (b in 100..999) {
        val p = a * b
        if (p in 1000..9999 && isPandigital19("$a$b$p")) products.add(p)
    }
    return products.sumOf { it.toLong() }
}

/** PE 033 — 33 题优化解（与 content/problems/0033/solution.kt 一致）。 */
private fun solve033(): Long {
    var num = 1; var den = 1
    for (d in 10..99) for (n in 10 until d) {
        if (n % 10 == 0 && d % 10 == 0) continue
        val n1 = n / 10; val n2 = n % 10; val d1 = d / 10; val d2 = d % 10
        val a: Int; val b: Int
        if (n1 == d1) { a = n2; b = d2 } else if (n1 == d2) { a = n2; b = d1 }
        else if (n2 == d1) { a = n1; b = d2 } else if (n2 == d2) { a = n1; b = d1 }
        else continue
        if (b == 0) continue
        if (n.toLong() * b != d.toLong() * a) continue
        num *= n; den *= d
    }
    return (den / gcd(num.toLong(), den.toLong())).toLong()
}

/** PE 034 — 34 题优化解（与 content/problems/0034/solution.kt 一致）。 */
private fun solve034(): Long {
    val f = IntArray(10) { factorialInt(it) }
    var sum = 0L
    for (n in 3..2540160) {
        var x = n; var s = 0
        while (x > 0) { s += f[x % 10]; x /= 10 }
        if (s == n) sum += n
    }
    return sum
}

/** PE 035 — 35 题优化解（与 content/problems/0035/solution.kt 一致）。 */
private fun solve035(): Long {
    val N = 1_000_000
    val isP = sieve(N)
    var count = 0L
    for (n in 2 until N) {
        val s = n.toString()
        if (s.length > 1 && s.any { (it - '0') % 2 == 0 || it == '5' }) continue
        if (isCircularPrime(n, isP)) count++
    }
    return count
}

/** PE 036 — 36 题优化解（与 content/problems/0036/solution.kt 一致）。 */
private fun solve036(): Long {
    var sum = 0L
    for (n in 1..999999 step 2) {
        if (!isPalStr(n.toString())) continue
        if (isPalStr(Integer.toBinaryString(n))) sum += n
    }
    return sum
}

/** PE 037 — 37 题优化解（与 content/problems/0037/solution.kt 一致）。 */
private fun solve037(): Long {
    var sum = 0L; var count = 0; var n = 11
    while (count < 11) {
        val s = n.toString()
        var ok = true
        var left = 0L; var right = 0L
        for (i in s.indices) { left = left * 10 + (s[i] - '0'); if (!isPrime(left)) { ok = false; break } }
        if (ok) {
            var p = 10L
            while (p <= n) { right = n % p; if (!isPrime(right)) { ok = false; break }; p *= 10 }
        }
        if (ok) { sum += n; count++ }
        n++
    }
    return sum
}

/** PE 038 — 38 题优化解（与 content/problems/0038/solution.kt 一致）。 */
private fun solve038(): Long {
    var best = 0L
    for (n in 1..9999) {
        val sb = StringBuilder(); var k = 1
        while (sb.length < 9) { sb.append(n * k); k++ }
        if (sb.length == 9 && isPandigital19(sb.toString())) {
            val v = sb.toString().toLong()
            if (v > best) best = v
        }
    }
    return best
}

/** PE 039 — 39 题优化解（与 content/problems/0039/solution.kt 一致）。 */
private fun solve039(): Long {
    val count = IntArray(1001)
    for (m in 2..31) for (n in 1 until m) {
        if ((m - n) % 2 == 0 || gcd(m.toLong(), n.toLong()) != 1L) continue
        val p0 = m * m - n * n + 2 * m * n + m * m + n * n
        for (k in 1..1000 / p0) count[k * p0]++
    }
    var best = 0; var bestP = 0
    for (p in 1..1000) if (count[p] > best) { best = count[p]; bestP = p }
    return bestP.toLong()
}

/** PE 040 — 40 题优化解（与 content/problems/0040/solution.kt 一致）。 */
private fun solve040(): Long {
    val sb = StringBuilder()
    var n = 1
    while (sb.length < 1_000_000) { sb.append(n); n++ }
    var prod = 1L
    var d = 1
    while (d <= 1_000_000) { prod *= (sb[d - 1] - '0'); d *= 10 }
    return prod
}

/** PE 041 — 41 题优化解（与 content/problems/0041/solution.kt 一致）。 */
private fun solve041(): Long {
    // 8、9 位全数字数的数位和为 36、45，必被 3 整除，故答案至多 7 位
    for (n in 7_654_321 downTo 1) {
        if (!isPandigitalN(n)) continue
        if (isPrime(n.toLong())) return n.toLong()
    }
    return 0
}

/** PE 042 — 42 题优化解（与 content/problems/0042/solution.kt 一致）。 */
private fun solve042(): Long {
    val text = File(ContentIndex.resolveContentDir(), "problems/0042/words.txt").readText()
    return readWords(text).count { isTriangleNumber(alphabeticalValue(it)) }.toLong()
}

/** PE 043 — 43 题优化解（与 content/problems/0043/solution.kt 一致）。 */
private fun solve043(): Long {
    val primes = intArrayOf(2, 3, 5, 7, 11, 13, 17)
    var sum = 0L
    val digits = IntArray(10)
    val used = BooleanArray(10)
    val d = IntArray(10)
    fun dfs(pos: Int) {
        if (pos == 10) {
            var v = 0L; for (x in d) v = v * 10 + x
            sum += v; return
        }
        for (cand in 0..9) {
            if (used[cand]) continue
            used[cand] = true; d[pos] = cand
            var ok = true
            if (pos >= 3) {
                val num = d[pos - 2] * 100 + d[pos - 1] * 10 + d[pos]
                if (num % primes[pos - 3] != 0) ok = false
            }
            if (ok) dfs(pos + 1)
            used[cand] = false
        }
    }
    dfs(0)
    return sum
}

/** PE 044 — 44 题优化解（与 content/problems/0044/solution.kt 一致）。 */
private fun solve044(): Long {
    var i = 2
    while (true) {
        val pi = pentagonal(i)
        for (j in i - 1 downTo 1) {
            val pj = pentagonal(j)
            if (isPentagonal(pi - pj) && isPentagonal(pi + pj)) return pi - pj
        }
        i++
    }
}

/** PE 045 — 45 题优化解（与 content/problems/0045/solution.kt 一致）。 */
private fun solve045(): Long {
    var n = 144
    while (true) {
        val h = n.toLong() * (2L * n - 1)
        if (isPentagonal(h)) return h
        n++
    }
}

/** PE 046 — 46 题优化解（与 content/problems/0046/solution.kt 一致）。 */
private fun solve046(): Long {
    val N = 10_000
    val isP = sieve(N)
    val twiceSquares = HashSet<Int>()
    var k = 1
    while (2 * k * k <= N) { twiceSquares.add(2 * k * k); k++ }
    var n = 9
    while (true) {
        if (n % 2 == 1 && !isP[n]) {
            var found = false
            for (p in 2 until n) {
                if (isP[p] && twiceSquares.contains(n - p)) { found = true; break }
            }
            if (!found) return n.toLong()
        }
        n += 2
    }
}

/** PE 047 — 47 题优化解（与 content/problems/0047/solution.kt 一致）。 */
private fun solve047(): Long {
    val spf = buildSpf(1_000_000)
    var run = 0; var n = 2
    while (true) {
        if (distinctFactorCount(n, spf) == 4) { run++; if (run == 4) return (n - 3).toLong() }
        else run = 0
        n++
    }
}

/** PE 048 — 48 题优化解（与 content/problems/0048/solution.kt 一致）。 */
private fun solve048(): Long {
    val mod = BigInteger.TEN.pow(10)
    var sum = BigInteger.ZERO
    for (i in 1..1000) sum = sum.add(BigInteger.valueOf(i.toLong()).modPow(BigInteger.valueOf(i.toLong()), mod))
    return sum.mod(mod).toLong()
}

/** PE 049 — 49 题优化解（与 content/problems/0049/solution.kt 一致）。 */
private fun solve049(): Long {
    val isP = sieve(9999)
    val buckets = HashMap<String, MutableList<Int>>()
    for (n in 1000..9999) if (isP[n]) buckets.getOrPut(sortedDigits(n)) { ArrayList() }.add(n)
    var best = 0L
    for ((key, list) in buckets) {
        for (i in list.indices) for (j in i + 1 until list.size) {
            val c = 2 * list[j] - list[i]
            if (c > 9999 || !isP[c] || sortedDigits(c) != key) continue
            val s = "" + list[i] + list[j] + c
            if (s == "148748178147") continue
            val v = s.toLong()
            if (v > best) best = v
        }
    }
    return best
}

/** PE 050 — 50 题优化解（与 content/problems/0050/solution.kt 一致）。 */
private fun solve050(): Long {
    val N = 1_000_000
    val isP = sieve(N)
    val primes = ArrayList<Int>()
    for (i in 2 until N) if (isP[i]) primes.add(i)
    val prefix = LongArray(primes.size + 1)
    for (i in primes.indices) prefix[i + 1] = prefix[i] + primes[i]
    var bestLen = 0; var bestPrime = 0L
    for (i in 0 until primes.size) {
        if (primes.size - i <= bestLen) break
        for (j in i + bestLen + 1..primes.size) {
            val s = prefix[j] - prefix[i]
            if (s >= N) break
            if (isP[s.toInt()]) { bestLen = j - i; bestPrime = s }
        }
    }
    return bestPrime
}

private fun p051DigitCount(n: Int): Int {
    var d = 1
    var t = n
    while (t >= 10) { t /= 10; d++ }
    return d
}

/** 家族基元：掩码位清零；掩码位数字不全相同则返回 −1（该素数不属于此家族）。 */
private fun p051FamilyBase(n: Int, mask: Int): Int {
    var base = 0
    var pow = 1
    var t = n
    var m = mask
    var seen = -1
    var uniform = true
    while (t > 0) {
        val d = t % 10
        if (m and 1 == 1) {
            if (seen < 0) seen = d else if (seen != d) uniform = false
        } else {
            base += d * pow
        }
        m = m shr 1
        pow *= 10
        t /= 10
    }
    return if (uniform) base else -1
}

/** 掩码位的权重和 Σ10^pos：基元加上 d·weight 就等于把掩码位全换成数字 d。 */
private fun p051MaskWeight(mask: Int): Int {
    var weight = 0
    var pow = 1
    var m = mask
    while (m > 0) {
        if (m and 1 == 1) weight += pow
        pow *= 10
        m = m shr 1
    }
    return weight
}

private fun solve051(): Long {
    val limit = 999_999
    val isP = sieve(limit)
    for (n in 2..limit) {
        if (!isP[n]) continue
        val digits = p051DigitCount(n)
        val leadingBit = 1 shl (digits - 1)
        for (mask in 1 until (1 shl digits)) {
            val k = Integer.bitCount(mask)
            if (k % 3 != 0 || k == digits) continue
            val base = p051FamilyBase(n, mask)
            if (base < 0) continue
            val weight = p051MaskWeight(mask)
            val from = if (mask and leadingBit != 0) 1 else 0
            var primeCount = 0
            for (d in from..9) if (isP[base + d * weight]) primeCount++
            if (primeCount >= 8) return n.toLong()
        }
    }
    return 0L
}

/** 0–9 各数字的计数各占 4 bit，压进一个 Long 作为数字指纹。 */
private fun p052DigitSignature(n: Int): Long {
    var sig = 0L
    var t = n
    while (t > 0) {
        sig += 1L shl ((t % 10) * 4)
        t /= 10
    }
    return sig
}

private fun solve052(): Long {
    var x = 9
    while (true) {
        val sig = p052DigitSignature(x)
        var ok = true
        for (k in 2..6) {
            if (p052DigitSignature(k * x) != sig) { ok = false; break }
        }
        if (ok) return x.toLong()
        x += 9
    }
}

private fun solve053(): Long {
    val limit = 1_000_000L
    var count = 0L
    for (n in 1..100) {
        var c = 1L
        var r = 1
        while (2 * r <= n) {
            c = c * (n - r + 1) / r
            if (c > limit) {
                count += (n - 2 * r + 1).toLong()
                break
            }
            r++
        }
    }
    return count
}

private fun p054CardValue(card: String): Int = when (card[0]) {
    'T' -> 10
    'J' -> 11
    'Q' -> 12
    'K' -> 13
    'A' -> 14
    else -> card[0] - '0'
}

/** 排序键：牌型等级 ×15^5 + 5 个逐级比较点数（不足补 0），点数按「出现次数降序、点数降序」展开。 */
private fun p054HandKey(hand: List<String>): Long {
    val values = IntArray(5) { p054CardValue(hand[it]) }
    values.sort()

    var flush = true
    for (i in 1..4) if (hand[i][1] != hand[0][1]) { flush = false; break }

    val counts = IntArray(15)
    for (v in values) counts[v]++
    var c4 = 0
    var c3 = 0
    var c2 = 0
    for (v in 2..14) when (counts[v]) {
        4 -> c4++
        3 -> c3++
        2 -> c2++
    }

    val ordered = IntArray(5)
    var n = 0
    for (c in 4 downTo 1) for (v in 14 downTo 2) if (counts[v] == c) ordered[n++] = v

    val wheel = counts[14] == 1 && counts[5] == 1 && counts[4] == 1 && counts[3] == 1 && counts[2] == 1
    val straight = n == 5 && values[4] - values[0] == 4

    val category = when {
        flush && (straight || wheel) -> 8
        c4 == 1 -> 7
        c3 == 1 && c2 == 1 -> 6
        flush -> 5
        straight || wheel -> 4
        c3 == 1 -> 3
        c2 == 2 -> 2
        c2 == 1 -> 1
        else -> 0
    }
    val tie = when (category) {
        8, 4 -> intArrayOf(if (wheel) 5 else values[4])
        5 -> IntArray(5) { values[4 - it] }
        else -> ordered
    }

    var key = category.toLong()
    for (i in 0 until 5) key = key * 15 + (if (i < tie.size) tie[i] else 0)
    return key
}

private fun solve054(): Long {
    val text = File(ContentIndex.resolveContentDir(), "problems/0054/poker.txt").readText()
    var wins = 0L
    for (line in text.lines()) {
        if (line.isBlank()) continue
        val cards = line.trim().split(" ")
        if (p054HandKey(cards.subList(0, 5)) > p054HandKey(cards.subList(5, 10))) wins++
    }
    return wins
}

private fun p055ReverseDigits(n: BigInteger): BigInteger = n.toString().reversed().toBigInteger()

private fun p055IsPalindromic(n: BigInteger): Boolean {
    val s = n.toString()
    var i = 0
    var j = s.length - 1
    while (i < j) {
        if (s[i] != s[j]) return false
        i++
        j--
    }
    return true
}

private fun solve055(): Long {
    var count = 0L
    for (i in 1 until 10000) {
        var n = BigInteger.valueOf(i.toLong())
        var isLychrel = true
        var iteration = 0
        while (iteration < 50) {
            n += p055ReverseDigits(n)
            iteration++
            if (p055IsPalindromic(n)) {
                isLychrel = false
                break
            }
        }
        if (isLychrel) count++
    }
    return count
}

private fun p056DigitSum(n: BigInteger): Int {
    var s = 0
    for (c in n.toString()) s += c - '0'
    return s
}

private fun solve056(): Long {
    var best = 0
    for (a in 2..99) {
        val base = BigInteger.valueOf(a.toLong())
        var power = BigInteger.ONE
        for (b in 1..99) {
            power = power.multiply(base)
            val s = p056DigitSum(power)
            if (s > best) best = s
        }
    }
    return best.toLong()
}

private fun p057Add(a: IntArray, b: IntArray): IntArray {
    val n = maxOf(a.size, b.size)
    val r = IntArray(n + 1)
    var carry = 0
    for (i in 0 until n) {
        val s = (if (i < a.size) a[i] else 0) + (if (i < b.size) b[i] else 0) + carry
        r[i] = s % 10
        carry = s / 10
    }
    r[n] = carry
    var len = n + 1
    while (len > 1 && r[len - 1] == 0) len--
    return if (len == n + 1) r else r.copyOf(len)
}

private fun p057Double(a: IntArray): IntArray {
    val r = IntArray(a.size + 1)
    var carry = 0
    for (i in a.indices) {
        val s = 2 * a[i] + carry
        r[i] = s % 10
        carry = s / 10
    }
    r[a.size] = carry
    var len = a.size + 1
    while (len > 1 && r[len - 1] == 0) len--
    return if (len == a.size + 1) r else r.copyOf(len)
}

private fun solve057(): Long {
    var num = intArrayOf(3)
    var den = intArrayOf(2)
    var count = 0L
    for (k in 1..1000) {
        if (k > 1) {
            val nextNum = p057Add(num, p057Double(den))
            val nextDen = p057Add(num, den)
            num = nextNum
            den = nextDen
        }
        if (num.size > den.size) count++
    }
    return count
}

private fun p058ModPow(base: Long, exp: Long, mod: Long): Long {
    var b = base % mod
    var e = exp
    var r = 1L
    while (e > 0L) {
        if (e and 1L == 1L) r = r * b % mod
        b = b * b % mod
        e = e shr 1
    }
    return r
}

private fun p058IsPrime(n: Long): Boolean {
    if (n < 2L) return false
    for (p in longArrayOf(2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L)) {
        if (n == p) return true
        if (n % p == 0L) return false
    }
    var d = n - 1L
    var s = 0
    while (d and 1L == 0L) { d = d shr 1; s++ }
    for (a in longArrayOf(2L, 3L, 5L, 7L)) {
        var x = p058ModPow(a, d, n)
        if (x == 1L || x == n - 1L) continue
        var witness = true
        for (r in 1 until s) {
            x = x * x % n
            if (x == n - 1L) { witness = false; break }
        }
        if (witness) return false
    }
    return true
}

private fun solve058(): Long {
    var side = 3L
    var primeCount = 0L
    while (true) {
        val corner = side * side
        val step = side - 1L
        for (k in 1..3) if (p058IsPrime(corner - k * step)) primeCount++
        if (10L * primeCount < 2L * side - 1L) return side
        side += 2L
    }
}

private val p059Freq: DoubleArray = DoubleArray(128) { -1000.0 }.also { f ->
    val letters = "etaoinshrdlcumwfgypbvkjxqz"
    val weights = doubleArrayOf(
        12.7, 9.1, 8.2, 7.5, 7.0, 6.7, 6.3, 6.1, 6.0, 4.3, 4.0, 2.8, 2.8,
        2.4, 2.4, 2.2, 2.0, 2.0, 1.9, 1.5, 1.0, 0.8, 0.15, 0.15, 0.1, 0.07
    )
    for (i in letters.indices) f[letters[i].code] = weights[i]
    f[' '.code] = 15.0
    for (c in ",.;:!?'\"-()") f[c.code] = 0.5
    for (c in 32..126) f[c] = maxOf(f[c], 0.0)
}

private fun p059LoadCipher(): IntArray =
    File(ContentIndex.resolveContentDir(), "problems/0059/cipher.txt")
        .readText().trim().split(",").map { it.trim().toInt() }.toIntArray()

private fun p059BestKey(bytes: IntArray): String {
    val sb = StringBuilder()
    for (r in 0 until 3) {
        var bestScore = -1e18
        var bestCh = 'a'
        for (c in 'a'..'z') {
            var s = 0.0
            var i = r
            while (i < bytes.size) { s += p059Freq[bytes[i] xor c.code]; i += 3 }
            if (s > bestScore) { bestScore = s; bestCh = c }
        }
        sb.append(bestCh)
    }
    return sb.toString()
}

private fun p059Decrypt(key: String, bytes: IntArray): String {
    val out = CharArray(bytes.size)
    for (i in bytes.indices) out[i] = (bytes[i] xor key[i % 3].code).toChar()
    return String(out)
}

private fun solve059(): Long {
    val bytes = p059LoadCipher()
    val text = p059Decrypt(p059BestKey(bytes), bytes)
    var sum = 0L
    for (c in text) sum += c.code
    return sum
}

/** PE 060 — 60 题优化解（与 content/problems/0060/solution.kt 一致）。 */

private val p060MrBases = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)

private fun p060ModPow(base: Long, exp: Long, mod: Long): Long {
    var b = base % mod
    var e = exp
    var r = 1L
    while (e > 0) {
        if (e and 1L == 1L) r = r * b % mod
        b = b * b % mod
        e = e shr 1
    }
    return r
}

private fun p060IsPrime64(n: Long): Boolean {
    if (n < 2L) return false
    for (p in p060MrBases) if (n % p == 0L) return n == p
    var d = n - 1
    var s = 0
    while (d and 1L == 0L) { d = d shr 1; s++ }
    outer@ for (a in p060MrBases) {
        var x = p060ModPow(a, d, n)
        if (x == 1L || x == n - 1) continue
        var r = 1
        while (r < s) {
            x = x * x % n
            if (x == n - 1) continue@outer
            r++
        }
        return false
    }
    return true
}

private fun p060Digits(n: Int): Int = when {
    n < 10 -> 1
    n < 100 -> 2
    n < 1000 -> 3
    else -> 4
}

private fun p060Concat(a: Int, b: Int): Long {
    var p = 1L
    repeat(p060Digits(b)) { p *= 10L }
    return a * p + b
}

private fun solve060(): Long {
    val isP = sieve(9999)
    val primes = ArrayList<Int>()
    for (i in 2..9999) if (isP[i]) primes.add(i)
    val m = primes.size
    val adj = Array(m) { java.util.BitSet(m) }
    for (i in 0 until m) {
        val a = primes[i]
        for (j in i + 1 until m) {
            val b = primes[j]
            if (p060IsPrime64(p060Concat(a, b)) && p060IsPrime64(p060Concat(b, a))) adj[i].set(j)
        }
    }

    var best = Long.MAX_VALUE
    fun dfs(size: Int, cand: java.util.BitSet, sum: Long) {
        if (size == 5) {
            if (sum < best) best = sum
            return
        }
        var i = cand.nextSetBit(0)
        while (i >= 0) {
            if (best != Long.MAX_VALUE && sum + (5 - size).toLong() * primes[i] >= best) return
            val next = cand.clone() as java.util.BitSet
            next.and(adj[i])
            dfs(size + 1, next, sum + primes[i])
            i = cand.nextSetBit(i + 1)
        }
    }

    val all = java.util.BitSet(m)
    all.set(0, m)
    dfs(0, all, 0L)
    return best
}

/** PE 061 — 61 题优化解（与 content/problems/0061/solution.kt 一致）。 */

private fun p061Polygonal(s: Int, n: Int): Int = when (s) {
    3 -> n * (n + 1) / 2
    4 -> n * n
    5 -> n * (3 * n - 1) / 2
    6 -> n * (2 * n - 1)
    7 -> n * (5 * n - 3) / 2
    8 -> n * (3 * n - 2)
    else -> 0
}

private fun solve061(): Long {
    val byPrefix = HashMap<Int, MutableList<IntArray>>()
    for (s in 3..8) {
        var n = 1
        while (true) {
            val v = p061Polygonal(s, n)
            if (v >= 10000) break
            if (v >= 1000) byPrefix.getOrPut(v / 100) { ArrayList() }.add(intArrayOf(v, s))
            n++
        }
    }

    val chain = ArrayList<Int>()
    var answer = 0L
    fun dfs(usedTypes: Int, firstPrefix: Int, prevSuffix: Int): Boolean {
        if (chain.size == 6) {
            if (prevSuffix == firstPrefix) {
                answer = chain.sumOf { it.toLong() }
                return true
            }
            return false
        }
        val next = byPrefix[prevSuffix] ?: return false
        for (e in next) {
            val v = e[0]
            val s = e[1]
            if (usedTypes and (1 shl s) != 0) continue
            if (chain.contains(v)) continue
            chain.add(v)
            if (dfs(usedTypes or (1 shl s), firstPrefix, v % 100)) return true
            chain.removeAt(chain.size - 1)
        }
        return false
    }

    for (list in byPrefix.values) {
        for (e in list) {
            chain.clear()
            chain.add(e[0])
            if (dfs(1 shl e[1], e[0] / 100, e[0] % 100)) return answer
        }
    }
    return answer
}

/** PE 062 — 62 题优化解（与 content/problems/0062/solution.kt 一致）。 */

private fun p062DigitKey(n: Long): Long {
    var x = n
    var key = 0L
    while (x > 0L) {
        key += 1L shl ((x % 10L).toInt() * 4)
        x /= 10L
    }
    return key
}

private fun solve062(): Long {
    val groups = HashMap<Long, MutableList<Long>>()
    var n = 1L
    while (n < 10000L) {
        val c = n * n * n
        groups.getOrPut(p062DigitKey(c)) { ArrayList() }.add(c)
        n++
    }

    var best = Long.MAX_VALUE
    for (list in groups.values) {
        if (list.size != 5) continue
        for (c in list) if (c < best) best = c
    }
    return best
}

/** PE 063 — 63 题优化解（与 content/problems/0063/solution.kt 一致）。 */

private fun solve063(): Long {
    val ten = BigInteger.TEN
    val nine = BigInteger.valueOf(9L)
    var count = 0L
    var n = 1
    while (true) {
        val lo = ten.pow(n - 1)
        if (nine.pow(n).compareTo(lo) < 0) break
        val hi = ten.pow(n)
        for (a in 1..9) {
            val p = BigInteger.valueOf(a.toLong()).pow(n)
            if (p.compareTo(lo) >= 0 && p.compareTo(hi) < 0) count++
        }
        n++
    }
    return count
}

/**
 * PE 064 — 奇数周期的平方根 = 1322（与 content/problems/0064/solution.kt 一致）。
 */

private fun p064Isqrt(n: Int): Int {
    var r = Math.sqrt(n.toDouble()).toInt()
    while (r.toLong() * r > n) r--
    while ((r + 1).toLong() * (r + 1) <= n) r++
    return r
}

private fun solve064(): Long {
    var count = 0L
    for (n in 2..10000) {
        val a0 = p064Isqrt(n)
        if (a0 * a0 == n) continue
        var m = 0
        var d = 1
        var a = a0
        var period = 0
        while (a != 2 * a0) {
            m = d * a - m
            d = (n - m * m) / d
            a = (a0 + m) / d
            period++
        }
        if (period and 1 == 1) count++
    }
    return count
}

/**
 * PE 065 — e 的第 100 个渐近分数的数位和 = 272（与 content/problems/0065/solution.kt 一致）。
 */

private fun p065Coefficient(k: Int): Int = when {
    k == 0 -> 2
    k % 3 == 2 -> 2 * (k + 1) / 3
    else -> 1
}

private fun solve065(): Long {
    var pPrev = BigInteger.ZERO // p_{n-2}
    var p = BigInteger.ONE      // p_{n-1}
    for (k in 0..99) {
        val a = BigInteger.valueOf(p065Coefficient(k).toLong())
        val next = a.multiply(p).add(pPrev)
        pPrev = p
        p = next
    }
    var sum = 0L
    var x = p
    while (x.signum() > 0) {
        sum += x.mod(BigInteger.TEN).toLong()
        x = x.divide(BigInteger.TEN)
    }
    return sum
}

/**
 * PE 066 — 使 Pell 方程 x² − Dy² = 1 最小解最大的 D = 661（与 content/problems/0066/solution.kt 一致）。
 */

private fun p066Isqrt(n: Int): Int {
    var r = Math.sqrt(n.toDouble()).toInt()
    while (r.toLong() * r > n) r--
    while ((r + 1).toLong() * (r + 1) <= n) r++
    return r
}

private fun solve066(): Long {
    var bestD = 0L
    var bestX = BigInteger.ZERO
    for (d in 2..1000) {
        val a0 = p066Isqrt(d)
        if (a0 * a0 == d) continue
        val db = BigInteger.valueOf(d.toLong())
        var m = 0
        var den = 1
        var a = a0
        var pPrev = BigInteger.ONE
        var p = BigInteger.valueOf(a0.toLong())
        var qPrev = BigInteger.ZERO
        var q = BigInteger.ONE
        while (true) {
            if (p.multiply(p).subtract(db.multiply(q).multiply(q)) == BigInteger.ONE) break
            m = den * a - m
            den = (d - m * m) / den
            a = (a0 + m) / den
            val ab = BigInteger.valueOf(a.toLong())
            val pNext = p.multiply(ab).add(pPrev)
            val qNext = q.multiply(ab).add(qPrev)
            pPrev = p; p = pNext
            qPrev = q; q = qNext
        }
        if (p > bestX) {
            bestX = p
            bestD = d.toLong()
        }
    }
    return bestD
}

/**
 * PE 067 — 100 行三角形的最大路径和 = 7273（与 content/problems/0067/solution.kt 一致）。
 */

private fun solve067(): Long {
    val rows = File(ContentIndex.resolveContentDir(), "problems/0067/triangle.txt")
        .readLines()
        .filter { it.isNotBlank() }
        .map { line -> line.trim().split(Regex("\\s+")).map(String::toInt) }
    val dp = rows.last().toIntArray()
    for (r in rows.size - 2 downTo 0) {
        val row = rows[r]
        for (c in row.indices) {
            dp[c] = row[c] + maxOf(dp[c], dp[c + 1])
        }
    }
    return dp[0].toLong()
}

// 068 · 幻五边形环
// 思路：五条线之和 5S = Σouter + 2Σinner = 55 + Σinner，故 S = (55 + Σinner)/5；
// 内点五元组确定后外点被唯一反推：outer_k = S − inner_k − inner_{k+1}。
// 枚举 C(10,5)·5! = 30240 种内点排列，排除 10 在内点的 17 位串后取最大 16 位串。
private fun solve068(): Long {
    val inner = IntArray(5)
    val used = BooleanArray(11)
    var best = 0L

    fun evaluate(innerSum: Int) {
        if ((55 + innerSum) % 5 != 0) return
        val s = (55 + innerSum) / 5
        val outer = IntArray(5)
        val seen = BooleanArray(11)
        for (k in 0 until 5) {
            val o = s - inner[k] - inner[(k + 1) % 5]
            if (o < 1 || o > 10 || used[o] || seen[o]) return
            seen[o] = true
            outer[k] = o
        }
        var hasTen = false
        for (o in outer) if (o == 10) hasTen = true
        if (!hasTen) return // 只有 10 在外点时才得到 16 位串

        var start = 0
        for (k in 1 until 5) if (outer[k] < outer[start]) start = k
        val sb = StringBuilder()
        for (k in 0 until 5) {
            val i = (start + k) % 5
            sb.append(outer[i]).append(inner[i]).append(inner[(i + 1) % 5])
        }
        val v = sb.toString().toLong()
        if (v > best) best = v
    }

    fun assign(pos: Int, sum: Int) {
        if (pos == 5) {
            evaluate(sum)
            return
        }
        for (v in 1..10) {
            if (used[v]) continue
            used[v] = true
            inner[pos] = v
            assign(pos + 1, sum + v)
            used[v] = false
        }
    }

    assign(0, 0)
    return best
}

// 069 · 最大欧拉函数比
// 思路：n/φ(n) = Π_{p|n} p/(p−1)，每个因子随 p 增大而减小、因子越多乘积越大，
// 故最优解是从 2 开始、尽可能长的素数前缀积（再乘一个素数就越过 10^6）。
private fun solve069(): Long {
    var best = 1L
    var p = 2L
    while (best * p <= 1_000_000L) {
        best *= p
        p++
        while (!isPrime(p)) p++
    }
    return best
}

// 070 · 欧拉函数与数字排列
// 思路：n/φ(n) = Π p/(p−1) 要小则不同素因子要少。含 ≥3 个不同素因子时
// p^3 < n < 10^7 ⇒ p ≤ 211 ⇒ n/φ(n) ≥ 211/210 ≈ 1.00476，必输给半素数候选；
// 故主搜索枚举 n = p·q（φ = (p−1)(q−1)），另单独扫描素数幂 n = p^a。
// 数位计数判排列，交叉相乘比较比值。
private fun p070SameDigits(a: Long, b: Long): Boolean {
    val cnt = IntArray(10)
    var x = a
    while (x > 0L) {
        cnt[(x % 10L).toInt()]++
        x /= 10L
    }
    var y = b
    while (y > 0L) {
        cnt[(y % 10L).toInt()]--
        y /= 10L
    }
    for (c in cnt) if (c != 0) return false
    return true
}

private fun solve070(): Long {
    val limit = 10_000_000L
    val primes = primesUpTo(5_000_000) // p ≥ 2 ⇒ q < 10^7/2
    var bestN = 0L
    var bestPhi = 0L

    // 两个素因子：n = p·q
    for (ai in primes.indices) {
        val p = primes[ai]
        if (p * p >= limit) break
        for (bi in ai + 1 until primes.size) {
            val q = primes[bi]
            val n = p * q
            if (n >= limit) break
            val phi = (p - 1) * (q - 1)
            if (!p070SameDigits(n, phi)) continue
            if (bestN == 0L || n * bestPhi < bestN * phi) {
                bestN = n
                bestPhi = phi
            }
        }
    }

    // 单个素因子：n = p^a（a ≥ 2；a = 1 时 n 与 n−1 数位和相差 1，不可能互为排列）
    for (p in primes) {
        if (p * p >= limit) break
        var n = p * p
        while (n < limit) {
            val phi = n / p * (p - 1)
            if (p070SameDigits(n, phi) && (bestN == 0L || n * bestPhi < bestN * phi)) {
                bestN = n
                bestPhi = phi
            }
            n *= p
        }
    }
    return bestN
}

// 071 · 有序分数
// 思路：设左邻为 p/q，则 3/7 − p/q = (3q − 7p)/(7q) = k/(7q)，k 为正整数。
// 固定 q 时 k 取最小可能值；k=1 时误差 1/(7q) < 2/(7·10^6)，故最优必在 k=1 候选中，
// 且 q 取满足 3q ≡ 1 (mod 7)（即 q ≡ 5 (mod 7)）的最大值 999997，得 p = 428570。
private fun solve071(): Long {
    var q = 1_000_000L
    while ((3 * q - 1) % 7 != 0L) q--
    return (3 * q - 1) / 7
}

private fun solve072(): Long {
    val n = 1_000_000
    val phi = IntArray(n + 1)
    val composite = BooleanArray(n + 1)
    val primes = IntArray(n / 10 + 64)
    var pc = 0
    phi[1] = 1
    for (i in 2..n) {
        if (!composite[i]) {
            primes[pc++] = i
            phi[i] = i - 1
        }
        var j = 0
        while (j < pc) {
            val p = primes[j]
            val ip = i.toLong() * p
            if (ip > n) break
            val m = ip.toInt()
            composite[m] = true
            if (i % p == 0) {
                phi[m] = phi[i] * p
                break
            }
            phi[m] = phi[i] * (p - 1)
            j++
        }
    }
    var total = 0L
    for (d in 2..n) total += phi[d]
    return total
}

private fun solve073(): Long {
    val n = 12000
    var count = 0L
    for (d in 2..n) {
        val lo = d / 3 + 1
        val hi = (d - 1) / 2
        for (x in lo..hi) if (gcd(x.toLong(), d.toLong()) == 1L) count++
    }
    return count
}

private val p074Fact = intArrayOf(1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880)

private fun p074DigitFactSum(n: Int): Int {
    var x = n
    var s = 0
    while (x > 0) {
        s += p074Fact[x % 10]
        x /= 10
    }
    return s
}

private fun solve074(): Long {
    val limit = 1_000_000
    val cap = 2_177_280
    val len = IntArray(cap + 1) { -1 }
    val seen = IntArray(cap + 1) { -1 }
    val path = IntArray(256)
    var total = 0L
    for (start in 1 until limit) {
        var x = start
        var depth = 0
        while (true) {
            val cached = len[x]
            if (cached >= 0) {
                for (j in 0 until depth) len[path[j]] = (depth - j) + cached
                break
            }
            val at = seen[x]
            if (at >= 0) {
                for (j in at until depth) len[path[j]] = depth - at
                for (j in 0 until at) len[path[j]] = depth - j
                break
            }
            seen[x] = depth
            path[depth] = x
            depth++
            x = p074DigitFactSum(x)
        }
        for (j in 0 until depth) seen[path[j]] = -1
        if (len[start] == 60) total++
    }
    return total
}

private fun solve075(): Long {
    val limit = 1_500_000
    val count = IntArray(limit + 1)
    var m = 2
    while (2L * m * (m + 1) <= limit) {
        for (n in 1 until m) {
            if (((m - n) and 1) == 1 && gcd(m.toLong(), n.toLong()) == 1L) {
                val base = 2 * m * (m + n)
                var p = base
                while (p <= limit) {
                    count[p]++
                    p += base
                }
            }
        }
        m++
    }
    var total = 0L
    for (l in 1..limit) if (count[l] == 1) total++
    return total
}

private fun solve076(): Long {
    val n = 100
    val dp = LongArray(n + 1)
    dp[0] = 1L
    for (coin in 1..n) {
        for (s in coin..n) dp[s] += dp[s - coin]
    }
    return dp[n] - 1L
}

private fun solve077(): Long {
    val limit = 100
    val isPrime = BooleanArray(limit + 1) { it >= 2 }
    var p = 2
    while (p * p <= limit) {
        if (isPrime[p]) {
            var m = p * p
            while (m <= limit) {
                isPrime[m] = false
                m += p
            }
        }
        p++
    }

    val ways = LongArray(limit + 1)
    ways[0] = 1L
    for (prime in 2..limit) {
        if (!isPrime[prime]) continue
        for (s in prime..limit) ways[s] += ways[s - prime]
    }

    for (n in 2..limit) if (ways[n] > 5000L) return n.toLong()
    error("limit $limit too small")
}

private fun solve078(): Long {
    val mod = 1_000_000L
    val limit = 100_000
    val p = IntArray(limit + 1)
    p[0] = 1

    for (n in 1..limit) {
        var total = 0L
        var k = 1
        while (true) {
            val g1 = k * (3 * k - 1) / 2
            if (g1 > n) break
            val sign = if (k % 2 == 1) 1L else -1L
            total += sign * p[n - g1]
            val g2 = k * (3 * k + 1) / 2
            if (g2 <= n) total += sign * p[n - g2]
            k++
        }
        val value = ((total % mod) + mod) % mod
        p[n] = value.toInt()
        if (value == 0L) return n.toLong()
    }
    error("limit $limit too small")
}

private fun solve079(): Long {
    val attempts = File(dev.pekt.content.ContentIndex.resolveContentDir(), "problems/0079/keylog.txt")
        .readText().lines().map { it.trim() }.filter { it.isNotEmpty() }

    val before = Array(10) { BooleanArray(10) }
    val present = BooleanArray(10)
    for (attempt in attempts) {
        for (i in attempt.indices) {
            val a = attempt[i] - '0'
            present[a] = true
            for (j in i + 1 until attempt.length) before[a][attempt[j] - '0'] = true
        }
    }

    val used = BooleanArray(10)
    val builder = StringBuilder()
    repeat(present.count { it }) {
        var candidate = -1
        for (d in 0..9) {
            if (!present[d] || used[d]) continue
            var hasUnusedPredecessor = false
            for (e in 0..9) {
                if (e != d && present[e] && !used[e] && before[e][d]) {
                    hasUnusedPredecessor = true
                    break
                }
            }
            if (!hasUnusedPredecessor) {
                require(candidate == -1) { "topological order is not unique" }
                candidate = d
            }
        }
        require(candidate != -1) { "cycle detected in precedence graph" }
        used[candidate] = true
        builder.append('0' + candidate)
    }
    return builder.toString().toLong()
}

private fun p080DigitSum(n: BigInteger): Int {
    var x = n
    var s = 0
    while (x.signum() != 0) {
        s += x.mod(BigInteger.TEN).toInt()
        x = x.divide(BigInteger.TEN)
    }
    return s
}

private fun solve080(): Long {
    val scale = BigInteger.TEN.pow(198)
    var total = 0L
    for (n in 1..100) {
        val r = Math.sqrt(n.toDouble()).toInt()
        if (r * r == n) continue
        total += p080DigitSum(BigInteger.valueOf(n.toLong()).multiply(scale).sqrt())
    }
    return total
}

private fun p081Parse(text: String): Array<IntArray> =
    text.lines().filter { it.isNotBlank() }
        .map { line -> line.trim().split(",").map { it.trim().toInt() }.toIntArray() }
        .toTypedArray()

private fun solve081(): Long {
    val g = p081Parse(
        File(ContentIndex.resolveContentDir(), "problems/0081/matrix.txt").readText()
    )
    val n = g.size
    val dp = Array(n) { LongArray(n) }
    for (r in 0 until n) {
        for (c in 0 until n) {
            val cell = g[r][c].toLong()
            dp[r][c] = if (r == 0 && c == 0) cell else {
                val up = if (r > 0) dp[r - 1][c] else Long.MAX_VALUE
                val left = if (c > 0) dp[r][c - 1] else Long.MAX_VALUE
                cell + minOf(up, left)
            }
        }
    }
    return dp[n - 1][n - 1]
}

private fun p082Parse(text: String): Array<IntArray> =
    text.lines().filter { it.isNotBlank() }
        .map { line -> line.trim().split(",").map { it.trim().toInt() }.toIntArray() }
        .toTypedArray()

private fun solve082(): Long {
    val g = p082Parse(
        File(ContentIndex.resolveContentDir(), "problems/0082/matrix.txt").readText()
    )
    val n = g.size
    var cur = LongArray(n) { g[it][0].toLong() }
    for (c in 1 until n) {
        val next = LongArray(n) { cur[it] + g[it][c] }
        for (r in 1 until n) next[r] = minOf(next[r], next[r - 1] + g[r][c])
        for (r in n - 2 downTo 0) next[r] = minOf(next[r], next[r + 1] + g[r][c])
        cur = next
    }
    return cur.min()
}

private fun p083Parse(text: String): Array<IntArray> =
    text.lines().filter { it.isNotBlank() }
        .map { line -> line.trim().split(",").map { it.trim().toInt() }.toIntArray() }
        .toTypedArray()

private fun solve083(): Long {
    val g = p083Parse(
        File(ContentIndex.resolveContentDir(), "problems/0083/matrix.txt").readText()
    )
    val n = g.size
    val inf = Long.MAX_VALUE / 4
    val dist = Array(n) { LongArray(n) { inf } }
    val pq = java.util.PriorityQueue<LongArray>(compareBy { it[0] })
    dist[0][0] = g[0][0].toLong()
    pq.add(longArrayOf(dist[0][0], 0L, 0L))
    val dr = intArrayOf(-1, 1, 0, 0)
    val dc = intArrayOf(0, 0, -1, 1)
    while (pq.isNotEmpty()) {
        val top = pq.poll()
        val d = top[0]
        val r = top[1].toInt()
        val c = top[2].toInt()
        if (d > dist[r][c]) continue
        if (r == n - 1 && c == n - 1) return d
        for (k in 0 until 4) {
            val nr = r + dr[k]
            val nc = c + dc[k]
            if (nr < 0 || nr >= n || nc < 0 || nc >= n) continue
            val nd = d + g[nr][nc]
            if (nd < dist[nr][nc]) {
                dist[nr][nc] = nd
                pq.add(longArrayOf(nd, nr.toLong(), nc.toLong()))
            }
        }
    }
    return dist[n - 1][n - 1]
}

private fun p084NextOf(squares: IntArray, from: Int): Int {
    for (s in squares) if (s > from) return s
    return squares[0]
}

private fun p084ResolveDist(t: Int): DoubleArray {
    val board = 40
    val jail = 10
    val d = DoubleArray(board)
    if (t == 30) {
        d[jail] = 1.0
        return d
    }
    if (t == 2 || t == 17 || t == 33) {
        d[0] += 1.0 / 16
        d[jail] += 1.0 / 16
        d[t] += 14.0 / 16
        return d
    }
    if (t == 7 || t == 22 || t == 36) {
        d[t] += 6.0 / 16
        d[0] += 1.0 / 16
        d[jail] += 1.0 / 16
        d[11] += 1.0 / 16
        d[24] += 1.0 / 16
        d[39] += 1.0 / 16
        d[5] += 1.0 / 16
        d[p084NextOf(intArrayOf(5, 15, 25, 35), t)] += 2.0 / 16
        d[p084NextOf(intArrayOf(12, 28), t)] += 1.0 / 16
        val back = (t - 3 + board) % board
        val sub = p084ResolveDist(back)
        for (i in 0 until board) d[i] += sub[i] / 16
        return d
    }
    d[t] = 1.0
    return d
}

private fun solve084(): Long {
    val board = 40
    val jail = 10
    val sides = 4
    val states = board * 3
    val diceProb = 1.0 / (sides * sides)
    val matrix = Array(states) { DoubleArray(states) }
    for (pos in 0 until board) {
        for (doubles in 0 until 3) {
            val from = pos * 3 + doubles
            for (i in 1..sides) {
                for (j in 1..sides) {
                    if (i == j && doubles == 2) {
                        matrix[from][jail * 3] += diceProb
                        continue
                    }
                    val dist = p084ResolveDist((pos + i + j) % board)
                    val nextDoubles = if (i == j) doubles + 1 else 0
                    for (q in 0 until board) {
                        val w = dist[q]
                        if (w != 0.0) matrix[from][q * 3 + nextDoubles] += w * diceProb
                    }
                }
            }
        }
    }
    var v = DoubleArray(states)
    v[0] = 1.0
    var iter = 0
    while (iter < 5000) {
        val next = DoubleArray(states)
        for (a in 0 until states) {
            val va = v[a]
            if (va == 0.0) continue
            val row = matrix[a]
            for (b in 0 until states) next[b] += va * row[b]
        }
        var delta = 0.0
        for (a in 0 until states) {
            val dd = Math.abs(next[a] - v[a])
            if (dd > delta) delta = dd
        }
        v = next
        iter++
        if (delta < 1e-15) break
    }
    val p = DoubleArray(board)
    for (q in 0 until board) p[q] = v[q * 3] + v[q * 3 + 1] + v[q * 3 + 2]
    val order = (0 until board).sortedByDescending { p[it] }
    var result = 0L
    for (k in 0 until 3) result = result * 100 + order[k]
    return result
}

private fun p085RectCount(m: Long, n: Long): Long = m * (m + 1) * n * (n + 1) / 4

private fun solve085(): Long {
    val target = 2_000_000L
    var bestDiff = Long.MAX_VALUE
    var bestArea = 0L
    var m = 1L
    while (m * (m + 1) / 2 <= target) {
        val need = 4.0 * target / (m * (m + 1))
        val n0 = ((Math.sqrt(1.0 + 4.0 * need) - 1.0) / 2.0).toLong()
        var n = maxOf(1L, n0 - 2)
        while (n <= n0 + 2) {
            val diff = Math.abs(p085RectCount(m, n) - target)
            if (diff < bestDiff) {
                bestDiff = diff
                bestArea = m * n
            }
            n++
        }
        m++
    }
    return bestArea
}

private fun p086IsSquare(v: Long): Boolean {
    val r = Math.sqrt(v.toDouble()).toLong()
    return r * r == v || (r + 1) * (r + 1) == v
}

private fun solve086(): Long {
    val target = 1_000_000L
    var count = 0L
    var maxSide = 0L
    while (count <= target) {
        maxSide++
        var m = 2L
        while (m <= 2 * maxSide) {
            if (p086IsSquare(m * m + maxSide * maxSide)) {
                val lo = (m + 1) / 2
                val hi = Math.min(m - 1, maxSide)
                if (hi >= lo) count += hi - lo + 1
            }
            m++
        }
    }
    return maxSide
}

private fun solve087(): Long {
    val limit = 50_000_000
    val primes = primesUpTo(7071).map { it.toInt() }
    val seen = BooleanArray(limit)
    var count = 0L
    for (p in primes) {
        val p2 = p * p
        if (p2 + 8 + 16 >= limit) break
        for (q in primes) {
            val q3 = q * q * q
            if (p2 + q3 + 16 >= limit) break
            for (r in primes) {
                val r4 = r * r * r * r
                val s = p2 + q3 + r4
                if (s >= limit) break
                if (!seen[s]) {
                    seen[s] = true
                    count++
                }
            }
        }
    }
    return count
}

private const val p088KMax = 12000
private const val p088Limit = 2 * p088KMax

private fun p088Search(p: Int, s: Int, m: Int, minFactor: Int, best: IntArray) {
    var f = minFactor
    while (f <= p088Limit / p) {
        val p2 = p * f
        val s2 = s + f
        val m2 = m + 1
        val k = m2 + (p2 - s2)
        if (k <= p088KMax && p2 < best[k]) best[k] = p2
        if (p2 * f <= p088Limit) p088Search(p2, s2, m2, f, best)
        f++
    }
}

private fun solve088(): Long {
    val best = IntArray(p088KMax + 1) { Int.MAX_VALUE }
    p088Search(1, 0, 0, 2, best)
    var sum = 0L
    val seen = HashSet<Int>()
    for (k in 2..p088KMax) if (best[k] != Int.MAX_VALUE && seen.add(best[k])) sum += best[k]
    return sum
}

private val p089Table = listOf(
    1000 to "M", 900 to "CM", 500 to "D", 400 to "CD", 100 to "C", 90 to "XC",
    50 to "L", 40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I",
)

private val p089Value = mapOf(
    'I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000,
)

private fun p089Decode(r: String): Int {
    var total = 0
    for (i in r.indices) {
        val v = p089Value.getValue(r[i])
        total += if (i + 1 < r.length && v < p089Value.getValue(r[i + 1])) -v else v
    }
    return total
}

private fun p089Encode(n: Int): String {
    val sb = StringBuilder()
    var v = n
    for ((value, sym) in p089Table) while (v >= value) { sb.append(sym); v -= value }
    return sb.toString()
}

private fun solve089(): Long {
    val text = File(ContentIndex.resolveContentDir(), "problems/0089/roman.txt").readText()
    var saved = 0L
    for (line in text.lines()) {
        val r = line.trim()
        if (r.isEmpty()) continue
        saved += r.length - p089Encode(p089Decode(r)).length
    }
    return saved
}

private val p090Squares = listOf(
    0 to 1, 0 to 4, 0 to 9, 1 to 6, 2 to 5, 3 to 6, 4 to 9, 6 to 4, 8 to 1,
)

/** 掩码 mask 的骰子能否摆出数字 d（6/9 可翻转）。 */
private fun p090Has(mask: Int, d: Int): Boolean {
    if ((mask shr d) and 1 == 1) return true
    return (d == 6 || d == 9) && ((mask shr (15 - d)) and 1 == 1)
}

private fun p090Ok(a: Int, b: Int): Boolean {
    for ((x, y) in p090Squares) {
        if ((p090Has(a, x) && p090Has(b, y)) || (p090Has(a, y) && p090Has(b, x))) continue
        return false
    }
    return true
}

private fun solve090(): Long {
    val sets = (0 until 1024).filter { Integer.bitCount(it) == 6 }
    var count = 0L
    for (i in sets.indices) for (j in i until sets.size) if (p090Ok(sets[i], sets[j])) count++
    return count
}

private const val p091N = 50

private fun solve091(): Long {
    val n = p091N
    var total = n.toLong() * n
    var rightAtP = 0L
    for (x in 0..n) for (y in 0..n) {
        if (x == 0 && y == 0) continue
        val g = gcd(x.toLong(), y.toLong()).toInt()
        val wx = y / g
        val wy = -x / g
        for (j in -n..n) {
            if (j == 0) continue
            val qx = x + j * wx
            val qy = y + j * wy
            if (qx in 0..n && qy in 0..n) rightAtP++
        }
    }
    return total + rightAtP
}

/** PE 092 — 数位平方链：数位平方和只有 567 种取值，先用小记忆化定下归宿，再用数位 DP 按平方和计数，10^7 个数只需 7·567·10 次加法。答案 8581146。 */
private fun p092DigitSquareSum(n: Int): Int {
    var x = n
    var s = 0
    while (x > 0) {
        val d = x % 10
        s += d * d
        x /= 10
    }
    return s
}

private fun solve092(): Long {
    val digits = 7
    val maxSum = digits * 81

    val dest = IntArray(maxSum + 1)
    for (s in 1..maxSum) {
        var x = s
        while (x != 1 && x != 89) x = p092DigitSquareSum(x)
        dest[s] = x
    }

    var dp = LongArray(maxSum + 1)
    dp[0] = 1L
    repeat(digits) {
        val next = LongArray(maxSum + 1)
        for (s in 0..maxSum) {
            val c = dp[s]
            if (c == 0L) continue
            for (d in 0..9) next[s + d * d] += c
        }
        dp = next
    }

    var count = 0L
    for (s in 1..maxSum) if (dest[s] == 89) count += dp[s]
    return count
}

/** PE 093 — 算术表达式：对 C(10,4) 组数字用「两两合并」递归穷举全部表达式（精确分数），
 *  取能得到最长 1..n 连续正整数段的数字集合。答案 1258。 */
private data class p093Frac(val n: Long, val d: Long) {
    companion object {
        fun of(num: Long, den: Long): p093Frac {
            var a = num
            var b = den
            if (b < 0) {
                a = -a
                b = -b
            }
            val g = p093Gcd(a, b)
            return p093Frac(a / g, b / g)
        }
    }
}

private fun p093Gcd(a: Long, b: Long): Long {
    var x = if (a < 0) -a else a
    var y = if (b < 0) -b else b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return if (x == 0L) 1L else x
}

private fun p093AllValues(list: MutableList<p093Frac>, out: MutableSet<p093Frac>) {
    if (list.size == 1) {
        out.add(list[0])
        return
    }
    for (i in list.indices) {
        for (j in i + 1 until list.size) {
            val a = list[i]
            val b = list[j]
            val rest = ArrayList<p093Frac>(list.size - 2)
            for (k in list.indices) if (k != i && k != j) rest.add(list[k])
            val cands = LinkedHashSet<p093Frac>()
            cands.add(p093Frac.of(a.n * b.d + b.n * a.d, a.d * b.d))
            cands.add(p093Frac.of(a.n * b.d - b.n * a.d, a.d * b.d))
            cands.add(p093Frac.of(b.n * a.d - a.n * b.d, a.d * b.d))
            cands.add(p093Frac.of(a.n * b.n, a.d * b.d))
            if (b.n != 0L) cands.add(p093Frac.of(a.n * b.d, a.d * b.n))
            if (a.n != 0L) cands.add(p093Frac.of(b.n * a.d, b.d * a.n))
            for (c in cands) {
                rest.add(c)
                p093AllValues(rest, out)
                rest.removeAt(rest.size - 1)
            }
        }
    }
}

private fun solve093(): Long {
    var bestLen = -1
    var bestCode = 0L
    for (a in 0..9) {
        for (b in a + 1..9) {
            for (c in b + 1..9) {
                for (d in c + 1..9) {
                    val start = mutableListOf(
                        p093Frac.of(a.toLong(), 1L), p093Frac.of(b.toLong(), 1L),
                        p093Frac.of(c.toLong(), 1L), p093Frac.of(d.toLong(), 1L),
                    )
                    val values = HashSet<p093Frac>()
                    p093AllValues(start, values)
                    val ints = HashSet<Long>()
                    for (f in values) if (f.d == 1L && f.n > 0L) ints.add(f.n)
                    var k = 1L
                    while (ints.contains(k)) k++
                    val len = (k - 1L).toInt()
                    if (len > bestLen) {
                        bestLen = len
                        bestCode = a * 1000L + b * 100L + c * 10L + d
                    }
                }
            }
        }
    }
    return bestCode
}

/** PE 094 — 近乎等边三角形：面积整数 ⟺ (3n∓1)² − 3m² = 4，令 x 为周长，
 *  则全部解由 Pell 递推 x_{k+1} = 4x_k − x_{k−1}（2, 4 起）生成；累加 x ≤ 10^9 的合法周长。答案 518408346。 */
private fun p094Isqrt(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

private fun solve094(): Long {
    val limit = 1_000_000_000L
    var prev = 2L
    var cur = 4L
    var sum = 0L
    while (cur <= limit) {
        val x = cur
        val n = if (x % 3L == 1L) (x - 1) / 3 else (x + 1) / 3
        if (n >= 2) {
            val sq = if (x % 3L == 1L) 3 * n * n + 2 * n - 1 else 3 * n * n - 2 * n - 1
            val r = p094Isqrt(sq)
            if (r * r == sq) sum += x
        }
        val next = 4 * cur - prev
        prev = cur
        cur = next
    }
    return sum
}

/** PE 095 — 亲和数链：倍率筛求 1..10^6 的真因数和，再按起点顺序走链并用路径下标数组检测环，
 *  取最长环的最小元素。答案 14316。 */
private fun solve095(): Long {
    val limit = 1_000_000
    val sigma = IntArray(limit + 1)
    for (d in 1..limit / 2) {
        var m = d + d
        while (m <= limit) {
            sigma[m] += d
            m += d
        }
    }

    val pos = IntArray(limit + 1) { -1 }
    var bestLen = 0
    var bestMin = 0
    for (start in 1..limit) {
        if (pos[start] != -1) continue
        val path = ArrayList<Int>()
        var cur = start
        while (true) {
            if (cur < 1 || cur > limit) break
            if (pos[cur] == -2) break
            if (pos[cur] >= 0) {
                val idx = pos[cur]
                val cycleLen = path.size - idx
                var mn = Int.MAX_VALUE
                for (i in idx until path.size) if (path[i] < mn) mn = path[i]
                if (cycleLen > bestLen || (cycleLen == bestLen && mn < bestMin)) {
                    bestLen = cycleLen
                    bestMin = mn
                }
                break
            }
            pos[cur] = path.size
            path.add(cur)
            cur = sigma[cur]
        }
        for (v in path) pos[v] = -2
    }
    return bestMin.toLong()
}

private fun p096BoxOf(idx: Int): Int = (idx / 27) * 3 + (idx % 9) / 3

private fun p096Search(g: IntArray, rows: IntArray, cols: IntArray, boxes: IntArray): Boolean {
    var best = -1
    var bestUsed = 0
    var bestCount = 10
    for (i in 0 until 81) {
        if (g[i] != 0) continue
        val used = rows[i / 9] or cols[i % 9] or boxes[p096BoxOf(i)]
        var cnt = 0
        for (d in 1..9) if (used and (1 shl d) == 0) cnt++
        if (cnt == 0) return false
        if (cnt < bestCount) {
            bestCount = cnt
            best = i
            bestUsed = used
            if (cnt == 1) break
        }
    }
    if (best < 0) return true
    val r = best / 9
    val c = best % 9
    val b = p096BoxOf(best)
    for (d in 1..9) {
        val bit = 1 shl d
        if (bestUsed and bit != 0) continue
        g[best] = d
        rows[r] = rows[r] or bit
        cols[c] = cols[c] or bit
        boxes[b] = boxes[b] or bit
        if (p096Search(g, rows, cols, boxes)) return true
        g[best] = 0
        rows[r] = rows[r] and bit.inv()
        cols[c] = cols[c] and bit.inv()
        boxes[b] = boxes[b] and bit.inv()
    }
    return false
}

private fun solve096(): Long {
    val text = java.io.File(
        dev.pekt.content.ContentIndex.resolveContentDir(),
        "problems/0096/sudoku.txt"
    ).readText()
    val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
    var sum = 0L
    var i = 0
    while (i < lines.size) {
        if (lines[i].startsWith("Grid")) {
            i++
            continue
        }
        val g = IntArray(81)
        for (r in 0 until 9) {
            val row = lines[i + r]
            for (c in 0 until 9) g[r * 9 + c] = row[c] - '0'
        }
        i += 9
        val rows = IntArray(9)
        val cols = IntArray(9)
        val boxes = IntArray(9)
        for (k in 0 until 81) {
            val d = g[k]
            if (d != 0) {
                val bit = 1 shl d
                rows[k / 9] = rows[k / 9] or bit
                cols[k % 9] = cols[k % 9] or bit
                boxes[p096BoxOf(k)] = boxes[p096BoxOf(k)] or bit
            }
        }
        if (!p096Search(g, rows, cols, boxes)) error("unsolvable grid")
        sum += g[0] * 100 + g[1] * 10 + g[2]
    }
    return sum
}

private fun solve097(): Long {
    val mod = java.math.BigInteger.TEN.pow(10)
    val power = java.math.BigInteger.TWO.modPow(java.math.BigInteger.valueOf(7830457L), mod)
    return power.multiply(java.math.BigInteger.valueOf(28433L))
        .add(java.math.BigInteger.ONE)
        .mod(mod)
        .toLong()
}

private fun p098Isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}

/** 十进制串的模式编码：每个位置 4 bit，取该字符首次出现的下标。 */
private fun p098Code(s: String): Long {
    val seen = IntArray(10)
    var next = 0
    var code = 0L
    for (ch in s) {
        val d = ch - '0'
        var idx = seen[d]
        if (idx == 0) {
            next++
            idx = next
            seen[d] = idx
        }
        code = (code shl 4) or (idx - 1).toLong()
    }
    return code
}

/** 大写单词的模式编码，与 [p098Code] 使用同一套「首次出现下标」规则。 */
private fun p098CodeWord(s: String): Long {
    val seen = IntArray(26)
    var next = 0
    var code = 0L
    for (ch in s) {
        val d = ch - 'A'
        var idx = seen[d]
        if (idx == 0) {
            next++
            idx = next
            seen[d] = idx
        }
        code = (code shl 4) or (idx - 1).toLong()
    }
    return code
}

private fun p098Contains(a: LongArray, v: Long): Boolean {
    var lo = 0
    var hi = a.size - 1
    while (lo <= hi) {
        val mid = (lo + hi) ushr 1
        val x = a[mid]
        when {
            x < v -> lo = mid + 1
            x > v -> hi = mid - 1
            else -> return true
        }
    }
    return false
}

private fun solve098(): Long {
    val text = java.io.File(
        dev.pekt.content.ContentIndex.resolveContentDir(),
        "problems/0098/words.txt"
    ).readText()
    val words = text.split(',').map { it.trim().trim('"') }.filter { it.isNotEmpty() }

    val groups = LinkedHashMap<String, MutableList<String>>()
    for (w in words) groups.getOrPut(w.toCharArray().sorted().joinToString("")) { ArrayList() }.add(w)

    val wanted = sortedSetOf<Int>()
    for (g in groups.values) if (g.size > 1) for (w in g) wanted.add(w.length)
    if (wanted.isEmpty()) return 0L

    val squaresByLen = HashMap<Int, LongArray>()
    val codesByLen = HashMap<Int, LongArray>()
    for (len in wanted) {
        var low = 1L
        repeat(len - 1) { low *= 10 }
        val firstRoot = p098Isqrt(low)
        val n0 = if (firstRoot * firstRoot < low) firstRoot + 1 else firstRoot
        val n1 = p098Isqrt(low * 10 - 1)
        val count = (n1 - n0 + 1).toInt()
        val squares = LongArray(count)
        val codes = LongArray(count)
        for (k in 0 until count) {
            val v = (n0 + k) * (n0 + k)
            squares[k] = v
            codes[k] = p098Code(v.toString())
        }
        squaresByLen[len] = squares
        codesByLen[len] = codes
    }

    var best = 0L
    for (g in groups.values) {
        if (g.size < 2) continue
        for (i in g.indices) for (j in i + 1 until g.size) {
            val w1 = g[i]
            val w2 = g[j]
            if (w1 == w2) continue
            val squares = squaresByLen[w1.length] ?: continue
            val codes = codesByLen.getValue(w1.length)
            val target = p098CodeWord(w1)
            for (k in squares.indices) {
                if (codes[k] != target) continue
                val s1 = squares[k].toString()
                val map = HashMap<Char, Char>()
                for (p in w1.indices) map[w1[p]] = s1[p]
                val t = StringBuilder(w2.length)
                for (ch in w2) t.append(map[ch] ?: ' ')
                if (t[0] == '0') continue
                val v = t.toString().toLongOrNull() ?: continue
                if (!p098Contains(squares, v)) continue
                if (squares[k] > best) best = squares[k]
                if (v > best) best = v
            }
        }
    }
    return best
}

private const val P099_REL = 1e-12

private fun p099ExactGreater(b1: Long, e1: Long, b2: Long, e2: Long): Boolean {
    if (b1 == b2) return e1 > e2
    val x = java.math.BigInteger.valueOf(b1).pow(e1.toInt())
    val y = java.math.BigInteger.valueOf(b2).pow(e2.toInt())
    return x > y
}

private fun solve099(): Long {
    val text = java.io.File(
        dev.pekt.content.ContentIndex.resolveContentDir(),
        "problems/0099/base_exp.txt"
    ).readText()
    val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
    val n = lines.size
    val bases = LongArray(n)
    val exps = LongArray(n)
    val values = DoubleArray(n)
    val errors = DoubleArray(n)
    var best = -1
    for ((i, line) in lines.withIndex()) {
        val parts = line.split(',')
        val b = parts[0].trim().toLong()
        val e = parts[1].trim().toLong()
        bases[i] = b
        exps[i] = e
        val v = e.toDouble() * Math.log(b.toDouble())
        values[i] = v
        errors[i] = Math.abs(v) * P099_REL + P099_REL
        if (best < 0 || v > values[best]) best = i
    }

    val floorValue = values[best] - errors[best]
    var winner = best
    for (i in 0 until n) {
        if (i == best) continue
        if (values[i] + errors[i] < floorValue) continue
        if (p099ExactGreater(bases[i], exps[i], bases[winner], exps[winner])) winner = i
    }
    return (winner + 1).toLong()
}

private fun solve100(): Long {
    val limit = 1_000_000_000_000L
    var x = 1L
    var y = 1L
    while ((x + 1) / 2 <= limit) {
        val nx = 3 * x + 4 * y
        val ny = 2 * x + 3 * y
        x = nx
        y = ny
    }
    return (y + 1) / 2
}

/**
 * 差分三角右斜边滚动：下标 i 是由前 i+1 项外推的第 i+2 项，即 OP(i+1, i+2)。
 * 等距节点下「保持最高阶差分不变」向前外推一格，等于沿次数 ≤ k−1 的插值多项式求值。
 */
private fun p101Predictions(terms: LongArray): LongArray {
    val edge = LongArray(terms.size)
    return LongArray(terms.size) { index ->
        var difference = terms[index]
        for (order in 0 until index) {
            val previous = edge[order]
            edge[order] = difference
            difference -= previous
        }
        edge[index] = difference
        edge.sum()
    }
}

/**
 * PE 101 — 最优多项式的 FIT 之和 = 37076114526（与 content/problems/0101/solution.kt 一致）。
 * u_n = 1 − n + n² − … + n¹⁰ 是十次的，故 k = 1..10 全部产生 FIT，k ≥ 11 起恢复真实项。
 */
private fun solve101(): Long {
    val terms = LongArray(12) { index ->
        val n = index + 1L
        var value = 1L
        repeat(10) { value = 1L - n * value }
        value
    }
    val predictions = p101Predictions(terms)
    check(predictions[10] == terms[11]) { "k = 11 起应恢复真实项：${predictions[10]} != ${terms[11]}" }
    return predictions.take(10).sum()
}

/**
 * PE 102 — 三角形内含原点：三角形是三个内侧半平面的交集，原点严格位于内部当且仅当
 * A×B、B×C、C×A 三个行列式全正或全负（取 0 表示原点落在边上，须排除）。
 * triangles.txt 经 ContentIndex 定位；坐标用 Long 乘法避免溢出。答案 228。
 */
private fun solve102(): Long {
    val lines = File(ContentIndex.resolveContentDir(), "problems/0102/triangles.txt")
        .readLines().filter { it.isNotBlank() }
    var count = 0L
    for (line in lines) {
        val t = line.split(',').map { it.trim().toLong() }
        require(t.size == 6 && t.all { it in -1000L..1000L }) { "无效三角形坐标：$line" }
        val ab = t[0] * t[3] - t[1] * t[2]
        val bc = t[2] * t[5] - t[3] * t[4]
        val ca = t[4] * t[1] - t[5] * t[0]
        if ((ab > 0 && bc > 0 && ca > 0) || (ab < 0 && bc < 0 && ca < 0)) count++
    }
    check(lines.size == 1000) { "应有 1000 个三角形，实际 ${lines.size}" }
    return count
}

/** 特殊和集的定义式判定（与 105 同一套定义）：枚举所有非空不相交子集对逐条比较。 */
private fun p103IsSpecialSumSet(values: IntArray): Boolean {
    val n = values.size
    val total = 1 shl n
    val sum = LongArray(total)
    val popcount = IntArray(total)
    for (mask in 1 until total) {
        val lowbit = mask and -mask
        val rest = mask xor lowbit
        sum[mask] = sum[rest] + values[lowbit.countTrailingZeroBits()]
        popcount[mask] = popcount[rest] + 1
    }
    for (b in 1 until total) for (c in b + 1 until total) {
        if (b and c != 0) continue                                  // 只比较不相交子集对
        when {
            popcount[b] == popcount[c] -> if (sum[b] == sum[c]) return false
            popcount[b] > popcount[c] -> if (sum[b] <= sum[c]) return false
            else -> if (sum[c] <= sum[b]) return false
        }
    }
    return true
}

/**
 * PE 103 — 剪枝搜索：在总和 ≤ [cap] 的严格递增 n 元组里找最小和的特殊和集。
 *
 * 每放一个元素 x 就增量维护「各势子集和」位图：含 x 的 s 元子集和 = 旧 (s−1) 元子集和 + x，
 * 与旧 s 元子集和撞车立即回溯（剪枝主力）；随后检查前缀内部的规则二、用未来元素下界估算的
 * 规则二，以及剩余位置填满后的总和下界。
 */
private class P103SpecialSetSearch(private val n: Int, private val cap: Int) {
    private val chosen = IntArray(n)
    private val subsetSumSeen = Array(n + 1) { BooleanArray(cap + 1) }   // 各势可达子集和
    private val sumsBySize = Array(n + 1) { IntArray(cap + 1) }          // 同上，可遍历形式
    private val sizeCount = IntArray(n + 1)
    private val undoLog = Array(n + 1) { IntArray(cap + 1) }
    private val undoCount = IntArray(n + 1)
    private val levelMark = Array(n + 1) { IntArray(n + 1) }
    private val prefix = LongArray(n + 1)
    private var bestSum = Long.MAX_VALUE
    private val bestSets = mutableListOf<List<Int>>()

    var visitedNodes = 0L
        private set

    init {
        subsetSumSeen[0][0] = true
        sumsBySize[0][0] = 0
        sizeCount[0] = 1
    }

    private fun unwind(size: Int, keep: Int) {
        while (undoCount[size] > keep) {
            val value = undoLog[size][--undoCount[size]]
            subsetSumSeen[size][value] = false
            sizeCount[size]--
        }
    }

    private fun undoLevel(level: Int) { for (size in 1..n) unwind(size, levelMark[level][size]) }

    /** 尝试把 x 放在第 level 位；0 = 成功，1 = 冲突（换更大的 x 还有机会），2 = 单调失败（更大的 x 只会更糟）。 */
    private fun tryPlace(level: Int, x: Int): Int {
        for (size in 1..n) levelMark[level][size] = undoCount[size]
        for (size in level + 1 downTo 1) {                       // 含 x 的子集：旧 (s−1) 元子集和 + x
            val limit = sizeCount[size - 1]
            val source = sumsBySize[size - 1]
            for (t in 0 until limit) {
                val value = source[t] + x
                if (value > cap || subsetSumSeen[size][value]) {
                    undoLevel(level)
                    return 1
                }
                subsetSumSeen[size][value] = true
                sumsBySize[size][sizeCount[size]] = value
                sizeCount[size]++
                undoLog[size][undoCount[size]++] = value
            }
        }
        chosen[level] = x
        prefix[level + 1] = prefix[level] + x
        for (k in 1..level) {                                    // 前缀内部的规则二
            if (prefix[k + 1] <= prefix[level + 1] - prefix[level + 1 - k]) {
                undoLevel(level)
                return 2
            }
        }
        for (k in 1..minOf(level, n - 1)) {                       // 右端涉及未来元素：取下界
            var minTop = 0L
            for (p in n - k until n) minTop += if (p <= level) chosen[p].toLong() else (chosen[level] + (p - level)).toLong()
            if (prefix[k + 1] <= minTop) {
                undoLevel(level)
                return 2
            }
        }
        var minTotal = prefix[level + 1]                           // 总和下界
        for (p in level + 1 until n) minTotal += chosen[level] + (p - level)
        if (minTotal > cap) {
            undoLevel(level)
            return 2
        }
        return 0
    }

    private fun search(level: Int) {
        visitedNodes++
        if (level == n) {
            val total = prefix[n]
            if (total < bestSum) { bestSum = total; bestSets.clear() }
            if (total == bestSum) bestSets.add(chosen.toList())
            return
        }
        var x = if (level == 0) 1 else chosen[level - 1] + 1
        val limit = cap - (n - 1 - level)                           // 给剩余位置留出递增空间
        while (x <= limit) {
            when (tryPlace(level, x)) {
                0 -> { search(level + 1); undoLevel(level) }
                1 -> Unit                                          // 冲突与 x 的大小无关，继续试
                else -> return                                      // 单调失败：更大的 x 只会更差
            }
            x++
        }
    }

    fun run(): Pair<Long, List<List<Int>>> { search(0); return bestSum to bestSets }
}

/**
 * PE 103 — n = 7 的最优特殊和集（串成 20313839404245）。
 * 先用题面给出的 n ≤ 6 最优集与递推候选做锚点复算，再证明 cap = 254 无解、cap = 255 唯一解。
 */
private fun solve103(): Long {
    val expected = linkedMapOf(
        1 to (1L to listOf(1)),
        2 to (3L to listOf(1, 2)),
        3 to (9L to listOf(2, 3, 4)),
        4 to (21L to listOf(3, 5, 6, 7)),
        5 to (51L to listOf(6, 9, 11, 12, 13)),
        6 to (115L to listOf(11, 18, 19, 20, 22, 25)),
    )
    for ((size, want) in expected) {                                // 题面给出的前五个最优集 + n = 6 最优集
        val (sum, sets) = P103SpecialSetSearch(size, want.first.toInt()).run()
        check(sum == want.first && sets.size == 1 && sets[0] == want.second) { "n = $size 复算失败：$sum $sets" }
        check(p103IsSpecialSumSet(sets[0].toIntArray())) { "n = $size 的搜索结果不是特殊和集" }
    }
    // 题面提到的「递推规则」候选：n = 5 的中间元素 11 加到各行得到 {11,17,20,22,23,24}，和 117
    val ruleCandidate = intArrayOf(11, 17, 20, 22, 23, 24)
    check(p103IsSpecialSumSet(ruleCandidate) && ruleCandidate.sum() == 117) { "n = 6 递推候选判定失败" }
    check(P103SpecialSetSearch(6, 116).run().first == 115L) { "n = 6 在 cap = 116 内的最小和应是 115（优于递推候选 117）" }
    check(P103SpecialSetSearch(7, 254).run().second.isEmpty()) { "cap = 254 时不应存在特殊和集" }
    val (_, optimum) = P103SpecialSetSearch(7, 255).run()
    check(optimum.size == 1 && p103IsSpecialSumSet(optimum[0].toIntArray())) { "n = 7 最优集不唯一或非特殊和集" }
    return optimum[0].joinToString("").toLong()
}

/** 末九位是否为 1–9 全数字（除法拆位 + 位掩码，不用字符串、不数位数）。 */
private fun p104Pandigital(value: Long): Boolean {
    if (value !in 100_000_000L..999_999_999L) return false
    var rest = value
    var mask = 0
    repeat(9) {
        val digit = (rest % 10).toInt()
        val bit = 1 shl digit
        if (digit == 0 || mask and bit != 0) return false
        mask = mask or bit
        rest /= 10
    }
    return mask == 1022
}

/** Binet 公式给出的第 k 个斐波那契数的前九位。 */
private fun p104LeadingNine(k: Int): Long {
    val x = k * Math.log10((1 + Math.sqrt(5.0)) / 2) - Math.log10(5.0) / 2
    return Math.pow(10.0, x - Math.floor(x) + 8).toLong()
}

/** PE 104 — 末九位用模 10^9 的递推精确维护，只对末九位全数字的候选算前九位。答案 329468。 */
private fun solve104(): Long {
    check(!p104Pandigital(12345678L) && p104Pandigital(123456789L) && !p104Pandigital(123456788L))
    check(p104Pandigital(p104LeadingNine(2749)))
    var previous = 1L
    var current = 1L
    var k = 2
    while (true) {
        val next = (previous + current) % 1_000_000_000L
        previous = current
        current = next
        k++
        if (p104Pandigital(current) && p104Pandigital(p104LeadingNine(k))) return k.toLong()
    }
}

/** 规则二：对每个 k，最小 (k+1) 元子集和 > 最大 k 元子集和（升序集合上等价于全部情形）。 */
private fun p105SatisfiesSizeRule(sorted: IntArray): Boolean {
    val n = sorted.size
    val prefix = LongArray(n + 1)
    for (i in 1..n) prefix[i] = prefix[i - 1] + sorted[i - 1]
    for (k in 1 until n) {
        val smallestPlusOne = prefix[k + 1]                       // a₁+…+a_{k+1}
        val largestK = prefix[n] - prefix[n - k]                  // a_{n−k+1}+…+a_n
        if (smallestPlusOne <= largestK) return false
    }
    return true
}

/** 规则一：同一势的所有子集和互不相同（位图按势分桶，见即失败）。 */
private fun p105SatisfiesDistinctSumRule(sorted: IntArray): Boolean {
    val n = sorted.size
    val total = 1 shl n
    val sum = LongArray(total)
    val popcount = IntArray(total)
    val seen = Array(n + 1) { BooleanArray(sorted.sum() + 1) }
    for (mask in 1 until total) {
        val lowbit = mask and -mask
        val index = lowbit.countTrailingZeroBits()
        val rest = mask xor lowbit
        sum[mask] = sum[rest] + sorted[index]
        popcount[mask] = popcount[rest] + 1
        val size = popcount[mask]
        val value = sum[mask].toInt()
        if (seen[size][value]) return false
        seen[size][value] = true
    }
    return true
}

private fun p105IsSpecialSumSet(values: IntArray): Boolean {
    val sorted = values.sortedArray()
    if (sorted.distinct().size != sorted.size) return false       // 集合要求元素互异
    return p105SatisfiesSizeRule(sorted) && p105SatisfiesDistinctSumRule(sorted)
}

/** PE 105 — 逐行判定并累加所有特殊和集之和 = 73702。sets.txt 经 ContentIndex 定位。 */
private fun solve105(): Long {
    // 题面给出的两个锚点：第一个不满足规则一，第二个满足两规则且 S(A) = 1286
    val notSpecial = intArrayOf(81, 88, 75, 42, 87, 84, 86, 65)
    val special = intArrayOf(157, 150, 164, 119, 79, 159, 161, 139, 158)
    check(!p105IsSpecialSumSet(notSpecial)) { "反例被判为特殊集" }
    check(p105IsSpecialSumSet(special) && special.sum() == 1286) { "题面正例判定失败" }
    return File(ContentIndex.resolveContentDir(), "problems/0105/sets.txt").readLines()
        .filter { it.isNotBlank() }
        .map { line -> line.split(',').map(String::trim).map(String::toInt).toIntArray() }
        .filter { p105IsSpecialSumSet(it) }
        .sumOf { it.sum().toLong() }
}

/** 第 k 个 Catalan 数 C_k = (1/(k+1))·C(2k,k)（组合数用 dev.pekt.math.binomial）。 */
private fun p106Catalan(k: Int): Long = binomial(2 * k, k).toLong() / (k + 1L)

/** 从 n 元严格递增集中取出的「真正需要测相等」的等势不相交子集对数。 */
private fun p106PairsNeedingTest(n: Int): Long =
    (1..n / 2).sumOf { k ->
        val union = binomial(n, 2 * k).toLong()                   // 选并集（2k 个位置）
        val splits = binomial(2 * k, k).toLong() / 2              // 无序拆成两个 k 元子集
        union * (splits - p106Catalan(k))                         // 减去被大小关系直接判定的（Dyson）拆法
    }

/** PE 106 — Σ C(n,2k)·(C(2k,k)/2 − Catalan_k)（n = 12）= 21384。O(n) 项求和。 */
private fun solve106(): Long {
    check(p106PairsNeedingTest(4) == 1L && p106PairsNeedingTest(7) == 70L) { "题面锚点不符" }
    return p106PairsNeedingTest(12)
}

/** 并查集：parent[x] = 父节点（根为自身），rank 为按秩合并的权重。 */
private class P107UnionFind(size: Int) {
    private val parent = IntArray(size) { it }
    private val rank = IntArray(size)

    fun find(x: Int): Int {
        var root = x
        while (parent[root] != root) root = parent[root]
        // 路径压缩
        var cur = x
        while (cur != root) { val next = parent[cur]; parent[cur] = root; cur = next }
        return root
    }

    /** 合并成功返回 true，二者本就连通返回 false。 */
    fun union(a: Int, b: Int): Boolean {
        var ra = find(a); var rb = find(b)
        if (ra == rb) return false
        if (rank[ra] < rank[rb]) { val t = ra; ra = rb; rb = t }
        parent[rb] = ra
        if (rank[ra] == rank[rb]) rank[ra]++
        return true
    }
}

/** 从邻接矩阵文本读入 (u, v, weight) 边列表（只取 i < j 的上三角，`-` 视为无边）。 */
private fun p107LoadEdges(text: String): List<Triple<Int, Int, Int>> {
    val edges = mutableListOf<Triple<Int, Int, Int>>()
    text.lines().forEachIndexed { i, line ->
        line.split(',').forEachIndexed { j, cell ->
            val w = cell.trim().toIntOrNull() ?: return@forEachIndexed
            if (i < j) edges.add(Triple(i, j, w))
        }
    }
    return edges
}

/** PE 107 — Kruskal 求最小生成树，节省 = 原总权重 − MST 权重 = 259679。network.txt 经 ContentIndex 定位。 */
private fun solve107(): Long {
    // 题面 7 顶点样例的自检：总权重 243，MST 权重 93，节省 150
    val sampleEdges = p107LoadEdges(
        listOf(
            "-,16,12,21,-,-,-",
            "16,-,-,17,20,-,-",
            "12,-,-,28,-,31,-",
            "21,17,28,-,18,19,23",
            "-,20,-,18,-,-,11",
            "-,-,31,19,-,-,27",
            "-,-,-,23,11,27,-",
        ).joinToString("\n") + "\n"
    )
    val sampleTotal = sampleEdges.sumOf { it.third.toLong() }
    val sampleUf = P107UnionFind(7)
    var sampleMst = 0L
    for ((u, v, w) in sampleEdges.sortedBy { it.third }) if (sampleUf.union(u, v)) sampleMst += w
    check(sampleTotal == 243L && sampleMst == 93L) { "样例自检失败：total=$sampleTotal, mst=$sampleMst" }

    val edges = p107LoadEdges(File(ContentIndex.resolveContentDir(), "problems/0107/network.txt").readText())
    check(edges.isNotEmpty()) { "network.txt 没有有效边" }
    val vertices = edges.flatMap { listOf(it.first, it.second) }.max() + 1
    val total = edges.sumOf { it.third.toLong() }
    val uf = P107UnionFind(vertices)
    var mstWeight = 0L
    var picked = 0
    for ((u, v, w) in edges.sortedBy { it.third }) {
        if (uf.union(u, v)) {
            mstWeight += w
            if (++picked == vertices - 1) break
        }
    }
    check(picked == vertices - 1) { "图不连通：只选出 $picked / ${vertices - 1} 条边" }
    return total - mstWeight
}

/** 108/110 共用的素数池：指数按非递增顺序落在最小的这些素数上。 */
private val p108FirstPrimes = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53)

/**
 * 在 ∏(2eᵢ+1) ≥ [target] 的约束下求 n = ∏pᵢ^{eᵢ} 的最小值（108/110 同一套搜索）。
 * 贪心初值 = 前 k 个素数各取一次，随后按指数非递增枚举 + 「前缀 n ≥ 已知最优」剪枝。
 */
private fun searchMinimalInverseCount(target: Long): Long {
    var best = 1L
    var divisors = 1L
    var index = 0
    while (divisors < target) {
        best *= p108FirstPrimes[index]
        divisors *= 3L
        index++
    }

    fun search(index: Int, maxExponent: Int, n: Long, divisorCount: Long) {
        if (n >= best) return
        if (divisorCount >= target) {
            best = n
            return
        }
        if (index >= p108FirstPrimes.size) return
        val p = p108FirstPrimes[index]
        var value = n
        for (exponent in 1..maxExponent) {
            if (value > best / p) return     // 再乘一次必然 ≥ best（顺带保证不溢出）
            value *= p
            search(index + 1, exponent, value, divisorCount * (2L * exponent + 1))
        }
    }

    search(0, 62, 1L, 1L)
    return best
}

/** PE 108 — 解个数 > 1000 ⟺ d(n²) ≥ 2001 的最小 n = 180180。 */
private fun solve108(): Long = searchMinimalInverseCount(2_001L)

/** PE 109 的单镖分值上界：最大区域是三倍 20。 */
private const val P109_BOARD_MAX = 60

/** 62 个计分区域的分值：先三档倍率 × 1..20，再外/内牛眼。 */
private fun p109DartValues(): IntArray {
    val values = IntArray(62)
    var index = 0
    for (multiplier in 1..3) {
        for (number in 1..20) values[index++] = number * multiplier
    }
    values[index++] = 25
    values[index] = 50
    return values
}

/** 21 个可作末镖的双倍区域：D1..D20 与内牛眼 D25。 */
private fun p109DoubleValues(): IntArray {
    val values = IntArray(21)
    for (number in 1..20) values[number - 1] = number * 2
    values[20] = 50
    return values
}

/** 得分严格小于 [limit] 的 checkout 方式数（前两镖按无序多重集计，末镖必须是双倍）。 */
private fun p109CountCheckouts(limit: Int): Long {
    val darts = p109DartValues()
    val doubles = p109DoubleValues()
    val counts = IntArray(P109_BOARD_MAX + 1)
    for (value in darts) counts[value]++
    var total = 0L
    for (d in doubles) {
        if (d >= limit) continue
        total++                                             // 1 镖：末镖即 d
        for (x in darts) if (x + d < limit) total++          // 2 镖：首镖可为任意区域
        for (v in 1..P109_BOARD_MAX) {                       // 3 镖：前两镖的多重集
            if (counts[v] == 0) continue
            val rest = limit - d - v                         // 第二镖的值 w 须满足 w < rest
            for (w in v until minOf(rest, P109_BOARD_MAX + 1)) {
                if (counts[w] == 0) continue
                total += if (w == v) {
                    counts[v].toLong() * (counts[v] + 1) / 2     // 同值区域可重复取
                } else {
                    counts[v].toLong() * counts[w]
                }
            }
        }
    }
    return total
}

/** PE 109 — 得分小于 100 的 checkout 数 = 38182。 */
private fun solve109(): Long {
    val exactly6 = p109CountCheckouts(7) - p109CountCheckouts(6)
    check(exactly6 == 11L) { "题面锚点：得分 6 应有 11 种 checkout，实际 $exactly6" }
    val allCheckouts = p109CountCheckouts(171)
    check(allCheckouts == 42336L) { "题面锚点：checkout 总数应为 42336，实际 $allCheckouts" }
    return p109CountCheckouts(100)
}

/** PE 110 — 解个数 > 4×10⁶ ⟺ d(n²) ≥ 8 000 001 的最小 n = 9350130049860600。 */
private fun solve110(): Long = searchMinimalInverseCount(8_000_001L)

/** 末位只可能是 1/3/7/9（n ≥ 2 的素数），下标即数字。 */
private val p111PrimeLastDigits =
    booleanArrayOf(false, true, false, true, false, false, false, true, false, true)

/** 确定性 Miller–Rabin 底数：对一切 n < 3.4×10¹⁴ 无错判。 */
private val p111MillerRabinBases = longArrayOf(2, 3, 5, 7, 11, 13, 17)

/** 用 BigInteger 模幂的确定性 Miller–Rabin，覆盖 10¹⁰ 量级的候选（工具库的试除法在此太慢）。 */
private fun p111IsPrime(candidate: Long): Boolean {
    if (candidate < 2L) return false
    for (base in p111MillerRabinBases) {
        if (candidate == base) return true
        if (candidate % base == 0L) return false
    }
    var oddPart = candidate - 1L
    var powers = 0
    while (oddPart % 2L == 0L) {
        oddPart /= 2L
        powers++
    }
    val modulus = BigInteger.valueOf(candidate)
    val one = BigInteger.ONE
    val minusOne = modulus - one
    for (base in p111MillerRabinBases) {
        if (base >= candidate) continue
        var power = BigInteger.valueOf(base).modPow(BigInteger.valueOf(oddPart), modulus)
        if (power == one || power == minusOne) continue
        var witnessed = false
        var round = 1
        while (round < powers && !witnessed) {
            power = power * power % modulus
            if (power == minusOne) witnessed = true
            round++
        }
        if (!witnessed) return false
    }
    return true
}

/**
 * 递归生成所有「恰好含 dNeeded 个数字 d」的 n 位数，素数计入 tally[0]（个数）/tally[1]（和）。
 * sumMod3 是已放置数位之和对 3 的余数，用于剔除必然被 3 整除的候选。
 */
private fun p111PlaceDigits(
    position: Int,
    n: Int,
    d: Int,
    dNeeded: Int,
    value: Long,
    sumMod3: Int,
    tally: LongArray,
) {
    val slotsLeft = n - position - 1
    for (digit in 0..9) {
        if (position == 0 && digit == 0) continue
        if (slotsLeft == 0 && !p111PrimeLastDigits[digit]) continue
        val remainingD = if (digit == d) dNeeded - 1 else dNeeded
        if (remainingD < 0 || remainingD > slotsLeft) continue
        val nextValue = value * 10L + digit
        val nextMod3 = (sumMod3 + digit) % 3
        if (slotsLeft > 0) {
            p111PlaceDigits(position + 1, n, d, remainingD, nextValue, nextMod3, tally)
        } else if (nextMod3 != 0 && p111IsPrime(nextValue)) {
            tally[0]++
            tally[1] += nextValue
        }
    }
}

/** 返回 [M(n, d), N(n, d), S(n, d)]：自 m = n 下降，取第一个有素数的层。 */
private fun p111StatsFor(n: Int, d: Int): LongArray {
    for (m in n downTo 1) {
        val tally = LongArray(2)
        p111PlaceDigits(0, n, d, m, 0L, 0, tally)
        if (tally[0] > 0L) return longArrayOf(m.toLong(), tally[0], tally[1])
    }
    return longArrayOf(0L, 0L, 0L)
}

/** PE 111 — Σ_d S(10, d) = 612407567715（先用题面四位素数表自检）。 */
private fun solve111(): Long {
    val expectedM = longArrayOf(2, 3, 3, 3, 3, 3, 3, 3, 3, 3)
    val expectedN = longArrayOf(13, 9, 1, 12, 2, 1, 1, 9, 1, 7)
    val expectedS = longArrayOf(67061, 22275, 2221, 46214, 8888, 5557, 6661, 57863, 8887, 48073)
    var sampleTotal = 0L
    for (d in 0..9) {
        val stats = p111StatsFor(4, d)
        check(stats[0] == expectedM[d] && stats[1] == expectedN[d] && stats[2] == expectedS[d]) {
            "d = $d 的四位素数表不符：实际 M=${stats[0]} N=${stats[1]} S=${stats[2]}"
        }
        sampleTotal += stats[2]
    }
    check(sampleTotal == 273700L) { "四位总和应为 273700，实际 $sampleTotal" }
    return (0..9).sumOf { p111StatsFor(10, it)[2] }
}

/**
 * PE 112 — 首个弹跳数占比为 [percent]% 的 n。
 * 前缀一旦同时出现上升与下降，其后缀全为弹跳数，可整块跳过；只在
 * (100−p)·n = 100·C 且 n 落在当前块内时返回，比例比较全用 Long 整数。
 */
private fun p112FirstBouncyProportion(percent: Int): Long {
    require(percent in 1..99)
    var nonBouncy = 0L
    val powers = LongArray(18) { 1L }
    for (i in 1 until powers.size) powers[i] = powers[i - 1] * 10L

    fun visit(prefix: Long, last: Int, remaining: Int, up: Boolean, down: Boolean): Long {
        if (up && down) {
            val lower = prefix * powers[remaining]
            val upper = lower + powers[remaining] - 1
            val numerator = 100L * nonBouncy
            val denominator = 100 - percent
            val candidate = numerator / denominator
            return if (numerator % denominator == 0L && candidate in lower..upper) candidate else 0L
        }
        if (remaining == 0) {
            nonBouncy++
            return 0L
        }
        for (digit in 0..9) {
            val answer = visit(prefix * 10 + digit, digit, remaining - 1, up || digit > last, down || digit < last)
            if (answer != 0L) return answer
        }
        return 0L
    }

    for (digits in 1..18) {
        for (first in 1..9) {
            val answer = visit(first.toLong(), first, digits - 1, false, false)
            if (answer != 0L) return answer
        }
    }
    error("在支持的 18 位整数范围内未找到目标比例")
}

/** PE 112 — 弹跳数占比首次达到 99% 的 n = 1587000。 */
private fun solve112(): Long {
    check(p112FirstBouncyProportion(50) == 538L)
    check(p112FirstBouncyProportion(90) == 21780L)
    return p112FirstBouncyProportion(99)
}

/** 小于 10^digits 的非弹跳正整数个数 = C(D+9,9) + C(D+10,10) − 10D − 2。 */
private fun p113CountNonBouncy(digits: Int): Long {
    require(digits in 1..100)
    return binomial(digits + 9, 9).toLong() + binomial(digits + 10, 10).toLong() - 10L * digits - 2L
}

/** PE 113 — 小于 10^100 的非弹跳数个数 = 51161058134250。 */
private fun solve113(): Long {
    check(p113CountNonBouncy(1) == 9L)
    check(p113CountNonBouncy(6) == 12951L)
    check(p113CountNonBouncy(10) == 277032L)
    return p113CountNonBouncy(100)
}

/**
 * 长度为 [length] 的一行、红块最短 [minLength] 时的填法数 f(length)（114 与 115 共用）。
 * f(n) = 2f(n−1) − f(n−2) + f(n−minLength−1)（n > minLength），f(0) = 1、
 * n < minLength 时只有全灰一种、n == minLength 再多一种整行红块。
 */
private fun p114FillCount(length: Int, minLength: Int): Long {
    val f = LongArray(length + 1)
    f[0] = 1
    for (n in 1..length) {
        f[n] = when {
            n < minLength -> 1L                                   // 只有全灰
            n == minLength -> 2L                                  // 全灰，或一块红填满整行
            else -> 2 * f[n - 1] - f[n - 2] + f[n - minLength - 1]
        }
    }
    return f[length]
}

/** PE 114 — 长度 50、红块最短 3 的填法数 = 16475640049。 */
private fun solve114(): Long {
    // 题面样例锚点：长度 7 有 17 种填法；长度 8 可混用块长
    check(p114FillCount(7, 3) == 17L) { "题面样例（7, 3 → 17）不符：${p114FillCount(7, 3)}" }
    check(p114FillCount(8, 3) == 27L) { "长度 8 的填法数不符：${p114FillCount(8, 3)}" }
    return p114FillCount(50, 3)
}

/**
 * 使 F(minLength, n) > [threshold] 的最小 n：沿 114 的递推单向递增扫描，
 * 第一次越过阈值的 n 就是最小解；表长未知，按容量翻倍兜住（F 指数增长，实际一次就够）。
 */
private fun p115LeastRowLength(minLength: Int, threshold: Long): Int {
    require(minLength >= 1) { "最短块长必须为正" }
    var capacity = 2 * minLength + 8
    while (true) {
        val f = LongArray(capacity)
        f[0] = 1                                        // 空行记一种
        for (n in 1 until capacity) {
            f[n] = when {
                n < minLength -> 1L                     // 放不下任何红块，只有全黑
                n == minLength -> 2L                    // 全黑，或一块红填满整行
                else -> 2 * f[n - 1] - f[n - 2] + f[n - minLength - 1]
            }
            if (f[n] > threshold) return n              // 递增扫描，首个越阈值即最小 n
        }
        capacity *= 2
    }
}

/** PE 115 — F(50, n) > 10⁶ 的最小 n = 168（题面 m = 3 → 30、m = 10 → 57 两个锚点自检）。 */
private fun solve115(): Long {
    check(p114FillCount(29, 3) == 673_135L && p114FillCount(30, 3) == 1_089_155L) {
        "m = 3 锚点不符：${p114FillCount(29, 3)} / ${p114FillCount(30, 3)}"
    }
    check(p115LeastRowLength(3, 1_000_000L) == 30) { "m = 3 的答案应为 30" }
    check(p114FillCount(56, 10) == 880_711L && p114FillCount(57, 10) == 1_148_904L) {
        "m = 10 锚点不符：${p114FillCount(56, 10)} / ${p114FillCount(57, 10)}"
    }
    check(p115LeastRowLength(10, 1_000_000L) == 57) { "m = 10 的答案应为 57" }
    return p115LeastRowLength(50, 1_000_000L).toLong()
}

/** 只用一种长度 tileLength 的彩色砖时，铺满长 rowLength 的一行且至少用一块彩色砖的方案数。 */
private fun p116WaysWithSingleColour(rowLength: Int, tileLength: Int): Long {
    val ways = LongArray(rowLength + 1)
    ways[0] = 1
    for (i in 1..rowLength) {
        var total = ways[i - 1]                                // 最右端是一块灰砖
        if (i >= tileLength) total += ways[i - tileLength]      // 最右端是一块彩色砖
        ways[i] = total
    }
    return ways[rowLength] - 1                                  // 去掉全灰方案
}

/** PE 116 — 红/绿/蓝三色各自独立计数（长 50）= 20492570929。 */
private fun solve116(): Long {
    val sample = (2..4).map { p116WaysWithSingleColour(5, it) }
    check(sample == listOf(7L, 3L, 2L)) { "题面长度 5 的样例不符：$sample" }
    return (2..4).sumOf { p116WaysWithSingleColour(50, it) }
}

/** 用长 1（灰）、2（红）、3（绿）、4（蓝）四种砖铺满长度 rowLength 的方案数（含全灰）。 */
private fun p117TilingWays(rowLength: Int): Long {
    val ways = LongArray(rowLength + 1)
    ways[0] = 1
    for (i in 1..rowLength) {
        var total = 0L
        for (length in 1..4) {
            if (i >= length) total += ways[i - length]
        }
        ways[i] = total
    }
    return ways[rowLength]
}

/** PE 117 — 长度 50 的混色铺法数（四那契数）= 100808458960497。 */
private fun solve117(): Long {
    // 题面样例锚点：长度 5 恰有 15 种铺法
    val sample = p117TilingWays(5)
    check(sample == 15L) { "题面长度 5 的样例不符：$sample" }
    return p117TilingWays(50)
}

/**
 * PE 118 — 枚举无重复数字的整数并用素数试除建表，再按数字掩码做记忆化计数
 * （每次只选含最小数字的块，消除块顺序重复）。答案 44680。
 */
private fun solve118(): Long {
    val limit = 31623
    val isP = sieve(limit)
    val primes = ArrayList<Int>()
    for (p in 2..limit) if (isP[p]) primes.add(p)
    fun isPrimeValue(value: Int): Boolean {
        if (value < 2) return false
        for (p in primes) {
            if (p > value / p) return true
            if (value % p == 0) return false
        }
        return true
    }
    val counts = IntArray(512)
    fun extend(value: Int, used: Int, sum: Int) {
        if (value < 10) {
            if (isPrimeValue(value)) counts[used]++
        } else if (value % 2 != 0 && value % 5 != 0 && sum % 3 != 0 && isPrimeValue(value)) {
            counts[used]++
        }
        for (digit in 1..9) {
            val bit = 1 shl (digit - 1)
            if (used and bit == 0) extend(value * 10 + digit, used or bit, sum + digit)
        }
    }
    extend(0, 0, 0)
    val memo = LongArray(512) { -1L }
    fun count(remaining: Int): Long {
        if (remaining == 0) return 1L
        if (memo[remaining] >= 0) return memo[remaining]
        val lowest = remaining and -remaining
        var total = 0L
        var block = remaining
        while (block != 0) {
            if (block and lowest != 0 && counts[block] != 0) {
                total += counts[block].toLong() * count(remaining xor block)
            }
            block = (block - 1) and remaining
        }
        memo[remaining] = total
        return total
    }
    check(count(7) == 2L)
    check(count(1 shl 2) == 1L)
    return count(511)
}

/** 十进制各位数字之和（整数除法与取模，不经过字符串、不数位数）。 */
private fun p119DigitSum(value: Long): Int {
    var rest = value
    var sum = 0
    while (rest > 0) {
        sum += (rest % 10L).toInt()
        rest /= 10L
    }
    return sum
}

/** limit 以内、形如 s^k（s ≥ 2、k ≥ 2）且数字和恰为 s 的全部数，升序去重。 */
private fun p119DigitPowerSums(limit: Long, maxBase: Int): List<Long> {
    val found = sortedSetOf<Long>()
    for (base in 2..maxBase) {
        var power = base.toLong() * base
        while (power < limit) {
            if (p119DigitSum(power) == base) found.add(power)
            power *= base
        }
    }
    return found.toList()
}

/** PE 119 — 第 30 项 a₃₀ = 63⁸ = 248155780267521（前 30 项与上界选取无关，做旁证）。 */
private fun solve119(): Long {
    val terms = p119DigitPowerSums(1_000_000_000_000_000L, 200)
    check(terms[1] == 512L) { "题面给出 a₂ = 512，实际 ${terms[1]}" }
    check(terms[9] == 614656L) { "题面给出 a₁₀ = 614656，实际 ${terms[9]}" }
    val tighter = p119DigitPowerSums(400_000_000_000_000L, 200)
    check(tighter.size >= 30 && tighter.take(30) == terms.take(30)) {
        "前 30 项应当与 limit 的选取无关"
    }
    val thirty = terms[29]
    var powerOfSixtyThree = 1L
    repeat(8) { powerOfSixtyThree *= 63L }
    check(p119DigitSum(thirty) == 63 && powerOfSixtyThree == thirty) {
        "末项旁证失败：a₃₀ = $thirty 应为 63⁸ 且数字和为 63"
    }
    return thirty
}

/** 闭式：a 为奇数时 r_max = a(a−1)，a 为偶数时 r_max = a(a−2)。 */
private fun p120MaxRemainder(a: Long): Long = if (a % 2L != 0L) a * (a - 1L) else a * (a - 2L)

/**
 * 直接枚举：对每个 n = 1..nBound 累乘出 (a−1)^n、(a+1)^n 模 a²，取余数最大值。
 * 只用于小范围的独立复核，不作为主算法。
 */
private fun p120DirectMaxRemainder(a: Long, nBound: Long): Long {
    val modulus = a * a
    var powerMinus = 1L
    var powerPlus = 1L
    var best = 0L
    for (n in 1..nBound) {
        powerMinus = powerMinus * ((a - 1L) % modulus) % modulus
        powerPlus = powerPlus * ((a + 1L) % modulus) % modulus
        val remainder = (powerMinus + powerPlus) % modulus
        if (remainder > best) best = remainder
    }
    return best
}

/** PE 120 — Σ_{a=3}^{1000} r_max(a) = 333082500（a = 3..60 用直接枚举复核闭式）。 */
private fun solve120(): Long {
    check(p120MaxRemainder(7L) == 42L) { "题面给出 a = 7 时 r_max = 42，实际 ${p120MaxRemainder(7L)}" }
    for (a in 3L..60L) {
        val direct = p120DirectMaxRemainder(a, 4L * a)
        check(direct == p120MaxRemainder(a)) { "a = $a：直接枚举 $direct ≠ 闭式 ${p120MaxRemainder(a)}" }
    }
    return (3L..1000L).sumOf { p120MaxRemainder(it) }
}

/** 获胜概率的精确分数 W/D，两者都是整数（分母为 (turns+1)!）。 */
private fun p121WinningFraction(turns: Int): Pair<Long, Long> {
    var denominator = 1L
    for (k in 1..turns) denominator *= (k + 1)                  // D = (turns+1)!
    var counts = LongArray(turns + 1)
    counts[0] = denominator                                      // 0 轮后 0 次蓝碟，概率 1
    for (turn in 1..turns) {
        val next = LongArray(turns + 1)
        for (blue in 0..turn) {
            var numerator = 0L
            if (blue <= turn - 1) numerator += counts[blue] * turn          // 本轮抽到红碟
            if (blue >= 1) numerator += counts[blue - 1]                     // 本轮抽到蓝碟
            next[blue] = numerator / (turn + 1)
        }
        counts = next
    }
    var winning = 0L
    for (blue in 0..turns) if (blue > turns - blue) winning += counts[blue]
    return winning to denominator
}

/** 游戏进行 turns 轮时，庄家应为单场游戏预留的最大奖金基金（整数英镑，含本金）。 */
private fun p121PrizeFund(turns: Int): Long {
    val (winning, denominator) = p121WinningFraction(turns)
    return denominator / winning
}

/** PE 121 — 15 轮游戏的奖金 = 2269（题面 4 轮 → 11/120、£10 的锚点自检）。 */
private fun solve121(): Long {
    val (numerator4, denominator4) = p121WinningFraction(4)
    check(120L * numerator4 == 11L * denominator4) {
        "题面锚点：4 轮获胜概率应为 11/120，实际 $numerator4/$denominator4"
    }
    check(p121PrizeFund(4) == 10L) { "题面锚点：4 轮奖金应为 £10，实际 ${p121PrizeFund(4)}" }
    return p121PrizeFund(15)
}

/** 计算 n^target 所需的最少乘法次数 m(target)：迭代加深 DFS 求最短加法链。 */
private fun p122MinMultiplications(target: Int): Int {
    if (target == 1) return 0
    val chain = IntArray(32)
    chain[0] = 1

    fun search(size: Int, limit: Int): Boolean {
        val last = chain[size - 1]
        if (last == target) return true
        val remaining = limit - (size - 1)                 // 还允许做几次乘法
        if (remaining <= 0) return false
        if (last.toLong() shl remaining < target) return false   // 每步至多翻倍
        for (i in size - 1 downTo 0) {
            // 即使下一步取 chain[i] + chain[i]，其后全翻倍也到不了 target 就可以收手
            if ((chain[i] + chain[i]).toLong() shl (remaining - 1) < target) break
            for (j in i downTo 0) {
                val candidate = chain[i] + chain[j]
                if (candidate <= last || candidate > target) continue
                if (candidate.toLong() shl (remaining - 1) < target) break
                chain[size] = candidate
                if (search(size + 1, limit)) return true
            }
        }
        return false
    }

    var limit = 1
    while (!search(1, limit)) limit++
    return limit
}

/**
 * PE 122 — Σ_{k=1}^{200} m(k)。m(15) = 5、m(191) = 11 两个锚点在题面/推导中给出，一并自检。
 */
private fun solve122(): Long {
    check(p122MinMultiplications(15) == 5) { "题面锚点：m(15) 应为 5，实际 ${p122MinMultiplications(15)}" }
    check(p122MinMultiplications(1) == 0 && p122MinMultiplications(2) == 1) { "小值锚点失败" }
    check(p122MinMultiplications(191) == 11) { "m(191) 应为 11，实际 ${p122MinMultiplications(191)}" }
    return (1..200).sumOf { p122MinMultiplications(it).toLong() }
}

/** 第 index 个素数为 prime 时的余数 r（index 从 1 开始）：偶数下标恒为 2，奇数下标为 2n·p mod p²。 */
private fun p123RemainderAt(index: Int, prime: Long): Long =
    if (index % 2 == 0) 2L else 2L * index * prime % (prime * prime)

/** 使余数首次超过 threshold 的最小下标；素数表不够长时抛错而不是给出错误答案。 */
private fun p123FirstIndexExceeding(threshold: Long): Long {
    val isP = sieve(300_000)
    var index = 0
    for (p in 2..300_000) {
        if (!isP[p]) continue
        index++
        if (p123RemainderAt(index, p.toLong()) > threshold) return index.toLong()
    }
    error("素数筛上界不足，无法覆盖阈值 $threshold")
}

/** PE 123 — 余数首次超过 10¹⁰ 的下标 = 21035（题面 n = 3 → 5、超过 10⁹ → 7037 两个锚点自检）。 */
private fun solve123(): Long {
    check(p123RemainderAt(3, 5L) == 5L) { "题面样例 n = 3 应得余数 5，实际 ${p123RemainderAt(3, 5L)}" }
    check(p123FirstIndexExceeding(1_000_000_000L) == 7037L) { "题面锚点：超过 10⁹ 的最小 n 应为 7037" }
    return p123FirstIndexExceeding(10_000_000_000L)
}

/** PE 124 — 根基排序：筛出 rad(n) 后按根基计数排序，取第 10000 项 = 21417。O(N log log N)。 */
private fun solve124(): Long {
    val limit = 100_000
    val radicals = IntArray(limit + 1) { 1 }
    for (p in 2..limit) {
        if (radicals[p] == 1) {
            for (multiple in p..limit step p) radicals[multiple] *= p
        }
    }
    val next = IntArray(limit + 1)
    for (n in 1..limit) next[radicals[n]]++
    var offset = 0
    for (r in 1..limit) {
        val count = next[r]
        next[r] = offset
        offset += count
    }
    val ordered = IntArray(limit)
    for (n in 1..limit) ordered[next[radicals[n]]++] = n
    return ordered[9_999].toLong()
}

/** 数字反转：12321 → 12321、120 → 21（不经过字符串）。 */
private fun p125ReverseDigits(value: Long): Long {
    var rest = value
    var reversed = 0L
    while (rest > 0) {
        reversed = reversed * 10 + rest % 10
        rest /= 10
    }
    return reversed
}

private fun p125IsPalindromic(value: Long): Boolean = value > 0 && p125ReverseDigits(value) == value

/**
 * 平方前缀和 P(0..maxBase)，P(k) = 1² + … + k²。上界取「单个平方数仍小于 limit」：
 * 区间和 P(b) − P(a−1) ≥ b²，所以 b² ≥ limit 的区间必然全部超限，可以安全截断。
 */
private fun p125SquarePrefixSums(limit: Long): LongArray {
    val sums = ArrayList<Long>(10_000)
    sums.add(0L)
    var base = 1L
    while (base * base < limit) {
        sums.add(sums[sums.size - 1] + base * base)
        base++
    }
    return sums.toLongArray()
}

/** 小于 limit 的「回文 + 至少 minTerms 项连续正整数平方和」之和（同一数只计一次）。 */
private fun p125Sum(limit: Long, minTerms: Int): Long {
    val prefix = p125SquarePrefixSums(limit)
    val palindromes = HashSet<Long>()
    var firstValid = 0                               // 最小的 start，使 P(end) − P(start) < limit
    for (end in 1 until prefix.size) {
        while (firstValid < end && prefix[end] - prefix[firstValid] >= limit) firstValid++
        for (start in firstValid until end) {        // 区间是 (start+1)² … end²，共 end−start 项
            if (end - start < minTerms) break        // 项数随 start 递增而减少 → 可以 break
            val sum = prefix[end] - prefix[start]    // start ≥ firstValid ⇒ sum 必 < limit
            if (p125IsPalindromic(sum)) palindromes.add(sum)
        }
    }
    return palindromes.sum()
}

/** PE 125 — 10⁸ 以内可写成连续平方和（至少两项）的回文数之和 = 2906969179。 */
private fun solve125(): Long {
    check(p125Sum(1000L, 2) == 4164L)                // 题面样例：1000 以内 11 个回文，和 4164
    check(p125Sum(5L, 2) == 0L && p125Sum(6L, 2) == 5L)   // 边界：5 = 1² + 2² 只在 limit > 5 时计入
    val prefix = p125SquarePrefixSums(100_000_000L)
    check(prefix[12] - prefix[5] == 595L)            // 题面例子：595 = 6² + 7² + … + 12²
    return p125Sum(100_000_000L, 2)
}
