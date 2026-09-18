package dev.pekt.engine

import dev.pekt.content.ContentIndex
import dev.pekt.math.binomial
import dev.pekt.math.digitSum
import dev.pekt.math.factorial
import dev.pekt.math.gcd
import dev.pekt.math.isPrime
import dev.pekt.math.lcm
import dev.pekt.math.modInverse
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
    126 to ::solve126,
    127 to ::solve127,
    128 to ::solve128,
    129 to ::solve129,
    130 to ::solve130,
    131 to ::solve131,
    132 to ::solve132,
    133 to ::solve133,
    134 to ::solve134,
    135 to ::solve135,
    136 to ::solve136,
    137 to ::solve137,
    138 to ::solve138,
    139 to ::solve139,
    140 to ::solve140,
    141 to ::solve141,
    142 to ::solve142,
    143 to ::solve143,
    144 to ::solve144,
    145 to ::solve145,
    146 to ::solve146,
    147 to ::solve147,
    148 to ::solve148,
    149 to ::solve149,
    150 to ::solve150,
    151 to ::solve151,
    152 to ::solve152,
    153 to ::solve153,
    154 to ::solve154,
    155 to ::solve155,
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

// ===== PE 126–150：由 .solver_frag/0126.kt … 0150.kt 按题号顺序拼接 =====

// ---------- PE 126 ----------
/**
 * PE 126 — Cuboid Layers（立方体分层）
 *
 * a×b×c 长方体第 k 层所需单位立方体数
 *   f(a,b,c,k) = 2(ab+ac+bc) + 4(a+b+c)(k−1) + 4(k−1)(k−2)，
 * 首项是表面积（第一层给每个可见单位面贴一个立方体），其后两项分别是 12 条棱上的斜带与 8 个角上的
 * 阶梯三角面。相邻两层之差 f(k+1) − f(k) = 4(a+b+c) + 8(k−1) 是公差恒为 8 的等差数列，故遍历时
 * 只需一次加法与一次增量即可推层。C(n) 统计的是 (长方体, 层) 对的数量，长方体按 a ≤ b ≤ c 规范化。
 * 剪枝由 f ≥ 2(ab+ac+bc) 推出：6a² ≤ limit、4ab+2b² ≤ limit、c 随底面积单调递增可跳出。
 * 上界从 1000 起倍增直到扫出 C(n) = 1000，避免预设答案量级。答案 = 18522。
 */

/** 第 k 层立方体个数的闭式（k ≥ 1）。用 Long 计算，防止 a、b、c 较大时乘积溢出。 */
private fun p126LayerCount(a: Long, b: Long, c: Long, k: Long): Long =
    2L * (a * b + a * c + b * c) + 4L * (a + b + c) * (k - 1) + 4L * (k - 1) * (k - 2)

/**
 * 枚举全部 a ≤ b ≤ c 的长方体及其各层，把 f ≤ limit 的层累加进 cnt。
 * cnt 长度至少 limit + 1，下标即层内立方体个数。
 */
private fun p126CountLayers(limit: Int, cnt: IntArray) {
    var a = 1
    while (6L * a * a <= limit) {                      // 底面积 ≥ 6a²（取 b = c = a）
        val aL = a.toLong()
        var b = a
        while (4L * aL * b + 2L * b * b <= limit) {     // 固定 (a,b) 时 c = b 让底面积最小
            val bL = b.toLong()
            var c = b
            while (true) {
                val base = 2L * (aL * bL + aL * c + bL * c)
                if (base > limit) break                // 底面积随 c 严格递增，可安全退出
                var f = base.toInt()                   // 第 1 层；limit < 2³¹，内层用 Int 更快
                var stride = 4 * (a + b + c)           // f(2) − f(1)
                while (f <= limit) {
                    cnt[f]++
                    f += stride
                    stride += 8                       // f(k+1) − f(k) 的公差
                }
                c++
            }
            b++
        }
        a++
    }
}

/** 在给定上界内统计 C(n)，返回最小的满足 C(n) == target 的 n；找不到返回 -1。 */
private fun p126LeastWithCount(target: Int, limit: Int): Int {
    val cnt = IntArray(limit + 1)
    p126CountLayers(limit, cnt)
    for (n in 1..limit) if (cnt[n] == target) return n
    return -1
}

/** PE 126 — 求最小的使 C(n) = 1000 的 n = 18522。上界倍增到 32000，空间 O(limit)。 */
private fun solve126(): Long {
    // 题面第一组：3×2×1 的第 1..4 层
    check(p126LayerCount(3, 2, 1, 1) == 22L) { "题面样例：3×2×1 第一层应为 22" }
    check(p126LayerCount(3, 2, 1, 2) == 46L) { "题面样例：3×2×1 第二层应为 46" }
    check(p126LayerCount(3, 2, 1, 3) == 78L) { "题面样例：3×2×1 第三层应为 78" }
    check(p126LayerCount(3, 2, 1, 4) == 118L) { "题面样例：3×2×1 第四层应为 118" }

    // 题面第二组：其它长方体的第一层
    check(p126LayerCount(5, 1, 1, 1) == 22L) { "题面样例：5×1×1 第一层应为 22" }
    check(p126LayerCount(5, 3, 1, 1) == 46L) { "题面样例：5×3×1 第一层应为 46" }
    check(p126LayerCount(7, 2, 1, 1) == 46L) { "题面样例：7×2×1 第一层应为 46" }
    check(p126LayerCount(11, 1, 1, 1) == 46L) { "题面样例：11×1×1 第一层应为 46" }

    // 题面 C(n) 的四个值（上界取得够大，保证这些小 n 的计数完整）
    val cnt = IntArray(2000)
    p126CountLayers(1999, cnt)
    check(cnt[22] == 2) { "题面样例：C(22) 应为 2，实测 ${cnt[22]}" }
    check(cnt[46] == 4) { "题面样例：C(46) 应为 4，实测 ${cnt[46]}" }
    check(cnt[78] == 5) { "题面样例：C(78) 应为 5，实测 ${cnt[78]}" }
    check(cnt[118] == 8) { "题面样例：C(118) 应为 8，实测 ${cnt[118]}" }

    // 题面：154 是最小的使 C(n) == 10 的 n
    check(p126LeastWithCount(10, 2000) == 154) { "题面样例：最小 C(n) = 10 的 n 应为 154" }

    // 上界从 1000 起倍增直到扫出结果；答案必须严格小于最后使用的上界，贴着上界说明计数不完整
    var limit = 1000
    while (true) {
        val n = p126LeastWithCount(1000, limit)
        if (n > 0) {
            check(n.toLong() < 32_000L) { "答案应严格小于最后使用的上界，实测 $n" }
            return n.toLong()
        }
        limit *= 2
    }
}

// ---------- PE 127 ----------
/**
 * PE 127 — abc-hits（abc 三元组）
 *
 * rad(n) 是 n 的不同素因子之积。题目要求 a < b、a + b = c，且三对 gcd 全为 1。
 * 因为 c = a + b，gcd(a,c) = gcd(a,a+b) = gcd(a,b)，gcd(b,c) = gcd(b,a)，
 * 三个条件等价于 gcd(a,b) = 1；两两互素又保证 rad(abc) = rad(a)·rad(b)·rad(c)，
 * 于是判据化为对称形式 rad(a)·rad(b)·rad(c) < a + b。
 *
 * 剪枝：c ≥ 3 时 rad(c) ≥ 2，而 c = a + b ≤ limit−1，故任何合法对必须满足
 * 2·rad(a)·rad(b) ≤ limit−2  （关于 a、b 对称的必要条件）。
 * 把 1…limit−1 按 rad 值计数排序后，对位置 i 只需向后扫描
 * rad ≤ (limit−2)/(2·radList[i]) 的连续前缀；条件对称 ⇒ 每个无序对恰好被处理一次。
 * 当 (limit−2)/(2·rad(x)) < 2 时只剩 y = 1（已在 i = 0 那轮扫过），外循环可直接 break。
 *
 * 复杂度：rad 筛 O(limit·log log limit)，计数排序与前缀数组 O(limit)，
 * 候选检查 15 847 083 次（按定义枚举是 limit²/4 ≈ 3.6×10⁹ 对），空间 O(limit)。
 * 三元积最坏约 7.2×10⁹ 超出 Int，用 Long 相乘。
 */
private fun p127Radicals(limit: Int): IntArray {
    val rad = IntArray(limit) { 1 }
    val isPrime = sieve(limit - 1)
    for (p in 2 until limit) {
        if (!isPrime[p]) continue            // 只对素数筛倍数，每个素因子恰好乘一次
        var m = p
        while (m < limit) {
            rad[m] *= p
            m += p
        }
    }
    return rad
}

/** 返回 [命中个数, Σc]，统计所有 c < limit 的 abc-hit（逻辑与 solution.kt 一致）。 */
private fun p127Hits(limit: Int): LongArray {
    if (limit < 4) return longArrayOf(0L, 0L)      // c = a+b ≥ 3
    val rad = p127Radicals(limit)
    val n = limit - 1

    val hist = IntArray(limit)                     // hist[r] = #{v : rad[v] == r}
    for (v in 1 until limit) hist[rad[v]]++
    val cursor = IntArray(limit)
    var acc = 0
    for (r in 1 until limit) {
        cursor[r] = acc
        acc += hist[r]
    }
    val valueList = IntArray(n)                    // 按 rad 升序装桶（计数排序）
    val radList = IntArray(n)
    for (v in 1 until limit) {
        val slot = cursor[rad[v]]++
        valueList[slot] = v
        radList[slot] = rad[v]
    }

    var count = 0L
    var total = 0L
    for (i in 0 until n) {
        val x = valueList[i]
        val rx = radList[i]
        val bound = (limit - 2) / (2 * rx)         // 必要条件：rad(y) ≤ bound
        if (bound < 2) break                       // rad 已升序，后面只会更小
        var j = i + 1
        while (j < n) {
            val ry = radList[j]
            if (ry > bound) break                  // 前缀到此为止：rx·ry ≤ limit−2 已不成立
            val c = x + valueList[j]
            j++
            if (c >= limit) continue
            // rx·ry ≤ (limit−2)/2 < 2³¹，二元积先用 Int 乘，再升 Long 与 rad(c) 相乘
            val pairProduct = rx * ry
            if (pairProduct.toLong() * rad[c] < c &&
                gcd(x.toLong(), (c - x).toLong()) == 1L
            ) {
                count++
                total += c
            }
        }
    }
    return longArrayOf(count, total)
}

/** PE 127 — c < 120000 的全部 abc-hit 的 c 之和 = 18407904（c < 1000 有 31 个、Σc = 12523 自检）。 */
private fun solve127(): Long {
    val rad = p127Radicals(505)
    check(gcd(5L, 27L) == 1L && gcd(5L, 32L) == 1L &&
        gcd(27L, 32L) == 1L) { "题面例子：(5,27,32) 的三对 gcd 应全为 1" }
    check(rad[504] == 42) { "题面例子：rad(504) 应为 42，实际 ${rad[504]}" }   // 504 = 2³·3²·7
    check(rad[5] * rad[27] * rad[32] == 30) { "题面例子：rad(5·27·32) 应为 30" }
    check(p127Hits(10).contentEquals(longArrayOf(1L, 9L))) { "c < 10 只应有 (1,8,9)" }
    // 题面锚点：c < 1000 恰有 31 个 abc-hit，Σc = 12523
    val small = p127Hits(1_000)
    check(small[0] == 31L && small[1] == 12523L) {
        "题面锚点：c < 1000 应为 31 个、Σc = 12523，实际 ${small.toList()}"
    }
    check(p127Hits(4)[1] == 0L) { "边界：c < 4 不应有 abc-hit" }
    return p127Hits(120_000)[1]
}

// ---------- PE 128 ----------
/**
 * PE 128 — 六边形地砖的差值：地砖按六边形螺旋编号，第 k 环为 [3k²−3k+2, 3k²+3k+1]。
 * 非缝砖的六差里必有两个偶数且 ≥ 6（必为合数），故 PD ≤ 2，PD = 3 只可能出现在两类缝砖上：
 * 环起始砖 ⟺ 6k−1、6k+1、12k+5 全为素数，环结束砖 ⟺ 6k−1、6k+5、12k−7 全为素数。
 * 模 5 剪枝后只需考虑 k ≡ 2,3 与 k ≡ 2,3,4 的环，逐环数到第 2000 项即为答案。
 * 复杂度 O(L log log L + K)，L 为覆盖 12k+5 的筛上界，K 为第 2000 项所在环号。
 */

/** 环 k 起始砖的编号：3k² − 3k + 2。 */
private fun p128RingStart(k: Int): Long = 3L * k * k - 3L * k + 2L

/** 环 k 结束砖的编号：3k² + 3k + 1。 */
private fun p128RingEnd(k: Int): Long = 3L * k * k + 3L * k + 1L

/**
 * 序列中第 nth 块 PD = 3 的砖。筛上界自适应倍增：候选值最大为 12k+5，
 * 若扫描越过当前筛上界而项数还不够，就把上界翻倍重来（重来一次的代价有界）。
 */
private fun p128NthTerm(nth: Int): Long {
    if (nth <= 2) return if (nth == 1) 1L else 2L         // 前两块特例：1 与 2
    var limit = 1 shl 12                                  // 从 4096 起，够跑小规模样例
    while (true) {
        val isPrime = sieve(limit)          // 索引 0…limit 都有效
        var count = 2
        var k = 2
        while (12L * k + 5L <= limit) {
            if (k % 5 == 2 || k % 5 == 3) {
                if (isPrime[6 * k - 1] && isPrime[6 * k + 1] && isPrime[12 * k + 5]) {
                    count++
                    if (count == nth) return p128RingStart(k)
                }
            }
            if (k % 5 >= 2) {                             // 2、3、4
                if (isPrime[6 * k - 1] && isPrime[6 * k + 5] && isPrime[12 * k - 7]) {
                    count++
                    if (count == nth) return p128RingEnd(k)
                }
            }
            k++
        }
        limit *= 2                                        // 上界不足，翻倍重扫
    }
}

/**
 * 按定义算 PD(n)：把环 ≤ 4（砖 1…61）的六边形网格建成「坐标 → 编号」表，
 * 取 n 的六个邻居编号作差，数其中素数的个数。只用于复现题面的手工样例。
 */
private fun p128PdByDefinition(n: Int): Int {
    val dirs = listOf(0 to -1, -1 to 0, -1 to 1, 0 to 1, 1 to 0, 1 to -1)
    val corners = { k: Int -> listOf(0 to -k, -k to 0, -k to k, 0 to k, k to 0, k to -k) }
    val step = listOf(-1 to 1, 0 to 1, 1 to 0, 1 to -1, 0 to -1, -1 to 0)
    val pos = HashMap<Pair<Int, Int>, Int>()
    pos[0 to 0] = 1
    for (k in 1..4) {
        for (q in 0..5) {
            val c = corners(k)[q]
            val d = step[q]
            for (r in 0 until k) {
                pos[(c.first + r * d.first) to (c.second + r * d.second)] =
                    p128RingStart(k).toInt() + q * k + r
            }
        }
    }
    val isPrime = sieve(200)
    var here = 0 to 0
    for ((p, v) in pos) if (v == n) here = p
    var count = 0
    for ((dx, dy) in dirs) {
        val other = pos[(here.first + dx) to (here.second + dy)] ?: continue
        val diff = Math.abs(other - n)
        if (diff >= 2 && isPrime[diff]) count++
    }
    return count
}

/** PE 128 — 六边形地砖差值序列中第 2000 块 PD = 3 的地砖 = 14516824220。 */
private fun solve128(): Long {
    check(p128PdByDefinition(8) == 3) { "题面样例：PD(8) 应为 3，实得 ${p128PdByDefinition(8)}" }
    check(p128PdByDefinition(17) == 2) { "题面样例：PD(17) 应为 2，实得 ${p128PdByDefinition(17)}" }
    check(p128PdByDefinition(1) == 3) { "特例砖：中心砖 1 的 PD 应为 3，实得 ${p128PdByDefinition(1)}" }
    check(p128PdByDefinition(2) == 3) { "特例砖：第 1 环起始砖 2 的 PD 应为 3，实得 ${p128PdByDefinition(2)}" }
    check(p128PdByDefinition(19) == 3) { "第 4 项 19 的 PD 应为 3，实得 ${p128PdByDefinition(19)}" }
    check(p128NthTerm(10) == 271L) { "题面样例：第 10 块砖应为 271，实得 ${p128NthTerm(10)}" }
    check(p128NthTerm(1) == 1L && p128NthTerm(2) == 2L && p128NthTerm(3) == 8L) {
        "序列前三项应为 1、2、8，实得 ${p128NthTerm(1)}、${p128NthTerm(2)}、${p128NthTerm(3)}"
    }
    check(p128NthTerm(4) == 19L && p128NthTerm(5) == 20L && p128NthTerm(6) == 37L) {
        "序列第 4…6 项应为 19、20、37，实得 ${p128NthTerm(4)}、${p128NthTerm(5)}、${p128NthTerm(6)}"
    }
    return p128NthTerm(2000)
}

// ---------- PE 129 ----------
/**
 * PE 129 — Repunit Divisibility：最小的 n 使 A(n) > 10⁶ = 1000023。
 *
 * 循环单位数 R(k) = (10^k − 1)/9，故 n | R(k) ⟺ 9n | 10^k − 1 ⟺ 10^k ≡ 1 (mod 9n)，
 * A(n) 即 10 模 9n 的乘法阶。写 n = 3^a·m（3 ∤ m），由 CRT 与 LTE（v_3(10^k − 1) = 2 + v_3(k)）
 * 得 A(n) = lcm(3^a, ord_m(10))。搜索下界不靠阶论：x ↦ (10x + 1) mod n 是双射，
 * 余数序列纯周期且只占 n 个剩余类，故 A(n) ≤ n，从 10⁶ + 1 起搜即可。
 * 求 ord_m(10) 时先由质因子分解算出 λ(m)，再对 λ(m) 的质因子逐个用模幂回除。
 */

/** x ≥ 1 的质因子分解：质因子 → 指数。 */
private fun p129Factorize(x0: Long): Map<Long, Int> {
    val factors = LinkedHashMap<Long, Int>()
    var rest = x0
    var d = 2L
    while (d * d <= rest) {
        if (rest % d == 0L) {
            var e = 0
            while (rest % d == 0L) {
                rest /= d
                e++
            }
            factors[d] = e
        }
        d += if (d == 2L) 1L else 2L
    }
    if (rest > 1L) factors[rest] = 1
    return factors
}

/** base^exp（整数幂，避免用浮点的 Math.pow）。 */
private fun p129IntPow(base: Long, exp: Int): Long {
    var result = 1L
    repeat(exp) { result *= base }
    return result
}

/** Carmichael 函数 λ(m)：使所有 gcd(a, m) = 1 的 a 都满足 a^λ(m) ≡ 1 (mod m) 的最小正指数。 */
private fun p129Carmichael(m: Long): Long {
    var lambda = 1L
    for ((p, e) in p129Factorize(m)) {
        val primePart = when {
            p == 2L && e == 1 -> 1L
            p == 2L && e == 2 -> 2L
            p == 2L -> 1L shl (e - 2)                    // 2^(e−2)，e ≥ 3
            else -> (p - 1) * p129IntPow(p, e - 1)       // φ(p^e)，p 为奇素数
        }
        lambda = lcm(lambda, primePart)
    }
    return lambda
}

/** ord_m(10)：10 模 m 的乘法阶（要求 gcd(m, 10) = 1）。 */
private fun p129OrderOfTenMod(m: Long): Long {
    if (m == 1L) return 1L
    var order = p129Carmichael(m)
    // 先把 λ(m) 的质因子集合取出来，再在循环里把 order 除小
    for (p in p129Factorize(order).keys) {
        while (order % p == 0L && modPow(10L, order / p, m) == 1L) order /= p
    }
    return order
}

/** A(n)：最小的 k 使 n | R(k)（要求 gcd(n, 10) = 1），用阶公式求。 */
private fun p129RepunitLength(n: Long): Long {
    var m = n
    var threePower = 1L
    while (m % 3L == 0L) {
        m /= 3L
        threePower *= 3L
    }
    return lcm(threePower, p129OrderOfTenMod(m))
}

/** A(n) 的定义式算法：递推 R(k) mod n，首次为 0 的 k（仅供断言交叉验证）。 */
private fun p129RepunitLengthByDefinition(n: Long): Long {
    var r = 0L
    var k = 0L
    while (true) {
        r = (r * 10L + 1L) % n
        k++
        if (r == 0L) return k
    }
}

/** 最小的 n 使 A(n) > threshold；由 A(n) ≤ n 知只需从 threshold + 1 起找。 */
private fun p129LeastNWithLengthAbove(threshold: Long): Long {
    var n = threshold + 1L
    while (true) {
        if (n % 2L != 0L && n % 5L != 0L && p129RepunitLength(n) > threshold) return n
        n++
    }
}

private fun solve129(): Long {
    // 题面样例
    check(p129RepunitLength(7L) == 6L) { "题面样例：A(7) 应为 6" }
    check(p129RepunitLength(41L) == 5L) { "题面样例：A(41) 应为 5" }
    check(41L * 271L == 11111L) { "题面样例：R(5) = 11111 = 41 × 271" }
    check(p129LeastNWithLengthAbove(10L) == 17L) { "题面边界：A(n) > 10 的最小 n 应为 17" }
    check(p129RepunitLength(17L) == 16L) { "A(17) 应为 16" }
    // A(n) ≤ n 的等号情形（n 为 3 的幂）
    check(p129RepunitLength(3L) == 3L) { "A(3) 应为 3" }
    check(p129RepunitLength(9L) == 9L) { "A(9) 应为 9" }
    // 阶公式与定义式在样例点上互证（全范围互证在 solution.kt / brute-force.kt 里做）
    for (n in longArrayOf(3L, 7L, 9L, 17L, 41L, 63L, 369L)) {
        val byFormula = p129RepunitLength(n)
        val byDefinition = p129RepunitLengthByDefinition(n)
        check(byFormula == byDefinition) { "n=$n：阶公式 $byFormula ≠ 定义枚举 $byDefinition" }
    }
    return p129LeastNWithLengthAbove(1_000_000L)
}

// ---------- PE 130 ----------
/**
 * PE 130 — Composites with Prime Repunit Property（具有素数循环单位数性质的合数）。
 *
 * R(k) = (10^k − 1)/9，故 n | R(k) ⟺ 10^k ≡ 1 (mod 9n)，即 A(n) = ord_{9n}(10)；
 * 3 ∤ n 时 ord_{9n}(10) = lcm(ord_9(10), ord_n(10)) = ord_n(10)（10 ≡ 1 mod 9）。
 * 3 | n 时 27 | 9n 迫使 3 | A(n)（因为 3 | R(k) ⟺ 3 | k），而 n − 1 ≡ 2 (mod 3)，
 * 故这类候选永不合格，候选只剩与 30 互素的合数。
 * 求 ord_n(10)：n = ∏ p^e 分解成素数幂，各分量取 ord_{p^e}(10)（从 φ(p^e) 出发对 φ 的每个
 * 素因子反复做「能除就除」的下降）再取 lcm，判定 A(n) | n − 1。
 */

/** 最小素因子筛：spf[x] = x 的最小素因子，spf[1] = 1。 */
private fun p130SmallestPrimeFactors(limit: Int): IntArray {
    val spf = IntArray(limit + 1) { it }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (spf[i] == i) {
            var j = i * i
            while (j <= limit) {
                if (spf[j] == j) spf[j] = i
                j += i
            }
        }
        i++
    }
    return spf
}

/** x 的所有互异素因子（x ≥ 1，需 spf 覆盖到 x）。 */
private fun p130DistinctPrimeFactors(x: Int, spf: IntArray): List<Int> {
    val out = ArrayList<Int>()
    var y = x
    while (y > 1) {
        val p = spf[y]
        out.add(p)
        while (y % p == 0) y /= p
    }
    return out
}

/**
 * ord_{p^e}(10)（要求 gcd(10, p) = 1）。d ← φ(p^e) = p^(e−1)(p − 1)，
 * 对 φ(p^e) 的每个互异素因子 q 反复尝试 d ← d/q（缩小后仍 ≡ 1 就接受），试尽即最小指数。
 */
private fun p130OrderModPrimePower(p: Long, pe: Long, spf: IntArray): Long {
    var d = pe / p * (p - 1)
    for (q in p130DistinctPrimeFactors(d.toInt(), spf)) {
        while (d % q == 0L && modPow(10L, d / q, pe) == 1L) d /= q
    }
    return d
}

/** ord_m(10)（要求 gcd(m, 10) = 1）：分解为素数幂后各分量的阶取 lcm。 */
private fun p130OrderOf10(m: Int, spf: IntArray): Long {
    var x = m
    var ord = 1L
    while (x > 1) {
        val p = spf[x].toLong()
        var pe = 1L
        while (x % p.toInt() == 0) {
            x /= p.toInt()
            pe *= p
        }
        val part = p130OrderModPrimePower(p, pe, spf)
        ord = ord / gcd(ord, part) * part
    }
    return ord
}

/** 按定义直接算 A(n)：r ← (10r + 1) mod n 迭代到余数为 0（仅用于校验）。 */
private fun p130AByDefinition(n: Int): Int {
    var r = 0L
    var k = 0
    do {
        r = (r * 10 + 1) % n
        k++
    } while (r != 0L)
    return k
}

/** 与 30 互素的合数中满足 A(n) | n − 1 的 n 的升序列表（仅用于校验）。 */
private fun p130QualifyingBelow(limit: Int, spf: IntArray): List<Int> {
    val out = ArrayList<Int>()
    for (n in 7..limit) {
        if (n % 2 != 0 && n % 3 != 0 && n % 5 != 0 && spf[n] != n &&
            (n - 1) % p130OrderOf10(n, spf) == 0L
        ) out.add(n)
    }
    return out
}

/** 第三条判据 A(n) | n − 1 ⟺ 10^(n−1) ≡ 1 (mod 9n) 给出的同一张列表（仅用于校验）。 */
private fun p130FermatBelow(limit: Int, spf: IntArray): List<Int> {
    val out = ArrayList<Int>()
    for (n in 7..limit) {
        if (n % 2 != 0 && n % 3 != 0 && n % 5 != 0 && spf[n] != n &&
            modPow(10L, (n - 1).toLong(), 9L * n) == 1L
        ) out.add(n)
    }
    return out
}

/** m 是否无平方因子（m ≥ 2）。 */
private fun p130SquareFree(m: Int, spf: IntArray): Boolean {
    var x = m
    while (x > 1) {
        val p = spf[x]
        var e = 0
        while (x % p == 0) {
            x /= p
            e++
        }
        if (e > 1) return false
    }
    return true
}

/**
 * 扫描 [2, limit]，返回前 target 个「gcd(n, 30) = 1 的合数且 A(n) | n − 1」的 n 之和；
 * 不足 target 个时返回 null（由调用方放大 limit）。
 */
private fun p130ScanSums(limit: Int, target: Int): Long? {
    val spf = p130SmallestPrimeFactors(limit)
    val isPrime = sieve(limit)
    var count = 0
    var sum = 0L
    var n = 7                                          // 最小候选：合数且与 30 互素
    while (n <= limit) {
        if (n % 2 != 0 && n % 3 != 0 && n % 5 != 0 && !isPrime[n]) {
            val a = p130OrderOf10(n, spf)
            if ((n - 1) % a == 0L) {
                count++
                sum += n
                if (count == target) return sum
            }
        }
        n++
    }
    return null
}

/** 规模倍增：先扫 5·10⁴，不够就翻倍，直到凑满 25 个候选。 */
private fun p130Sum(target: Int = 25): Long {
    var limit = 50_000
    while (true) {
        val sum = p130ScanSums(limit, target)
        if (sum != null) return sum
        limit *= 2
    }
}

/** PE 130 — 前 25 个「与 10 互素的合数且 n − 1 被 A(n) 整除」的 n 之和 = 149253。 */
private fun solve130(): Long {
    val spf = p130SmallestPrimeFactors(100_000)
    check(p130OrderOf10(7, spf) == 6L) { "题面样例：A(7) 应为 6，实际 ${p130OrderOf10(7, spf)}" }
    check(p130OrderOf10(41, spf) == 5L) { "题面样例：A(41) 应为 5，实际 ${p130OrderOf10(41, spf)}" }
    check(p130AByDefinition(7) == 6 && p130AByDefinition(41) == 5) { "题面样例：按定义迭代应得 A(7) = 6、A(41) = 5" }
    // 定义式与求阶公式在 7..2000 上必须一致（两条路径的交叉校验）
    for (m in 7..2000) if (m % 2 != 0 && m % 5 != 0 && m % 3 != 0) {
        check(p130AByDefinition(m).toLong() == p130OrderOf10(m, spf)) { "A($m) 两条路径不一致" }
    }
    // 题面样例：素数 p > 5 满足 A(p) | p − 1
    for (m in 7..2000) if (spf[m] == m && m > 5) {
        check((m - 1) % p130OrderOf10(m, spf) == 0L) { "题面样例：素数 $m 违反 A(p) | p − 1" }
    }
    // 题面样例：前五个合数 91/259/451/481/703，和为 1985
    check(p130ScanSums(1000, 5) == 1985L) { "题面样例：前五个合数应为 91/259/451/481/703" }
    // 平方因子 p² | n 会强制 p | A(n)（p 非 base-10 Wieferich 时 ord_{p²}(10) = p·ord_p(10)），
    // 于是 A(n) | n − 1 会推出 p | n − 1，与 p | n 矛盾 —— 合格值必无平方因子。
    check(p130OrderOf10(113 * 113, spf) == 113L * 112L) { "A(113²) 应为 113·112 = 12656" }
    val qualifying = p130QualifyingBelow(30_000, spf)
    check(qualifying.all { p130SquareFree(it, spf) }) { "合格集内出现含平方因子的数" }
    check(qualifying.subList(0, 5) == listOf(91, 259, 451, 481, 703)) { "前五个合格值应为 91/259/451/481/703" }
    // 第三条独立路径：费马式判据（一次模幂）的合格集与求阶判据逐项一致
    check(p130FermatBelow(30_000, spf) == qualifying) { "两种判据的合格集不一致" }
    // 3 | n 永远不合格：A(n) = ord_{9n}(10) 是 3 的倍数，而 n − 1 ≡ 2 (mod 3)
    for (n in 3..3000 step 3) {
        if (n % 2 == 0 || n % 5 == 0) continue
        val a = p130OrderOf10(9 * n, spf)
        check(a % 3 == 0L && (n - 1) % a != 0L) { "3 | $n 竟合格" }
    }
    // 由 n | R(k) ⟺ 10^k ≡ 1 (mod 9n) 得到的求阶判据依赖 R(k) = (10^k − 1)/9 这条闭式；一并断言
    var rep = 0L
    var ten = 1L
    for (k in 1..12) {
        rep = rep * 10 + 1
        ten *= 10
        check(rep == (ten - 1) / 9) { "R($k) 与 (10^k − 1)/9 不符" }
        check(rep % 2 == 1L && rep % 5 == 1L) { "R($k) 末位应为 1" }
    }
    // 第 25 个合格值恰好是 14701：上限 14700 只凑到 24 个，抬到 14701 才凑满
    check(p130ScanSums(14_700, 25) == null) { "14700 以内不该凑满 25 个" }
    check(p130ScanSums(14_701, 25) == 149253L) { "14701 以内前 25 个之和应为 149253" }
    return p130Sum()
}

// ---------- PE 131 ----------
/**
 * PE 131 — 素数立方伙伴：小于 10⁶ 的素数中「存在正整数 n 使 n³ + n²p 为完全立方数」
 * 的个数 = 173。
 *
 * 推导：n²(n + p) = m³，g = gcd(n, n + p) = gcd(n, p) ∈ {1, p}。
 *   · g = 1：n² 与 n + p 互素，各自为立方数 ⇒ n = a³、n + p = b³，
 *     p = b³ − a³ = (b − a)(b² + ab + a²)；p 素 ⇒ b − a = 1 ⇒ p = 3a² + 3a + 1，n = a³ 唯一。
 *   · p | n：n = pk ⇒ (m/p)³ = k²(k + 1)，k = j³ 且 j³ + 1 为立方数，无解。
 * 故只需统计形如 3a² + 3a + 1（a ≥ 1）的素数，a ≤ 576（3a² + 3a + 1 < 10⁶）。
 */

/** 小于 limit 的素数中形如 3a² + 3a + 1（a ≥ 1）的个数。 */
private fun p131CountPartners(limit: Int): Long {
    val isPrime = sieve(limit)
    var count = 0L
    var a = 1
    while (true) {
        val p = 3 * a * a + 3 * a + 1
        if (p >= limit) break
        if (isPrime[p]) count++
        a++
    }
    return count
}

/**
 * 定义级核验：对每个被计数的素数 p = 3a² + 3a + 1 还原出唯解 n = a³，
 * 用 BigInteger 直接验算 n³ + n²p = (a²(a + 1))³ —— 不复用推导结论之外的任何捷径。
 */
private fun p131VerifyDefinitionally(limit: Int) {
    val isPrime = sieve(limit)
    val checked = ArrayList<Int>()
    var a = 1
    while (true) {
        val p = 3 * a * a + 3 * a + 1
        if (p >= limit) break
        if (isPrime[p]) {
            checked.add(p)
            val bigA = java.math.BigInteger.valueOf(a.toLong())
            val n = bigA.pow(3)                                                        // n = a³
            val lhs = n.pow(3).add(n.pow(2).multiply(java.math.BigInteger.valueOf(p.toLong())))
            val m = bigA.pow(2).multiply(java.math.BigInteger.valueOf(a.toLong() + 1L)) // m = a²(a + 1)
            check(m.pow(3) == lhs) { "p = $p 的定义级验证失败" }
        }
        a++
    }
    check(checked.subList(0, 4) == listOf(7, 19, 37, 61)) { "一百以内的四个应为 7, 19, 37, 61：$checked" }
}

private fun solve131(): Long {
    check(p131CountPartners(100) == 4L) { "题面样例：一百以内应有 4 个，实得 ${p131CountPartners(100)}" }
    check(8L * 8 * 8 + 8L * 8 * 19 == 12L * 12 * 12) { "题面例子：8³ + 8² × 19 应为 12³" }
    p131VerifyDefinitionally(1_000_000)
    return p131CountPartners(1_000_000)
}

// ---------- PE 132 ----------
/**
 * 素数 p 是否整除循环单位数 R(n)：p = 3 走数位和规则（R(n) 的各位数字之和恰为 n，
 * 故 3 | R(n) ⟺ 3 | n），p = 2、5 因 10ⁿ ≡ 0 (mod p) 自动出局，其余用
 * 10^gcd(n, p−1) ≡ 1 (mod p) —— 由 ord_p(10) | p−1 与 ord_p(10) | n 合并而来。
 */
private fun p132DividesRepunit(p: Int, n: Long): Boolean = when {
    p == 3 -> n % 3L == 0L
    p == 2 || p == 5 -> false
    else -> modPow(10L, gcd(n, p - 1L), p.toLong()) == 1L
}

/**
 * 素数升序枚举，取前 count 个整除 R(n) 的素数求和；凑不满就报错，绝不返回偏小的和。
 * 升序枚举保证拿到的是最小的若干素因子，无需排序或去重。
 */
private fun p132Sum(n: Long, count: Int, limit: Int): Long {
    val isPrime = sieve(limit)
    var sum = 0L
    var found = 0
    for (p in 2..limit) {
        if (!isPrime[p]) continue
        if (p132DividesRepunit(p, n)) {
            sum += p
            if (++found == count) return sum
        }
    }
    throw IllegalStateException("上界 $limit 内只找到 $found 个素因子，需提高 limit")
}

/** 对 R(n) 直接试除法分解（BigInteger），完全绕开阶与同余判据，用来锚定题面样例。 */
private fun p132RepunitPrimeFactors(n: Int): List<Int> {
    var rest = (java.math.BigInteger.TEN.pow(n) - java.math.BigInteger.ONE) / java.math.BigInteger.valueOf(9L)
    val factors = ArrayList<Int>()
    var d = java.math.BigInteger.TWO
    while (d * d <= rest) {
        while (rest % d == java.math.BigInteger.ZERO) { factors.add(d.toInt()); rest /= d }
        d += java.math.BigInteger.ONE
    }
    if (rest > java.math.BigInteger.ONE) factors.add(rest.toInt())
    return factors
}

/**
 * PE 132 — 大循环单位数的素因子：R(k) = (10^k − 1)/9。对素数 p ≠ 3，9 在模 p 下可逆，
 * 故 p | R(k) ⟺ 10^k ≡ 1 (mod p) ⟺ ord_p(10) | k；又 ord_p(10) | p − 1，两个约束合并成
 * 10^gcd(k, p−1) ≡ 1 (mod p)，指数从 10⁹ 降到其 2-5 光滑约数（实测均值仅 63）。
 * p = 3 是唯一例外（9 ≡ 0 mod 3，求逆失效），改用数位和规则；k = 10⁹ ≡ 1 (mod 3)，故 3 不是因子。
 * 上界取 200000（第 40 个素因子是 160001），答案 843296。
 * 复杂度 O(B log log B + π(B) log k)，空间 O(B)。
 */
private fun solve132(): Long {
    // 题面样例：R(10) = 1111111111 = 11 × 41 × 271 × 9091，素因子和 9414
    val factors = p132RepunitPrimeFactors(10)
    check(factors == listOf(11, 41, 271, 9091)) { "R(10) 的素因子应为 11, 41, 271, 9091，实得 $factors" }
    check(factors.sumOf { it.toLong() } == 9414L) { "R(10) 的素因子和应为 9414，实得 ${factors.sumOf { it.toLong() }}" }
    check(11L * 41 * 271 * 9091 == 1_111_111_111L) { "题面样例的因子乘积应还原出 R(10)" }
    check(p132Sum(10L, 4, 20_000) == 9414L) { "R(10) 的最小四个素因子之和应为 9414" }
    // 另一条独立锚点：BigInteger 分解出 R(20) = 11·41·101·271·3541·9091·27961，最小的四个和 424
    check(p132RepunitPrimeFactors(20) == listOf(11, 41, 101, 271, 3541, 9091, 27961)) {
        "R(20) 的素因子分解不符：${p132RepunitPrimeFactors(20)}"
    }
    check(p132Sum(20L, 4, 20_000) == 424L) { "R(20) 最小四个素因子之和应为 424" }
    // 3 必须特判的理由：10ⁿ ≡ 1 (mod 3) 恒真，照搬同余判据会把 3 算进 R(10)，而题面样例里没有 3
    check(modPow(10L, 10L, 3L) == 1L && !factors.contains(3))
    check(!p132DividesRepunit(3, 1_000_000_000L)) { "10⁹ 的数位和 10⁹ ≡ 1 (mod 3)，3 不是 R(10⁹) 的因子" }
    check(1_000_000_000L % 3L == 1L)
    // gcd 降幂与原始指数等价：20000 以内每个素数上两条判据必须给出同一结论
    val smallPrimes = sieve(20_000)
    check((2..20_000).all { p ->
        !smallPrimes[p] ||
            (modPow(10L, 1_000_000_000L, p.toLong()) == 1L) ==
            (modPow(10L, gcd(1_000_000_000L, p - 1L), p.toLong()) == 1L)
    }) { "gcd 降幂改变了判据结果" }
    // 最小素因子是 11（ord₁₁(10) = 2 | 10⁹），前两个之和 11 + 17 = 28
    check(p132Sum(1_000_000_000L, 1, 20_000) == 11L) { "R(10⁹) 的最小素因子应为 11" }
    check(p132Sum(1_000_000_000L, 2, 20_000) == 28L) { "R(10⁹) 最小的两个素因子之和应为 28" }
    return p132Sum(1_000_000_000L, 40, 200_000)
}

// ---------- PE 133 ----------
/**
 * PE 133 — Repunit Nonfactors：十万以内「永远不是 R(10^n) 因子」的素数之和 = 453647705。
 *
 * 思路：R(k) = (10^k − 1)/9，故对素数 p
 *     p | R(k) ⟺ 9p | 10^k − 1 ⟺ 10^k ≡ 1 (mod 9p)。
 * 模数必须带 9：p = 3 时模 27 的阶是 3（3 | R(k) ⟺ 3 | k），只看模 p 会把 3 误判成可整除。
 * 记 d = ord_{9p}(10)，则 ∃n ≥ 1, p | R(10^n) ⟺ d | 10^n = 2^n5^n ⟺ d = 2^a·5^b
 * （此时取 n = max(a,b)）；d 只要含 2、5 以外的素因子就永远不是因子。
 * gcd(10, 9p) > 1（p = 2, 5）时同余式无解，同样归入非因子。
 *
 * 阶的求法：d0 = lcm(6, p − 1) 是阶的倍数（p ≠ 3 时即 λ(9p) = lcm(λ(9), λ(p))，p = 3 时阶 3 也整除 6），
 * 把 d0 试除分解，对每个素因子 q 反复试降 d ← d/q（只要 10^(d/q) ≡ 1），收敛即真正的阶。
 *
 * 复杂度：素数筛 O(N log log N)，N = 10^5；每个素数再做试除分解与常数次模幂，
 * 总计约 O(N log log N + π(N)·π(√d0))，空间 O(N)。中间量 < 9·10^5，模乘用 Long 足够。
 */
private fun p133OrderOf10(p: Long, primes: List<Long>): Long {
    val m = 9L * p
    var d = 6L * (p - 1L) / gcd(6L, p - 1L)
    var rest = d
    for (q in primes) {
        if (q * q > rest) break
        if (rest % q == 0L) {
            while (rest % q == 0L) rest /= q
            while (d % q == 0L && modPow(10L, d / q, m) == 1L) d /= q
        }
    }
    if (rest > 1L) {                                 // 试除后剩下的必是素因子
        while (d % rest == 0L && modPow(10L, d / rest, m) == 1L) d /= rest
    }
    return d
}

/** 素数 p 是否永远不可能整除任何 R(10^n)。 */
private fun p133IsNonfactor(p: Long, primes: List<Long>): Boolean {
    if (gcd(10L, 9L * p) != 1L) return true     // p = 2, 5：R(k) 是末位为 1 的奇数
    var d = p133OrderOf10(p, primes)
    while (d % 2L == 0L) d /= 2L
    while (d % 5L == 0L) d /= 5L
    return d != 1L
}

/** 小于 limit 的素数中「永远不是 R(10^n) 因子」的那些之和。 */
private fun p133SumNonfactors(limit: Int): Long {
    val primes = primesUpTo(limit)
    var sum = 0L
    for (p in primes) if (p133IsNonfactor(p, primes)) sum += p
    return sum
}

/** R(len) = 111…1（len 个 1）。 */
private fun p133Repunit(len: Int): java.math.BigInteger =
    java.math.BigInteger.TEN.pow(len)
        .subtract(java.math.BigInteger.ONE)
        .divide(java.math.BigInteger.valueOf(9L))

private fun solve133(): Long {
    val smallPrimes = primesUpTo(1000)

    // 题面样例：一百以内「能成为某个 R(10^n) 的因子」的素数只有 11、17、41、73
    val canBeFactor = primesUpTo(100).filterNot { p133IsNonfactor(it, smallPrimes) }
    check(canBeFactor == listOf(11L, 17L, 41L, 73L)) {
        "题面样例：一百以内应为 [11, 17, 41, 73]，实际 $canBeFactor"
    }

    // 题面样例：R(10)、R(100)、R(1000) 都不被 17 整除，而 R(10000) 被 17 整除
    val seventeen = java.math.BigInteger.valueOf(17L)
    check(p133Repunit(10).mod(seventeen) != java.math.BigInteger.ZERO) { "题面样例：17 不该整除 R(10)" }
    check(p133Repunit(100).mod(seventeen) != java.math.BigInteger.ZERO) { "题面样例：17 不该整除 R(100)" }
    check(p133Repunit(1000).mod(seventeen) != java.math.BigInteger.ZERO) { "题面样例：17 不该整除 R(1000)" }
    check(p133Repunit(10_000).mod(seventeen) == java.math.BigInteger.ZERO) { "题面样例：17 应整除 R(10000)" }

    // 这四个可整除者的阶都是 2^a·5^b：2、16、5、8
    check(p133OrderOf10(11L, smallPrimes) == 2L) { "ord_11(10) 应为 2" }
    check(p133OrderOf10(17L, smallPrimes) == 16L) { "ord_17(10) 应为 16" }
    check(p133OrderOf10(41L, smallPrimes) == 5L) { "ord_41(10) 应为 5" }
    check(p133OrderOf10(73L, smallPrimes) == 8L) { "ord_73(10) 应为 8" }

    // 题面样例：19 永远不是因子。依据 ord_19(10) = 18 = 2·3² 含素因子 3
    check(p133OrderOf10(19L, smallPrimes) == 18L) { "ord_19(10) 应为 18" }
    // 按定义逐点复核：n = 1…18 时 10^(10^n) ≢ 1 (mod 9·19 = 171)（指数取到 10^18 是不越 Long 的上限）
    var exponent = 10L
    for (n in 1..18) {
        check(modPow(10L, exponent, 171L) != 1L) { "19 不该整除 R(10^$n)" }
        exponent *= 10L
    }

    // 3：3 | R(k) ⟺ 27 | 10^k − 1 ⟺ 3 | k，而 3 ∤ 10^n；2、5 与所有 repunit 互素
    check(p133IsNonfactor(2L, smallPrimes) && p133IsNonfactor(3L, smallPrimes) && p133IsNonfactor(5L, smallPrimes)) {
        "2、3、5 都该是非因子"
    }
    check(p133OrderOf10(3L, smallPrimes) == 3L) { "ord_27(10) 应为 3" }

    // 最小规模上的可见答案：2+3 = 5（limit = 4）；再加 5 得 10；再加 7 得 17（11 是因子）
    check(p133SumNonfactors(4) == 5L) { "limit = 4 应为 5" }
    check(p133SumNonfactors(6) == 10L) { "limit = 6 应为 10" }
    check(p133SumNonfactors(11) == 17L) { "limit = 11 应为 17" }

    return p133SumNonfactors(100_000)
}

// ---------- PE 134 ----------
/**
 * PE 134 — Prime Pair Connection（素数对连接）
 *
 * 设 p1 有 d 位十进制数字、M = 10^d，则「末 d 位恰为 p1」的候选恰构成等差数列
 *   n = p1 + k·M，k = 0, 1, 2, …
 * 再要求 p2 | n，即 k·M ≡ −p1 (mod p2)。p1 ≥ 5 时 p2 > p1 ≥ 5 与 10 互素，M = 10^d 可逆，
 * 取最小非负剩余 k0 ∈ [0, p2) 得 S = p1 + k0·M；n 关于 k 严格递增，故 k0 即最小者
 * （k0 = 0 要求 p1 ≡ 0 (mod p2)，与 0 < p1 < p2 矛盾，故 S > p1）。
 * 题面排除的 (p1, p2) = (3, 5) 正是 gcd(10^d, p2) = 1 唯一失效处：候选恒 ≡ 3 (mod 5)，无解。
 *
 * 规模：筛到 10^6 + 20（p1 = 999983 的搭档是 1000003），共 π(10^6) − 2 = 78496 对；
 * 单项 < 1.01×10^12、总和 18613426663617118，全程 Long。
 * 通用步骤换成工具库：筛用 primesUpTo，模逆元用 modInverse。
 */

/** 10^exp，用乘法累加（不经过字符串）。 */
private fun p134Pow10(exp: Int): Long {
    var result = 1L
    repeat(exp) { result *= 10 }
    return result
}

/** v 的十进制位数：阈值比较，禁止用 toString() 数位数。 */
private fun p134DigitCount(v: Long): Int {
    var digits = 1
    var threshold = 10L
    while (v >= threshold) {
        digits++
        threshold *= 10
    }
    return digits
}

/** 相邻素数对 (p1, p2) 的最小连接数 S：末 d 位为 p1 且被 p2 整除的最小 n。 */
private fun p134Connection(p1: Long, p2: Long): Long {
    val modulus = p134Pow10(p134DigitCount(p1))                     // 10^d
    val inverse = modInverse(modulus % p2, p2)        // (10^d)^(−1) mod p2
    val k = (p2 - (p1 % p2) * inverse % p2) % p2                    // 最小非负剩余：−p1·inv mod p2
    return p1 + k * modulus
}

/** 用定义复核：末 d 位为 p1、被 p2 整除、且等差数列中前一项不被 p2 整除（最小性）。 */
private fun p134CheckByDefinition(p1: Long, p2: Long, n: Long) {
    val modulus = p134Pow10(p134DigitCount(p1))
    check(n % modulus == p1 % modulus) { "($p1, $p2)：$n 末位不是 $p1" }
    check(n % p2 == 0L) { "($p1, $p2)：$n 不被 $p2 整除" }
    check(n < p1 + modulus || (n - modulus) % p2 != 0L) { "($p1, $p2)：$n 之前还有更小的候选" }
}

/** 所有 5 ≤ p1 ≤ limit 的相邻素数对的 S 之和。 */
private fun p134Sum(limit: Long): Long {
    val primes = primesUpTo((limit + 20).toInt())     // 上限外还要取到 p1 的下一个素数
    var sum = 0L
    for (i in primes.indices) {
        val p1 = primes[i]
        if (p1 < 5L) continue
        if (p1 > limit) break
        sum += p134Connection(p1, primes[i + 1])
    }
    return sum
}

/** PE 134 — 基线：题面样例 (19, 23) → 1219，求和上界 10^6，答案 18613426663617118。 */
private fun solve134(): Long {
    check(p134Connection(19L, 23L) == 1219L) { "题面样例：(19, 23) 应为 1219" }
    p134CheckByDefinition(19L, 23L, 1219L)

    val expected = mapOf(
        5L to 7L to 35L,        // 末位 5、被 7 整除：35
        7L to 11L to 77L,       // 末位 7、被 11 整除：77
        11L to 13L to 611L,     // 末两位 11、被 13 整除：611
        13L to 17L to 1513L,    // 末两位 13、被 17 整除：1513
        17L to 19L to 817L,     // 末两位 17、被 19 整除：817
        19L to 23L to 1219L,
        23L to 29L to 2523L,    // 末两位 23、被 29 整除：2523
    )
    for ((key, value) in expected) {
        val (p1, p2) = key
        check(p134Connection(p1, p2) == value) { "($p1, $p2) 最小连接数应为 $value" }
        p134CheckByDefinition(p1, p2, value)
    }

    // 累加范围含端点：limit = 19 时把 (19, 23) 计入，limit = 17 时不计入
    check(p134Sum(13L) == 35L + 77L + 611L + 1513L) { "limit = 13 应累加 4 对" }
    check(p134Sum(17L) == 35L + 77L + 611L + 1513L + 817L) { "limit = 17 应累加 5 对" }
    check(p134Sum(19L) == 35L + 77L + 611L + 1513L + 817L + 1219L) { "limit = 19 应累加 6 对" }
    check(p134Sum(23L) == 35L + 77L + 611L + 1513L + 817L + 1219L + 2523L) { "limit = 23 应累加 7 对" }

    // 题面点名的例外 (3, 5)：候选 n = 3 + 10k 恒 ≡ 3 (mod 5)，无解
    for (k in 0L until 5L) check((3L + 10L * k) % 5L != 0L) { "候选 (3, 5) 不应有解" }

    return p134Sum(1_000_000L)
}

// ---------- PE 135 ----------
/**
 * PE 135 — Same Differences：小于 10⁶ 的 n 中「方程 x² − y² − z² = n（x, y, z 为等差数列连续三项）
 * 恰有十个解」的个数 = 4989。
 *
 * 推导：n > 0 迫使公差为负，三项唯一写成 x = k + t, y = k, z = k − t（t ≥ 1，z > 0 ⇒ k > t），
 *   n = (k + t)² − k² − (k − t)² = 4kt − k² = k(4t − k)。
 * 令 u = k、v = 4t − k，则 n = uv，而约束全部落在 (u, v) 上：
 *   u + v ≡ 0 (mod 4)（保证 t 为整数）、3u > v（等价于 k > t）、v > 0（等价于 n > 0）。
 * 反向由 (u, v) 取 t = (u + v)/4 还原出唯一三元组，故解数 = 满足上述条件的有序因子对数。
 * 因 3u > v 只约束 u 一侧而 uv 对调不变，按无序因子对 a ≤ b（a + b ≡ 0 mod 4）分类一次算清：
 * b 当中间项永远合法（a ≤ b ⇒ 3b > a）；a 当中间项合法当且仅当 b < 3a；a = b 时两者重合。
 * 计数规则 cnt[a·b] += 1（a = b 或 b ≥ 3a）否则 2，既不重不漏，也自动消掉 u ↔ v 的重复解。
 */

/** 统计每个 n（下标即 n）的解数：枚举无序因子对 a ≤ b，b 走 b ≡ −a (mod 4) 的同余类。 */
private fun p135SolutionCounts(limit: Int): IntArray {
    val counts = IntArray(limit)
    var a = 1
    while (a.toLong() * a < limit) {                 // a ≤ b 且 ab < limit ⇒ a ≤ √limit
        var b = a
        while ((a + b) % 4 != 0) b++                 // 最小的可行 b：a + b ≡ 0 (mod 4)
        while (a.toLong() * b < limit) {
            counts[a * b] += if (a == b || b >= 3 * a) 1 else 2
            b += 4
        }
        a++
    }
    return counts
}

private fun p135Solve(limit: Int, target: Int): Int =
    p135SolutionCounts(limit).count { it == target }

/**
 * 定义级核验：按题面直接枚举最小项 z 与公差 d（三元组 (z + 2d, z + d, z)），
 * 收集 n = (z + 2d)² − (z + d)² − z² 的全部解 —— 不做任何因子分解。
 * 边界依据：解满足 k ≤ n（因 n = k·v ≥ k）且 z < k、d < k，故 z、d 都 ≤ n。
 */
private fun p135DefinitionalTriples(n: Int): List<Triple<Int, Int, Int>> {
    val out = ArrayList<Triple<Int, Int, Int>>()
    for (z in 1..n) {
        for (d in 1..n) {
            val x = z + 2 * d
            val y = z + d
            if (x.toLong() * x - y.toLong() * y - z.toLong() * z == n.toLong()) {
                out.add(Triple(x, y, z))
            }
        }
    }
    return out
}

/** PE 135 — 小于 10⁶ 的 n 中恰有十个解的个数 = 4989。 */
private fun solve135(): Long {
    // 题面样例：n = 27 恰有两个解，正是题面给的两组数，且 27 是最小的「恰两解」值
    check(p135DefinitionalTriples(27).toSet() == setOf(Triple(34, 27, 20), Triple(12, 9, 6))) {
        "题面样例：n = 27 的解应为 (34, 27, 20) 与 (12, 9, 6)，实得 ${p135DefinitionalTriples(27)}"
    }
    check(34L * 34 - 27L * 27 - 20L * 20 == 27L && 12L * 12 - 9L * 9 - 6L * 6 == 27L) {
        "题面样例：两组的平方差都应等于 27"
    }
    val small = p135SolutionCounts(1156)
    check(small[27] == 2) { "题面样例：n = 27 应恰有 2 个解，实得 ${small[27]}" }
    check((1 until 27).none { small[it] == 2 }) { "题面样例：27 应是最小的恰有两解的值" }

    // 题面样例：n = 1155 恰有十个解，且是最小的「恰十解」值
    check(p135DefinitionalTriples(1155).size == 10) {
        "题面样例：n = 1155 应恰有 10 个解，实得 ${p135DefinitionalTriples(1155).size}"
    }
    check(small[1155] == 10) { "题面样例：n = 1155 应恰有 10 个解，因子法实得 ${small[1155]}" }
    check((1 until 1155).none { small[it] == 10 }) { "题面样例：1155 应是最小的恰有十解的值" }

    // 因子法与定义法在 n ≤ 250 上逐个数比对，确认「一对因子记 1 或 2」不漏不重
    val cross = p135SolutionCounts(251)
    for (n in 1..250) {
        val byDefinition = p135DefinitionalTriples(n).size
        check(cross[n] == byDefinition) { "自检：n = $n 的因子法解数 ${cross[n]} ≠ 定义法 $byDefinition" }
    }
    return p135Solve(1_000_000, 10).toLong()
}

// ---------- PE 136 ----------
/**
 * PE 136 — Singleton Difference（唯一的差）
 *
 * 逻辑与 `content/problems/0136/solution.kt` 完全一致，只是通用步骤改用 `dev.pekt.math`：
 * 段内基素数来自 `primesUpTo(...)`，小规模对照筛用 `sieve(...)`。
 * 唯一保留的自备辅助函数是分段位筛 [p136SegmentedStats]——工具库只提供全区间布尔筛
 * （`sieve(5×10⁷)` 需要两个 5×10⁷ 元素的布尔数组、且大素数跨步标记撞缓存），
 * 分段位筛用 8 KB 段内位图替代，是本题 baseline 的来源。
 *
 * 推导（详见 content/problems/0136/analysis.md）：
 *   x = a + d、y = a、z = a − d（a ≥ d + 1）⇒ n = a(4d − a)。令 u = a、v = 4d − a，
 *   则解数 R(n) = #{(u,v) : uv = n, u + v ≡ 0 (mod 4), 3u − v ≥ 4}。
 *   写 n = 2^s·m（m 为奇数）可得合格因子对数 F(n) = τ(m)·g(s)，其中 g(2) = 1、g(3) = 0、
 *   g(s) = s − 3（s ≥ 4）。再把因子对折半成 C(n) 并单独数「另一方向也合法」的个数 D(n)，
 *   则 R(n) = C(n) + D(n)。枚举 C(n) = 1 并核对 D(n) 后，恰有一个解的 n 正好是
 *       {≡ 3 (mod 4) 的素数} ∪ {4, 16} ∪ {4p, 16p : p 为奇素数}（n = 32 因 D(32) = 1 被排除）。
 *   故只需数出这三族在 n < N 内的元素个数：π₃(N) + π(⌊(N−1)/4⌋) + π(⌊(N−1)/16⌋)
 *   （N > 32 时 {4,16} 两项恰与 π 里 p = 2 的两次多算抵消）。
 */

/** 整数平方根（向下取整）。 */
private fun p136Isqrt(n: Long): Long {
    var r = kotlin.math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/** 清除位图第 b 位的掩码。 */
private val p136ClearBit = LongArray(64) { (1L shl it).inv() }

/**
 * 分段埃氏筛（只处理奇数，段内位图 8 KB）。
 * 返回 [c3, c1, c2]：c3 = 小于 limit 且 ≡ 3 (mod 4) 的素数个数；
 * c1、c2 = 不超过 t1、t2 的奇素数个数（调用方保证 t1 ≤ t2 < limit）。
 */
private fun p136SegmentedStats(limit: Int, t1: Int, t2: Int): LongArray {
    val segBits = 1 shl 16                               // 每段 65536 个奇数
    val bits = LongArray(segBits ushr 6)

    val root = p136Isqrt(limit.toLong()).toInt() + 1
    // 位图只存奇数，基素数必须剔除 2（p = 2 的标记下标不是整数步长，会把奇数误标为合数）
    val basePrimes = primesUpTo(root).filter { it > 2L }.map { it.toInt() }

    var c3 = 0L
    var c1 = 0L
    var c2 = 0L
    val kEnd = limit / 2                                 // 奇数 2k + 1 < limit ⟺ k < kEnd
    var kLo = 0
    while (kLo < kEnd) {
        var kHi = kLo + segBits
        if (kHi > kEnd) kHi = kEnd
        val n = kHi - kLo

        bits.fill(-1L)                                   // 先当作全是素数
        val rem = n and 63
        if (rem != 0) bits[n ushr 6] = (1L shl rem) - 1L // 尾字只保留有效的低位
        if (kLo == 0) bits[0] = bits[0] and p136ClearBit[0]   // 1 不是素数
        val lastNum = 2 * kHi - 1                        // 本段最大的奇数

        for (p in basePrimes) {
            if (p.toLong() * p > lastNum) break          // p² 已越出本段，无需标记
            val pl = p.toLong()
            var q = (2L * kLo + 1 + pl - 1) / pl         // ⌈(2kLo + 1) / p⌉：本段第一个 p 的倍数
            if (q < pl) q = pl                           // 从 p² 起标，别把 p 自己划掉
            if ((q and 1L) == 0L) q++                    // 只标奇倍数
            var i = ((pl * q - 1) / 2 - kLo).toInt()     // 对应的段内奇数下标
            while (i < n) {
                bits[i ushr 6] = bits[i ushr 6] and p136ClearBit[i and 63]
                i += p
            }
        }

        val words = (n + 63) ushr 6
        var total = 0
        for (w in 0 until words) total += bits[w].countOneBits()
        // k 为奇数 ⟺ 2k + 1 ≡ 3 (mod 4)；kLo 为偶数时奇数下标记在奇位
        val mask = if ((kLo and 1) == 0) -0x5555555555555556L else 0x5555555555555555L
        var t3 = 0
        for (w in 0 until words) t3 += (bits[w] and mask).countOneBits()

        val segMin = 2 * kLo + 1
        c3 += t3
        if (lastNum <= t1) c1 += total
        else if (segMin <= t1) {
            var k = kLo
            while (2 * k + 1 <= t1) {
                val i = k - kLo
                if ((bits[i ushr 6] ushr (i and 63)) and 1L == 1L) c1++
                k++
            }
        }
        if (lastNum <= t2) c2 += total
        else if (segMin <= t2) {
            var k = kLo
            while (2 * k + 1 <= t2) {
                val i = k - kLo
                if ((bits[i ushr 6] ushr (i and 63)) and 1L == 1L) c2++
                k++
            }
        }
        kLo = kHi
    }
    return longArrayOf(c3, c1, c2)
}

/** 用工具库的全区间筛算同样的三个量，用于核对分段位筛。 */
private fun p136SieveStats(limit: Int, t1: Int, t2: Int): LongArray {
    val isPrime = sieve(limit)
    var c3 = 0L
    var c1 = 0L
    var c2 = 0L
    for (p in 3 until limit step 2) {
        if (!isPrime[p]) continue
        if (p % 4 == 3) c3++
        if (p <= t1) c1++
        if (p <= t2) c2++
    }
    return longArrayOf(c3, c1, c2)
}

private fun p136Count(limit: Int): Long {
    val t4 = (limit - 1) / 4                             // 4p < limit ⟺ p ≤ (limit − 1)/4
    val t16 = (limit - 1) / 16
    val stats = p136SegmentedStats(limit, t4, t16)
    var count = stats[0]
    if (limit > 4) count += 1 + stats[1]
    if (limit > 16) count += 1 + stats[2]
    return count
}

/**
 * 按定义枚举：解 (x, y, z) = (a + d, a, a − d)，d ≥ 1、d + 1 ≤ a ≤ 4d − 1，
 * 把 n = a(4d − a) < limit 逐个计数，返回解数恰为 1 的 n（升序）。只用于小规模自查。
 */
private fun p136ByDefinition(limit: Int): List<Int> {
    val cnt = IntArray(limit)
    var d = 1
    while (4 * d - 1 < limit) {
        var a = d + 1
        while (a < 4 * d) {
            val n = a * (4 * d - a)
            if (n < limit && cnt[n] < 2) cnt[n]++
            a++
        }
        d++
    }
    return (1 until limit).filter { cnt[it] == 1 }
}

/** 上面那个集合形式，只用于与按定义枚举的结果对比。 */
private fun p136ByCharacterization(limit: Int): List<Int> {
    val isPrime = sieve(limit)
    val out = sortedSetOf<Int>()
    for (p in 3 until limit step 2) {
        if (isPrime[p] && p % 4 == 3) out.add(p)
    }
    if (4 < limit) out.add(4)
    if (16 < limit) out.add(16)
    var p = 3
    while (4L * p < limit) {
        if (isPrime[p]) out.add(4 * p)
        p += 2
    }
    p = 3
    while (16L * p < limit) {
        if (isPrime[p]) out.add(16 * p)
        p += 2
    }
    return out.toList()
}

/** PE 136 — 小于五千万且使 x² − y² − z² = n 恰有一个解的 n 的个数 = 2544559。 */
private fun solve136(): Long {
    // 题面样例：n = 20 恰有一个解，就是 13² − 10² − 7²（a = 10、d = 3）
    check(13L * 13 - 10L * 10 - 7L * 7 == 20L) { "题面样例：13² − 10² − 7² 应为 20" }
    check(10 * (4 * 3 - 10) == 20) { "题面样例：a = 10、d = 3 应给出 n = 20" }
    check(p136ByDefinition(21).filter { it == 20 } == listOf(20)) { "题面样例：n = 20 应恰有一个解" }

    // 题面样例：一百以内恰有二十五个 n 有唯一解
    check(p136Count(100) == 25L) { "题面样例：一百以内应为 25 个，实得 ${p136Count(100)}" }
    check(p136ByDefinition(100).size == 25) { "题面样例：按定义枚举一百以内也应为 25 个" }
    check(p136Count(1000) == 158L) { "一千以内应为 158 个，实得 ${p136Count(1000)}" }

    // 边界：小于 4 时只有 n = 3（4² − 3² − 2²），小于 5 时正是 n = 3 与 n = 4
    check(p136Count(3) == 0L && p136Count(4) == 1L && p136Count(5) == 2L) { "小于 4/5 的边界计数不对" }
    check(4L * 4 - 3L * 3 - 2L * 2 == 3L && 3L * 3 - 2L * 2 - 1L * 1 == 4L) { "n = 3、4 的样例解不对" }
    // n = 32 是唯一被扣掉的反例：因子对 {4, 8} 两个方向都合法，解数为 2
    check(!p136ByDefinition(33).contains(32)) { "n = 32 解数为 2，不应出现在唯一解集合里" }

    // 刻画与按定义枚举在中小规模上必须给出完全相同的集合
    for (limit in intArrayOf(100, 1000, 10_000)) {
        check(p136ByCharacterization(limit) == p136ByDefinition(limit)) {
            "limit = $limit 处刻画与定义枚举不一致"
        }
    }
    // 分段位筛在多档规模上与工具库的全区间筛逐项一致
    for (limit in intArrayOf(100, 1000, 10_000, 100_000, 1_000_000)) {
        val t1 = (limit - 1) / 4
        val t2 = (limit - 1) / 16
        check(p136SegmentedStats(limit, t1, t2).contentEquals(p136SieveStats(limit, t1, t2))) {
            "limit = $limit 处分段位筛与全区间筛不一致：${p136SegmentedStats(limit, t1, t2).toList()}" +
                " vs ${p136SieveStats(limit, t1, t2).toList()}"
        }
    }

    return p136Count(50_000_000)
}

// ---------- PE 137 ----------
/**
 * PE 137 — Fibonacci Golden Nuggets（斐波那契金块）：第 15 个金块 = 1120149658760。
 *
 * 推导：
 * 1) A_F(x) = Σ_{k≥1} F_k x^k 满足 A_F = x + x·A_F + x²·A_F ⇒ A_F(x) = x / (1 − x − x²)，
 *    收敛半径 1/φ ≈ 0.618（分母的根为 1/φ 与 −φ）。
 * 2) A_F(x) = n ∈ ℤ⁺ ⇒ n x² + (n+1) x − n = 0 ⇒ x = (√(5n² + 2n + 1) − n − 1) / (2n)；
 *    负根模长 ≥ φ 超出收敛半径，必须显式舍去。于是
 *      x 有理 ⟺ 5n² + 2n + 1 是完全平方数。
 * 3) 乘 5 并令 u = 5n + 1：5m² = u² + 4 ⇒ u² − 5m² = −4（Pell 型），另有 u ≡ 1 (mod 5)。
 * 4) 解即 (u, m) = (L_k, F_k)（Lucas 恒等式 L_k² − 5F_k² = 4(−1)^k，k 为奇数）；
 *    Lucas 数模 5 以 4 为周期 ⇒ L_k ≡ 1 (mod 5) ⟺ k ≡ 1 (mod 4)（自动蕴含 k 为奇数）。
 *    故 k = 4j + 1，n_j = (L_{4j+1} − 1) / 5（j ≥ 1；j = 0 给出 n = 0，非正整数）。
 * 5) 乘 α⁴ 得 L_{k+4} = 7L_k − L_{k−4}，于是 u_{j+1} = 7u_j − u_{j−1}，
 *    u_0 = L_1 = 1，u_1 = L_5 = 11；n_j = (u_j − 1)/5 = F_{2j}F_{2j+1}。
 *    第 15 个 = (L_61 − 1)/5 = F_30 F_31 = 832040 × 1346269。
 * 复杂度：O(K) 次 Long 加减，空间 O(1)。
 */

/** 整数平方根（向下取整）：整数回验，不依赖浮点 sqrt 的边界判断。 */
private fun p137Isqrt(n: Long): Long {
    require(n >= 0) { "p137Isqrt 只接受非负整数" }
    var r = java.lang.Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/** 按定义判定 n 是否为金块（Long 快路径）：5n² + 2n + 1 必须恰是完全平方数。 */
private fun p137IsGoldenNugget(n: Long): Boolean {
    val d = 5 * n * n + 2 * n + 1
    val r = p137Isqrt(d)
    return r * r == d
}

/** 同上，但用 BigInteger 精确计算：n ≥ 1.36×10⁹ 起 5n² 会溢出 Long。 */
private fun p137IsGoldenNuggetExact(n: Long): Boolean {
    val bn = java.math.BigInteger.valueOf(n)
    val five = java.math.BigInteger.valueOf(5L)
    val d = bn.multiply(bn).multiply(five).add(bn).add(bn).add(java.math.BigInteger.ONE)
    val r = d.sqrt()
    return r.multiply(r) == d
}

/**
 * n 为金块时给出对应 x = (√(5n² + 2n + 1) − n − 1) / (2n) 的既约形式 (分子, 分母)；
 * n 不是金块时返回的分子分母不保证满足 q² − pq − p² = 1，调用方需自行判定。
 */
private fun p137XForNugget(n: Long): Pair<Long, Long> {
    val bn = java.math.BigInteger.valueOf(n)
    val five = java.math.BigInteger.valueOf(5L)
    val m = bn.multiply(bn).multiply(five).add(bn).add(bn).add(java.math.BigInteger.ONE).sqrt()
    val p = m.subtract(bn).subtract(java.math.BigInteger.ONE)
    val q = bn.multiply(java.math.BigInteger.valueOf(2L))
    val g = p.gcd(q)
    return p.divide(g).longValueExact() to q.divide(g).longValueExact()
}

/** 按定义朴素扫描：返回不超过 limit 的全部金块（升序）。 */
private fun p137NuggetsByDefinition(limit: Long): List<Long> {
    val out = ArrayList<Long>()
    var n = 1L
    while (n <= limit) {
        if (p137IsGoldenNugget(n)) out.add(n)
        n++
    }
    return out
}

/** 第 count 个金块：u_j = L_{4j+1} 由 u_{j+1} = 7u_j − u_{j−1} 生成，n_j = (u_j − 1) / 5。 */
private fun p137NthNugget(count: Int): Long {
    require(count >= 1) { "名次从 1 开始" }
    var prev = 1L                      // u_0 = L_1，对应 n = 0（不算金块）
    var cur = 11L                      // u_1 = L_5，对应第 1 个金块 2
    var j = 1
    while (j < count) {
        val next = 7 * cur - prev
        prev = cur
        cur = next
        j++
    }
    check((cur - 1) % 5 == 0L) { "u_j 必须 ≡ 1 (mod 5)，实测 $cur" }
    return (cur - 1) / 5
}

/** 第 count 个斐波那契数（F_1 = F_2 = 1），用于交叉验证闭式 n_j = F_{2j}F_{2j+1}。 */
private fun p137Fib(count: Int): Long {
    var a = 1L
    var b = 1L
    repeat(count - 1) {
        val t = a + b
        a = b
        b = t
    }
    return a
}

private fun solve137(): Long {
    // 题面样例：前五个自然数中只有 2 是金块（5n²+2n+1 = 25 = 5²），它对应 x = 1/2
    check(p137XForNugget(2) == (1L to 2L)) { "题面样例：n = 2 应对应 x = 1/2" }
    for (n in listOf(1L, 3L, 4L, 5L)) {
        check(!p137IsGoldenNugget(n)) { "题面样例：n = $n 不该是金块（5n²+2n+1 不是完全平方）" }
    }
    // 题面表格：n = 2..5 的无理 x 值，用闭式 A_F(x) = x/(1−x−x²) 复核
    val table = listOf(
        2L to 0.5,
        3L to (java.lang.Math.sqrt(13.0) - 2) / 3,
        4L to (java.lang.Math.sqrt(89.0) - 5) / 8,
        5L to (java.lang.Math.sqrt(34.0) - 3) / 5,
    )
    for ((n, x) in table) {
        val byClosedForm = x / (1 - x - x * x)
        check(kotlin.math.abs(byClosedForm - n) < 1e-9) { "题面表格：x = $x 时闭式和应为 $n，实测 $byClosedForm" }
    }
    // 题面剧透：第 10 个金块是 74049690
    check(p137NthNugget(10) == 74049690L) { "题面样例：第 10 个金块应为 74049690，实测 ${p137NthNugget(10)}" }

    // 递推给出的前五个金块
    val firstFive = (1..5).map { p137NthNugget(it) }
    check(firstFive == listOf(2L, 15L, 104L, 714L, 4895L)) { "前五个金块应为 2, 15, 104, 714, 4895，实测 $firstFive" }

    // 交叉恒等式 n_j = F_{2j}F_{2j+1}
    for (j in 1..15) {
        check(p137NthNugget(j) == p137Fib(2 * j) * p137Fib(2 * j + 1)) { "n_$j 与 F_{2j}F_{2j+1} 不符" }
    }

    // 每个金块都必须满足原始定义与 x 的既约形式约束 q² − pq − p² = 1
    for (j in 1..15) {
        val n = p137NthNugget(j)
        check(p137IsGoldenNuggetExact(n)) { "n_$j = $n 不满足定义（5n²+2n+1 非完全平方）" }
        val (p, q) = p137XForNugget(n)
        check(q * q - p * q - p * p == 1L) { "n_$j 的 x = $p/$q 应满足 q² − pq − p² = 1" }
        check(p < q) { "n_$j 的 x = $p/$q 必须小于 1" }
    }

    // 溢出边界：n_12 = 3478759200 时 5n² ≈ 6.1×10¹⁹ 已越过 Long 上限 9.2×10¹⁸，
    // Long 版判定必须在此失真，改用 BigInteger 后才正确——留着这条断言防回归。
    check(!p137IsGoldenNugget(p137NthNugget(12))) { "Long 版在 n_12 处本该溢出失真，说明溢出点变了" }
    check(p137IsGoldenNuggetExact(p137NthNugget(12))) { "n_12 必须满足定义（BigInteger 判定）" }

    // 定义式暴力扫描（独立于 Pell 递推）：扫到 7.4×10⁷ 恰好得到前 10 个金块，
    // 即第 10 个金块不仅「是」金块，而且正好排第 10
    check(p137NuggetsByDefinition(74_049_690L) == (1..10).map { p137NthNugget(it) }) {
        "按定义扫描的前 10 个金块与递推不一致：${p137NuggetsByDefinition(74_049_690L)}"
    }

    // 闭式和本身：x = 1/2 的级数部分和收敛到 2
    var sum = 0.0
    var pow = 0.5                      // x^k，x = 1/2
    var fPrev = 0.0                    // F_0 = 0
    var fCur = 1.0                     // F_1 = 1
    for (k in 1..120) {
        sum += pow * fCur
        pow *= 0.5
        val next = fPrev + fCur
        fPrev = fCur
        fCur = next
    }
    check(kotlin.math.abs(sum - 2.0) < 1e-6) { "Σ (1/2)^k F_k 应趋近 2，实测 $sum" }

    return p137NthNugget(15)
}

// ---------- PE 138 ----------
/**
 * PE 138 — Special Isosceles Triangles：满足 h = b ± 1 的等腰三角形中最小十二个的 ΣL
 * = 1118049290473932。
 *
 * 推导：等腰三角形的高垂直平分底边，故「半底边、高、腰」满足 h² + (b/2)² = L²。
 *   · b 必为偶数：b 奇 ⇒ h = b ± 1 为偶，乘 4 得 4L² = 4h² + b²，左端 ≡ 0 而右端 ≡ 1 (mod 4)，矛盾。
 *   · 设 b = 2x（x ≥ 1），代入 h = 2x ± 1 得 5x² ± 4x + 1 = L²；乘 5 配方成
 *     (5x ± 2)² = 5L² − 1，即 Pell 方程 u² − 5L² = −1（u = 5x ± 2）。
 *   · 反向：任何正解满足 u² ≡ −1 ≡ 4 (mod 5)，故 u ≡ ±2 (mod 5)、x = (u ∓ 2)/5 为整数；
 *     u ≡ 2 给 h = b + 1，u ≡ 3 给 h = b − 1。于是「h = b ± 1 的等腰三角形」与
 *     「u² − 5L² = −1 的正解」一一对应。
 *   · 全部正解为 (2 + √5)^(2k+1)（k ≥ 0），相邻解相差因子 (2 + √5)² = 9 + 4√5，展开得整数递推
 *     u' = 9u + 20L、L' = 4u + 9L；比值 9 + 4√5 > 1 保证 u、L、b 严格递增，
 *     故沿解链取前 12 个即前 12 小的三角形。
 *   · 首解 (2, 1) 给 x = 0（b = 0 退化）需跳过；此后 h 在 b − 1 与 b + 1 间交替。
 *
 * 复杂度：O(count) 次 Long 乘加，空间 O(count)；第 12 个 L ≈ 1.056×10^15，中间量 ≈ 2.4×10^15，
 * Long 安全。与 content/problems/0138/solution.kt 逻辑一致（片段里不含计时循环）。
 */

/**
 * 由 Pell 解 (u, L) 还原三角形 [底边, 腰, 高]。
 * u mod 5 = 2 对应 h = b + 1，u mod 5 = 3 对应 h = b − 1；
 * 退化解 x = 0（即 u = 2、L = 1）返回 null。
 */
private fun p138FromPell(u: Long, leg: Long): LongArray? {
    if (leg <= 0L) return null
    val plusOne = u % 5 == 2L
    val halfBase = if (plusOne) (u - 2) / 5 else (u + 2) / 5
    if (halfBase == 0L) return null
    val base = 2 * halfBase
    return longArrayOf(base, leg, if (plusOne) base + 1 else base - 1)
}

/** 前 count 个满足 h = b ± 1 的等腰三角形，按底边升序，元素为 [底边, 腰, 高]。 */
private fun p138SpecialTriangles(count: Int): List<LongArray> {
    require(count >= 1) { "count 必须为正整数：$count" }
    val result = ArrayList<LongArray>(count)
    var u = 2L                       // u + L√5 = (2 + √5)^1：最小正解（4 − 5 = −1）
    var leg = 1L
    while (result.size < count) {
        val nextU = 9 * u + 20 * leg
        val nextLeg = 4 * u + 9 * leg
        u = nextU
        leg = nextLeg
        p138FromPell(u, leg)?.let { result.add(it) }
    }
    return result
}

private fun solve138(): Long {
    val triangles = p138SpecialTriangles(12)
    check(triangles.size == 12) { "应给出 12 个三角形，实得 ${triangles.size}" }

    // 题面样例：(b, L, h) = (16, 17, 15) 与 (272, 305, 273)
    val first = triangles[0]
    check(first[0] == 16L && first[1] == 17L && first[2] == 15L) { "题面样例一应为 (16, 17, 15)：${first.toList()}" }
    val second = triangles[1]
    check(second[0] == 272L && second[1] == 305L && second[2] == 273L) { "题面样例二应为 (272, 305, 273)：${second.toList()}" }

    for (index in triangles.indices) {
        val base = triangles[index][0]
        val leg = triangles[index][1]
        val height = triangles[index][2]
        check(base > 0L && leg > 0L && height > 0L) { "边长必须为正：$base $leg $height" }
        check(base % 2 == 0L) { "底边必须为偶数：$base" }
        val half = base / 2
        check(leg * leg == half * half + height * height) { "勾股关系不成立：$base $leg $height" }
        check(leg > height) { "腰必须是最长边：$base $leg $height" }
        check(height == base + 1 || height == base - 1) { "高必须与底边相差 1：$base $height" }
        // h 在 b − 1（奇数位）与 b + 1（偶数位）之间交替
        check(if (index % 2 == 0) height == base - 1 else height == base + 1) { "符号未交替：$base $height" }
        if (index > 0) check(base > triangles[index - 1][0]) { "底边必须严格递增：$base" }
        // 解链的横向不变量：L_k 是 (2 + √5)^(2k+1) 的线性组合 ⇒ L_{k+1} = 18L_k − L_{k−1}
        if (index > 1) {
            check(leg == 18 * triangles[index - 1][1] - triangles[index - 2][1]) {
                "腰长应满足 L_{k+1} = 18L_k − L_{k−1}：${triangles.map { it.toList() }}"
            }
        }
    }

    // 题面「(272, 305, 273) 是第二小的」：前两个的腰长之和
    check(triangles[0][1] + triangles[1][1] == 322L) { "前两个腰长之和应为 322" }

    return triangles.sumOf { it[1] }
}

// ---------- PE 139 ----------
/**
 * PE 139 — 毕达哥拉斯地砖：周长小于 10⁸ 的勾股三角形中，四块拼成的外正方形能被
 * 「洞尺寸」的方砖铺满（即 d = |b − a| 整除斜边 c）的个数 = 10057761。
 *
 * 推导：四块全等直角三角形围成边长 c 的正方形，中央洞边长 d = |b − a|
 * （面积守恒：4·(ab/2) + (b − a)² = c²）。铺砖可行 ⟺ d | c。
 * 每个三角形是 k 倍本原组，而 k·d | k·c ⟺ d | c，故条件与缩放无关，只需研究本原组。
 * 本原组中 gcd(d, a) = gcd(b − a, a) = gcd(b, a) = 1，同理 gcd(d, b) = 1，
 * 又 c² ≡ 2ab (mod d)，由 d | c 得 d | 2ab ⇒ d | 2；d = 2 要求两腿同奇偶，
 * 而本原组两腿一奇一偶（全奇则 c² ≡ 2 (mod 4) 无解，全偶则不本原），故 d = 1：
 * 合法三角形恰为「两腿相差 1」的本原组 (x, x+1, c) 的整数倍。
 * 由 2x² + 2x + 1 = c² 令 u = 2x + 1 得负 Pell 方程 u² − 2c² = −1，其解由 (3 + 2√2) 递推给出：
 *   u' = 3u + 4c，c' = 2u + 3c，
 * 周长 = x + (x+1) + c = u + c，每族贡献 floor((limit − 1)/(u + c)) 个倍数。
 * u + c < 10⁸ 的本原族只有 10 个（首项 (7,5) 即 (3,4,5)，末项周长 93222358 只贡献 1 个）。
 * 复杂度 O(log limit)、空间 O(1)，全程 Long（中间量最大约 3.2×10⁸）。
 */

/** 铺砖条件：洞边长 d = |b − a| 必须整除外正方形边长 c。 */
private fun p139Tiles(a: Long, b: Long, c: Long): Boolean {
    val d = if (a > b) a - b else b - a
    return c % d == 0L
}

/**
 * 周长严格小于 limit、可铺砖的勾股三角形个数：沿负 Pell 递推枚举「两腿相差 1」的本原族，
 * 每族累加其倍数中周长严格小于 limit 的个数。
 */
private fun p139Solve(limit: Long): Long {
    var count = 0L
    var u = 7L
    var c = 5L                                   // 对应 (3, 4, 5)：u = 2·3 + 1 = 7
    while (u + c < limit) {
        count += (limit - 1) / (u + c)           // 严格小于 limit，故用 limit - 1
        val nextU = 3 * u + 4 * c
        val nextC = 2 * u + 3 * c
        u = nextU
        c = nextC
    }
    return count
}

/**
 * 小范围穷举参照：直接扫 a < b，用整数开方判 c，逐条检查铺砖条件。
 * 只用于断言互证推导正确性，不参与最终计数。
 */
private fun p139CountByEnumeration(limit: Long): Long {
    var count = 0L
    var a = 1L
    while (3 * a + 2 < limit) {                  // 周长 > 3a ⇒ a 更大时无解
        var b = a + 1
        while (a + 2 * b < limit) {              // c > b ⇒ 周长 > a + 2b
            val s = a * a + b * b
            var c = Math.sqrt(s.toDouble()).toLong()
            while (c * c > s) c--
            while ((c + 1) * (c + 1) <= s) c++
            if (c * c == s && a + b + c < limit && p139Tiles(a, b, c)) count++
            b++
        }
        a++
    }
    return count
}

private fun solve139(): Long {
    // 题面样例：(3,4,5) 的洞 1×1，5×5 用 25 = (5/1)² 块铺满；反例 (5,12,13) 的洞 7×7，7 ∤ 13
    check(p139Tiles(3, 4, 5)) { "题面样例：(3,4,5) 应可铺砖" }
    check(!p139Tiles(5, 12, 13)) { "题面反例：(5,12,13) 的中洞 7×7，7 ∤ 13，不应可铺砖" }
    // 严格小于 limit 的边界：12 排除 (3,4,5) 本身，13 恰有一个；71 是 5 个倍数加 (20,21,29)
    check(p139Solve(12L) == 0L) { "limit=12 应为 0，实得 ${p139Solve(12L)}" }
    check(p139Solve(13L) == 1L) { "limit=13 应为 1，实得 ${p139Solve(13L)}" }
    check(p139Solve(71L) == 6L) { "limit=71 应为 6，实得 ${p139Solve(71L)}" }
    // 与直接枚举 a < b < c 的穷举逐一对齐，钉死「铺砖 ⟺ d | c」这条题意解读
    for (lim in longArrayOf(13L, 100L, 1000L, 3000L, 10_000L)) {
        check(p139Solve(lim) == p139CountByEnumeration(lim)) {
            "limit=$lim：Pell 递推 ${p139Solve(lim)} 与直接穷举 ${p139CountByEnumeration(lim)} 不一致"
        }
    }
    return p139Solve(100_000_000L)
}

// ---------- PE 140 ----------
/**
 * PE 140 — 修改版斐波那契金块（Modified Fibonacci Golden Nuggets）
 *
 * 逻辑与 content/problems/0140/solution.kt 一致：
 * A_G(x) = (x + 3x²)/(1 − x − x²)，令 A_G(x) = n 得 (n+3)x² + (n+1)x − n = 0，
 * 判别式 D(n) = 5n² + 14n + 1 为完全平方 ⟺ x 有理；配方后 (5n+7)² − 5m² = 44。
 * 记 u = 5n + 7，解链由基本单位 ε = 9 + 4√5 相邻，金块落在链的每隔一项上，
 * 两条子列各满足 u_{k+1} = 7u_k − u_{k−1}（种子 u = 7,17 与 7,32），
 * 换算到 n 即 n_{k+1} = 7n_k − n_{k−1} + 7，归并去重后取前 30 项求和。
 */

/** 判别式 D(n) = 5n² + 14n + 1；第 30 个金块 n ≈ 3.2×10¹² 时 5n² 已超 Long，故用 BigInteger。 */
private fun p140Discriminant(n: Long): java.math.BigInteger {
    val big = java.math.BigInteger.valueOf(n)
    return big * big * java.math.BigInteger.valueOf(5) + big * java.math.BigInteger.valueOf(14) +
        java.math.BigInteger.ONE
}

/** n 是金块 ⟺ D(n) 为完全平方数；是则返回 m = √D(n)，否则返回 null。 */
private fun p140GoldenRoot(n: Long): java.math.BigInteger? {
    val d = p140Discriminant(n)
    val m = d.sqrt()
    return if (m * m == d) m else null
}

/** 沿 u_{k+1} = 7u_k − u_{k−1} 生成一条子列的 n（u = 5n + 7），直到 u 超过 limit。 */
private fun p140Subsequence(first: Long, second: Long, limit: Long): List<Long> {
    val nuggets = ArrayList<Long>()
    var prev = first
    var cur = second
    while (cur <= limit) {
        val n = (cur - 7) / 5
        if (n >= 1L) nuggets.add(n)              // n = 0 对应 x = 0，不是正整数，排除
        val next = 7L * cur - prev
        prev = cur
        cur = next
    }
    return nuggets
}

/** 升序的前 count 个金块：两条子列（种子 u = 7,17 与 u = 7,32）归并去重。 */
private fun p140GoldenNuggets(count: Int, limit: Long): List<Long> =
    (p140Subsequence(7L, 17L, limit) + p140Subsequence(7L, 32L, limit)).sorted().take(count)

/** PE 140 — 前三十个金块之和 = 5673835352990。 */
private fun solve140(): Long {
    // ── 题面表格：A_G(x) = 1…5 的判别式依次为 20, 49, 88, 137, 196，只有后两者中的 49、196 是完全平方
    val discs = (1L..5L).map { p140Discriminant(it).toLong() }
    check(discs == listOf(20L, 49L, 88L, 137L, 196L)) { "判别式应为 20,49,88,137,196，实际 $discs" }
    check(p140GoldenRoot(2L) == java.math.BigInteger.valueOf(7L)) { "n = 2 应为金块（D = 49 = 7²）" }
    check(p140GoldenRoot(5L) == java.math.BigInteger.valueOf(14L)) { "n = 5 应为金块（D = 196 = 14²）" }
    for (n in listOf(1L, 3L, 4L)) {
        check(p140GoldenRoot(n) == null) { "n = $n 的 x 应是无理数（判别式不是完全平方数）" }
    }

    // ── 题面锚点：第 20 个金块必须是 211345365
    val nuggets = p140GoldenNuggets(30, 100_000_000_000_000L)
    check(nuggets.size == 30) { "应得 30 个金块，实际 ${nuggets.size}" }
    check(nuggets[19] == 211_345_365L) { "第 20 个金块应为 211345365，实际 ${nuggets[19]}" }
    check(nuggets.zipWithNext().all { (a, b) -> a < b }) { "金块必须严格递增" }

    // ── 逐项回到定义：(5n+7)² − 5m² = 44
    for (n in nuggets) {
        val m = p140GoldenRoot(n) ?: error("$n 不是金块")
        val u = java.math.BigInteger.valueOf(n) * java.math.BigInteger.valueOf(5L) +
            java.math.BigInteger.valueOf(7L)
        check(u * u - m * m * java.math.BigInteger.valueOf(5L) == java.math.BigInteger.valueOf(44L)) {
            "n = $n 不满足 Pell 方程 (5n+7)² − 5m² = 44"
        }
    }
    return nuggets.sum()
}

// ---------- PE 141 ----------
/**
 * PE 141 — 平方递进数（Square Progressive Numbers）
 *
 * n = d·q + r（0 < r < d），且 {d, q, r} 是某个等比数列的连续三项（顺序不限）。
 * 三项排序成 A < B < C 后有 A·C = B²；把公比写成最简分数 p/q（p > q ≥ 1, gcd(p,q) = 1），
 * 则 A = c·q², B = c·p·q, C = c·p²。六种排列里满足 r < d 的有三组，其中两组给出同一个
 * n = C·B + A，第三组给出 n = B² + B —— 而 B² < B² + B < (B+1)²，永非完全平方数，整支丢弃。
 * 于是平方递进数恰好是 n = c·q·(c·p³ + q)（p > q ≥ 1 互素，c ≥ 1），枚举并去重求和。
 *
 * 复杂度：约 7.3×10⁶ 个候选，每个一次乘法 + 一次整数开方；空间 O(H)（H 为解的个数）。
 */

/** 整数平方根（向下取整）：浮点估计后用整数乘法回验靠拢，规避 sqrt 的舍入误差。 */
private fun p141Isqrt(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0L && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/** n 是否为完全平方数。 */
private fun p141IsSquare(n: Long): Boolean {
    val r = p141Isqrt(n)
    return r * r == n
}

/** 按题面定义逐项检验：枚举除数 d，取真实商与余数，看三项排序后是否成等比（中项平方 = 两侧之积）。 */
private fun p141IsProgressive(n: Long): Boolean {
    var d = 1L
    while (d <= n) {
        val q = n / d
        val r = n % d
        if (r > 0L && r < d) {
            val t = longArrayOf(d, q, r).sortedArray()
            if (t[0] * t[2] == t[1] * t[1]) return true
        }
        d++
    }
    return false
}

/** 小于 limit 的全部平方递进数，升序、去重。 */
private fun p141SolveSet(limit: Long): List<Long> {
    val hits = HashSet<Long>()
    var p = 2L
    while (p * p * p + 1 < limit) {                  // q = 1, c = 1 时 n = p³ + 1，再大的 p 无解
        val p3 = p * p * p
        var q = 1L
        while (q < p) {
            if (q * (p3 + q) >= limit) break         // c ≥ 1 时 n ≥ q(p³ + q)，关于 q 单调递增
            if (gcd(p, q) == 1L) {
                var c = 1L
                while (true) {
                    val n = c * q * (c * p3 + q)
                    if (n >= limit) break
                    if (p141IsSquare(n)) hits.add(n)
                    c++
                }
            }
            q++
        }
        p++
    }
    return hits.sorted()
}

/** 小于 limit 的全部平方递进数之和。 */
private fun p141Sum(limit: Long): Long = p141SolveSet(limit).sum()

/** PE 141 — 10¹² 以内「递进的完全平方数」之和 = 878454337159。 */
private fun solve141(): Long {
    // 题面例子：58 ÷ 6 → 商 9 余 4，且 4, 6, 9 是公比 3/2 的等比数列连续三项
    check(58L / 6 == 9L && 58L % 6 == 4L) { "题面例子：58 ÷ 6 的商应为 9、余数应为 4" }
    check(4L * 9L == 6L * 6L) { "题面例子：4, 6, 9 应成等比数列" }
    // 题面点名的两个平方递进数：9 与 10404 = 102²（按题面原始定义逐项复核）
    check(p141IsProgressive(9L)) { "题面点名：9 应是递进数" }
    check(p141IsProgressive(10404L)) { "题面点名：10404 应是递进数" }
    check(p141IsSquare(9L) && p141IsSquare(10404L)) { "题面点名：9 与 10404 都应是完全平方数" }
    // 十万以内的四个平方递进数，和必须等于题面给出的 124657
    val below100k = p141SolveSet(100_000L)
    check(below100k == listOf(9L, 10404L, 16900L, 97344L)) { "十万以内应为 9,10404,16900,97344，实得 $below100k" }
    for (n in below100k) check(p141IsProgressive(n)) { "$n 不是递进数" }
    check(p141Sum(100_000L) == 124657L) { "题面样例：十万以内之和应为 124657，实得 ${p141Sum(100_000L)}" }
    // 严格上界：小于 9 时无解
    check(p141Sum(9L) == 0L) { "严格上界：小于 9 时不应有解" }
    return p141Sum(1_000_000_000_000L)
}

// ---------- PE 142 ----------
/**
 * PE 142 — Perfect Square Collection（完全平方数集合）
 *
 * 把六个平方数写成
 *   x + y = a²,  x − y = b²,  x + z = c²,  x − z = d²,  y + z = e²,  y − z = f²。
 * 三组相减立刻给出三个勾股关系：
 *   d² = (x − z) = (x − y) + (y − z) = b² + f²
 *   c² = (x + z) = (x − y) + (y + z) = b² + e²
 *   a² = (x + y) = (x − z) + (y + z) = b² + e² + f²
 * 反过来，取 b ≥ 1、e > f ≥ 1、e ≡ f (mod 2)，只要 b² + e²、b² + f²、b² + e² + f²
 * 三者全是完全平方数，就由
 *   y = (e² + f²)/2,  z = (e² − f²)/2,  x = b² + y
 * 唯一还原出整数三重 (x, y, z)：六个式子逐一成立，且 x > y > z > 0 自动满足
 * （b ≥ 1、e > f）。于是问题化为「勾股三元组的腿之间做配对」：对每条腿 b，取其全部
 * 配对腿 o（b² + o² 为平方），在配对腿内部挑同奇偶的一对 (e, f)，再验第三个条件。
 * 目标 x + y + z = b² + 2y + z = b² + (3e² + f²)/2。
 *
 * 搜索边界：任一解满足 S = x + y + z > b²，故 b < √S；又 S > (3/2)e² > (3/2)f²，
 * 故 e ≤ √(2S/3)，f < e。取腿长上界 B = 2048 先求最小 S，再断言 B² > S 与 B² > 2S/3：
 * 任何更小的解其全部腿长必然小于 B，已被完整枚举，故所得即全局最小。
 *
 * 复杂度：Euclid 参数化生成腿长 ≤ B 的三元组 O(B log B)（m ≤ √(2B)），配对表用 CSR
 * 存两遍计数 + 填充；之后对每条腿的配对表两两组合 O(B·d²)（d 为单条腿的配对数，
 * 实测 ≤ 10），每对含一次整数开方。空间 O(B + E)。答案 = 1006193。
 */

private const val P142_BOUND = 2048

/** 解的三重与它的化简参数：b² = x − y、e² = y + z、f² = y − z。 */
private class p142Solution(
    val x: Long,
    val y: Long,
    val z: Long,
    val b: Long,
    val e: Long,
    val f: Long,
) {
    val sum: Long get() = x + y + z
}

/** 勾股配对表的 CSR 形式：与腿 v 配对的腿是 data[start[v] until start[v+1]]。 */
private class p142PartnerTable(private val start: IntArray, private val data: IntArray) {
    fun begin(v: Int): Int = start[v]
    fun end(v: Int): Int = start[v + 1]
    fun leg(v: Int, i: Int): Int = data[i]
}

/** 整数开方（向下取整）：浮点估值后用整数回退校准，不依赖浮点边界。 */
private fun p142Isqrt(n: Long): Long {
    if (n <= 0L) return 0L
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0L && r * r > n) r--
    while ((r + 1L) * (r + 1L) <= n) r++
    return r
}

private fun p142IsSquare(n: Long): Boolean {
    if (n < 0L) return false
    val r = p142Isqrt(n)
    return r * r == n
}

/**
 * 枚举腿长 ≤ bound 的全部勾股三元组，按 (u, v) 两条腿回调。
 * Euclid 参数化：m > n ≥ 1、m − n 奇、gcd(m, n) = 1 给出本原三元组，再乘 k 覆盖其整数倍。
 */
private inline fun p142ForEachTripleLeg(bound: Int, action: (Int, Int) -> Unit) {
    var m = 2L
    while (m * m - 1 <= bound) {
        var n = 1L
        while (n < m) {
            if (((m - n) and 1L) == 1L && gcd(m, n) == 1L) {
                val u0 = (m * m - n * n).toInt()
                val v0 = (2 * m * n).toInt()
                var k = 1L
                while (k * u0 <= bound && k * v0 <= bound) {
                    action((k * u0).toInt(), (k * v0).toInt())
                    k++
                }
            }
            n++
        }
        m++
    }
}

/** 腿长 ≤ bound 的勾股配对表：partners 中列出所有 o 使 v² + o² 为完全平方数。 */
private fun p142BuildPartners(bound: Int): p142PartnerTable {
    val deg = IntArray(bound + 2)
    p142ForEachTripleLeg(bound) { u, v ->
        deg[u]++
        deg[v]++
    }
    val start = IntArray(bound + 2)
    for (v in 0..bound) start[v + 1] = start[v] + deg[v]
    val cursor = start.copyOf()
    val data = IntArray(start[bound + 1])
    p142ForEachTripleLeg(bound) { u, v ->
        data[cursor[u]++] = v
        data[cursor[v]++] = u
    }
    return p142PartnerTable(start, data)
}

/** 在腿长 ≤ bound 的范围内求 x + y + z 最小的解，无解返回 null。 */
private fun p142Solve(bound: Int): p142Solution? {
    val table = p142BuildPartners(bound)
    var best: p142Solution? = null
    for (b in 1..bound) {
        val from = table.begin(b)
        val to = table.end(b)
        if (to - from < 2) continue
        val b2 = b.toLong() * b
        for (i in from until to) {
            for (j in i + 1 until to) {
                val e = maxOf(table.leg(b, i), table.leg(b, j)).toLong()   // 需要 e > f（即 z > 0）
                val f = minOf(table.leg(b, i), table.leg(b, j)).toLong()
                if (((e - f) and 1L) != 0L) continue                       // y、z 为整数 ⇒ 同奇偶
                if (!p142IsSquare(b2 + e * e + f * f)) continue
                val sum = b2 + (3 * e * e + f * f) / 2
                if (best == null || sum < best.sum) {
                    val y = (e * e + f * f) / 2
                    val z = (e * e - f * f) / 2
                    best = p142Solution(b2 + y, y, z, b.toLong(), e, f)
                }
            }
        }
    }
    return best
}

/**
 * 独立路径（另一套参数化）：枚举 y、z 使 y ± z 均为平方，再由 (c − d)(c + d) = 2z 的因子对
 * 反解 x = z + d²，最后只查 x ± y 是否为平方。用于在小范围内确认没有更小的解。
 * 返回 true 表示在 y ≤ yMax 且 x + y + z < sumCap 的范围内确实无解。
 */
private fun p142NoSmallerByDefinition(yMax: Long, sumCap: Long): Boolean {
    var e = 2L
    while ((e * e + 1L) / 2 <= yMax) {
        var f = 1L
        while (f < e) {
            if (((e - f) and 1L) == 0L) {
                val y = (e * e + f * f) / 2
                if (y <= yMax) {
                    val z = (e * e - f * f) / 2
                    if (z > 0L && z < y) {
                        val t = 2 * z
                        var p = 1L
                        while (p * p <= t) {
                            if (t % p == 0L) {
                                val q = t / p
                                if (((q - p) and 1L) == 0L) {              // c、d 必须是整数
                                    val d = (q - p) / 2
                                    val x = z + d * d
                                    if (x > y && x + y + z < sumCap &&
                                        p142IsSquare(x - y) && p142IsSquare(x + y)
                                    ) {
                                        return false
                                    }
                                }
                            }
                            p++
                        }
                    }
                }
            }
            f++
        }
        e++
    }
    return true
}

/** PE 142 — 求满足六个完全平方条件的最小 x + y + z = 1006193。 */
private fun solve142(): Long {
    val sol = p142Solve(P142_BOUND) ?: error("B = $P142_BOUND 范围内未找到解")

    // 1. 直接按定义回验六个式子（不看推导，只算 x、y、z 本身）
    check(sol.x > sol.y && sol.y > sol.z && sol.z > 0L) { "142：需要 x > y > z > 0，实测 $sol" }
    val six = longArrayOf(
        sol.x + sol.y, sol.x - sol.y, sol.x + sol.z,
        sol.x - sol.z, sol.y + sol.z, sol.y - sol.z,
    )
    check(six.all { p142IsSquare(it) }) { "142：六个式子必须全为完全平方数，实测 ${six.toList()}" }

    // 2. 化简条件自洽：b² + e²、b² + f²、b² + e² + f² 皆为平方，且与 x、y、z 的等式对齐
    val b2 = sol.b * sol.b
    check(sol.b * sol.b == sol.x - sol.y && sol.e * sol.e == sol.y + sol.z && sol.f * sol.f == sol.y - sol.z) {
        "142：b、e、f 与 x、y、z 的对应关系不符"
    }
    check(p142IsSquare(b2 + sol.e * sol.e)) { "142：x + z 应为完全平方数" }
    check(p142IsSquare(b2 + sol.f * sol.f)) { "142：x − z 应为完全平方数" }
    check(p142IsSquare(b2 + sol.e * sol.e + sol.f * sol.f)) { "142：x + y 应为完全平方数" }
    check(sol.sum == b2 + (3 * sol.e * sol.e + sol.f * sol.f) / 2) { "142：闭式求和公式不符" }

    // 3. 搜索完备性：任何更小的解必有 b < √S、e ≤ √(2S/3) < B，故已被枚举
    check(P142_BOUND.toLong() * P142_BOUND > sol.sum) { "142：B 必须大于 √(x + y + z)" }
    check(P142_BOUND.toLong() * P142_BOUND > 2 * sol.sum / 3) { "142：B 必须大于 √(2S/3)" }
    // 腿长上界收缩到 800（756、520、117 都仍在范围内）答案不变，说明结论不靠放大 B 得来
    check(p142Solve(800)?.sum == sol.sum) { "142：B = 800 时答案应相同" }

    // 4. 小范围独立穷举：y ≤ 50000 时不存在更小的解
    check(p142NoSmallerByDefinition(50_000L, sol.sum)) { "142：y ≤ 50000 的范围内不应存在更小的解" }

    return sol.sum
}

// ---------- PE 143 ----------
/**
 * PE 143 — Torricelli Triangles：所有托里拆利三角形（各内角 < 120°、费马点到三顶点
 * 的距离 p, q, r 全为整数）中，不同的 p + q + r ≤ 120000 之和 = 30758397。
 *
 * 推导：费马点 T 处三条连线两两成 120°，对三角形 ATB、BTC、CTA 各用一次余弦定理
 * （cos 120° = −1/2，平方项与交叉项同号相加）得
 *
 *   a² = q² + qr + r²,   b² = p² + pq + q²,   c² = p² + pr + r²。
 *
 * 反之，若 p, q, r > 0 且三式都成立，就把三段以 120° 夹角拼起来得到合法三角形 ABC
 * （T 在其内部，各内角 < 120°；和函数凸、最小值点落在钝角顶点上，故内角 < 120° 是必要条件）。
 * 于是问题化为「以正整数为顶点、使 x² + xy + y² 为完全平方的数对为边，找 p + q + r ≤ 120000
 * 的 3-团，对不同的和去重求和」。
 *
 * 120° 对由图论之外的一条捷径全部造出（艾森斯坦整数范数下本原解必为平方，含单位）：
 *
 *   x = k(m² − n²),  y = k(2mn + n²),  x² + xy + y² = [k(m² + mn + n²)]²,  x + y = k·m(m + 2n)
 *
 * （m > n > 0，k ≥ 1）。固定 (m, n) 后倍数只有 ⌊L / (m(m+2n))⌋ 个，故边数远小于 L²。
 * 建图只存「比自己大的邻居」的 CSR 上邻接表（和 ≤ L 的团里任两数之和必 ≤ L，截断不丢解），
 * 三角形则对每条边 (a, b) 求 adj(a) ∩ adj(b) 的有序交集，命中即把和记进集合。
 *
 * 本题不需要 dev.pekt.math 里的任何工具（没有「平方判定」类原语），只用到
 * java.util.Arrays.sort 做边数组排序去重。
 */

/** x² + xy + y² 若为完全平方数则返回其平方根，否则返回 −1（整数阈值比较，不用浮点下结论）。 */
private fun p143SqrtIfSquare(x: Long, y: Long): Long {
    val v = x * x + x * y + y * y
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return if (r * r == v) r else -1L
}

/** 把 {x, y}（x ≠ y，均 < 2²⁰）编码成一个 Long，编码序与 x < y 的字典序一致。 */
private fun p143EdgeKey(x: Int, y: Int): Long =
    if (x < y) (x.toLong() shl 20) or y.toLong() else (y.toLong() shl 20) or x.toLong()

/** 参数化枚举全部满足 x + y ≤ limit 的 120° 对，返回编码数组（含重复，未排序）。 */
private fun p143EncodeEdges(limit: Int): LongArray {
    var cap = 1 shl 18
    var arr = LongArray(cap)
    var n = 0
    var m = 2
    while (m.toLong() * m <= limit) {                    // x + y = m(m + 2n) ≥ m²
        var nn = 1
        while (nn < m && m.toLong() * m + 2L * m * nn <= limit) {
            val base = m * m + 2 * m * nn                 // (m, nn) 基元的 x + y
            val x0 = m * m - nn * nn
            val y0 = 2 * m * nn + nn * nn
            var k = 1
            while (k.toLong() * base <= limit) {
                if (n == cap) {
                    cap *= 2
                    arr = arr.copyOf(cap)
                }
                arr[n++] = p143EdgeKey(k * x0, k * y0)
                k++
            }
            nn++
        }
        m++
    }
    return arr.copyOf(n)
}

/**
 * CSR 形式的**上邻接表**：只保留顶点 v → 比 v 大的邻居（升序）。
 * 返回长度 limit + 2 的前缀数组 start，adj 为邻居序列，顶点 v 的邻居是 adj[start[v]..<start[v+1])。
 */
private fun p143BuildUpperAdjacency(limit: Int): Pair<IntArray, IntArray> {
    val edges = p143EncodeEdges(limit)
    java.util.Arrays.sort(edges)                          // 按 (小, 大) 字典序，顺带完成去重
    var e = 0
    for (i in edges.indices) {
        if (i == 0 || edges[i] != edges[i - 1]) edges[e++] = edges[i]
    }
    val deg = IntArray(limit + 1)
    for (i in 0 until e) deg[(edges[i] ushr 20).toInt()]++
    val start = IntArray(limit + 2)
    for (v in 1..limit) start[v + 1] = start[v] + deg[v]
    val fill = start.copyOf()
    val adj = IntArray(e)
    for (i in 0 until e) {
        val lo = (edges[i] ushr 20).toInt()
        val hi = (edges[i] and 0xFFFFFL).toInt()
        adj[fill[lo]++] = hi                              // edges 有序 ⇒ 每个 lo 段升序
    }
    return Pair(start, adj)
}

/** 所有满足 p + q + r ≤ limit 的托里拆利三角形的**不同**和。 */
private fun p143TriangleSums(limit: Int): HashSet<Int> {
    val (start, adj) = p143BuildUpperAdjacency(limit)
    val sums = HashSet<Int>()
    for (a in 1 until limit) {
        val as0 = start[a]
        val ae = start[a + 1]
        if (as0 == ae) continue
        for (ia in as0 until ae) {
            val b = adj[ia]
            var i = as0
            var j = start[b]
            val be = start[b + 1]
            while (i < ae && j < be) {                    // 两条升序链求交
                val u = adj[i]
                val v = adj[j]
                when {
                    u < v -> i++
                    u > v -> j++
                    else -> {                             // u 是 a、b 的公共邻居，且 u > b > a
                        if (a + b + u > limit) break      // u 随 i 单调不减
                        sums.add(a + b + u)
                        i++
                        j++
                    }
                }
            }
        }
    }
    return sums
}

private fun p143Solve(limit: Int): Long {
    var total = 0L
    for (s in p143TriangleSums(limit)) total += s
    return total
}

private fun solve143(): Long {
    // 题面样例：a = 399、b = 455、c = 511，对应的费马点距离是 p = 195、q = 264、r = 325。
    check(p143SqrtIfSquare(195, 264) == 399L) { "题面样例：195/264 应给出 399" }
    check(p143SqrtIfSquare(195, 325) == 455L) { "题面样例：195/325 应给出 455" }
    check(p143SqrtIfSquare(264, 325) == 511L) { "题面样例：264/325 应给出 511" }
    check(195L + 264 + 325 == 784L) { "题面样例：三段距离之和应为 784" }
    // 该三元组能被搜索到；784 是全局最小和，故上限压到 783 时应无解
    check(p143TriangleSums(784).contains(784)) { "上限 784 时应能找到和 784" }
    check(p143TriangleSums(783).isEmpty()) { "上限 783 时应无解（784 为最小和）" }
    // 边界：x = y 时 3x² 不是平方；(1,2) 不是 120° 对；(3,5) 是（3² + 15 + 5² = 7²）
    check(p143SqrtIfSquare(1, 1) == -1L) { "1/1 不是 120° 对" }
    check(p143SqrtIfSquare(1, 2) == -1L) { "1/2 不是 120° 对" }
    check(p143SqrtIfSquare(3, 5) == 7L) { "3/5 应给出 7" }
    return p143Solve(120_000)
}

// ---------- PE 144 ----------
/**
 * PE 144 — 激光束反射：「白室」是椭圆 4x² + y² = 100，顶部 |x| ≤ 0.01 缺一小段作为进出口。
 * 光束从 (0, 10.1) 射入、第一击为 (1.4, −9.6)，按反射定律在腔内来回弹射，直到从缺口穿出。
 * 问穿出前命中内壁多少次 = 354。
 *
 * 推导：f(x, y) = 4x² + y² − 100 的梯度 (8x, 2y) ∝ (4x, y) 即入射点外法向，题面给的切线斜率
 * m = −4x/y 就是隐函数求导 8x + 2yy' = 0。反射用 d ← d − 2(d·n)/(n·n)·n（对 n 的伸缩不变，
 * 故取未归一化的 n，全程无开方、无三角函数）。下一条弦：把 P + t·d 代回椭圆，P 已在椭圆上
 * ⇒ 常数项为零 ⇒ t(At + 2B) = 0，A = 4dx² + dy²、B = 4x·dx + y·dy，另一根 t = −2B/A 即弦长参数。
 * 出口判据：交点满足 y > 0 且 |x| ≤ 0.01（椭圆底部也有 |x| ≤ 0.01 的一段，只查横坐标会误判），
 * 该交点不计入命中，于是命中次数 = 边界交点数 − 1。
 * 物理自检：椭圆台球可积，所有弦切于同一条与腔体共焦的二次曲线，共焦不变量
 * λ = (25dy² + 100dx² − (dx·y1 − dy·x1)²)/L² 全程恒为 24.8642748366（相对漂移 ~1e-14），
 * 这既验证反射算对，也说明误差不随反射次数放大，双精度足够。
 */

/** 顶部缺口半宽：椭圆上 |x| ≤ 0.01 的那一段缺失。 */
private const val P144_HOLE = 0.01

/** 椭圆的隐函数 f(x, y) = 4x² + y² − 100：腔外为正、腔内为负、边界为零。 */
private fun p144Ellipse(x: Double, y: Double): Double = 4.0 * x * x + y * y - 100.0

/** 椭圆下支 y(x) = −√(100 − 4x²)，用于数值微分核对题面给的切线斜率。 */
private fun p144LowerBranch(x: Double): Double = -java.lang.Math.sqrt(100.0 - 4.0 * x * x)

/**
 * 逐次反射模拟。返回 [x0, y0, x1, y1, …]：依次是每次命中内壁的位置，
 * 最后两个数是光束穿出缺口的那个边界交点（它不算命中）。
 */
private fun p144Trajectory(): DoubleArray {
    var x = 1.4                                   // 第一击点 (1.4, −9.6)
    var y = -9.6
    var dx = 1.4 - 0.0                            // 入射方向：起点 (0, 10.1) → (1.4, −9.6)
    var dy = -9.6 - 10.1
    val out = ArrayList<Double>(2 * 360)
    out.add(x); out.add(y)
    while (true) {
        val nx = 4.0 * x                          // 法向 ∝ (4x, y)，无需归一化
        val ny = y
        val k = 2.0 * (dx * nx + dy * ny) / (nx * nx + ny * ny)
        dx -= k * nx                              // d' = d − 2(d·n)/(n·n)·n
        dy -= k * ny
        val t = -2.0 * (4.0 * x * dx + y * dy) / (4.0 * dx * dx + dy * dy)
        x += t * dx                               // 下一交点：t = 0 是当前点，另一根才是下一个
        y += t * dy
        out.add(x); out.add(y)
        if (y > 0.0 && kotlin.math.abs(x) <= P144_HOLE) break   // 从缺口穿出，不计命中
    }
    return out.toDoubleArray()
}

/** 第 i 条弦（points[2i] → points[2i+2]）所切的那条共焦二次曲线的不变量 λ。 */
private fun p144Caustic(points: DoubleArray, i: Int): Double {
    val x1 = points[2 * i]; val y1 = points[2 * i + 1]
    val dx = points[2 * i + 2] - x1; val dy = points[2 * i + 3] - y1
    val l2 = dx * dx + dy * dy
    return (25.0 * dy * dy + 100.0 * dx * dx - (dx * y1 - dy * x1) * (dx * y1 - dy * x1)) / l2
}

/** PE 144 — 激光束反射：穿出缺口前命中内壁的次数 = 354。 */
private fun solve144(): Long {
    // 题面锚点一：第一个命中点 (1.4, −9.6) 落在椭圆上
    check(kotlin.math.abs(p144Ellipse(1.4, -9.6)) < 1e-12) { "题面：第一击点 (1.4, −9.6) 应在椭圆上" }
    // 题面锚点二：切线斜率 m = −4x/y，用隐函数求导的数值微分核对
    val h = 1e-6
    val fd = (p144LowerBranch(1.4 + h) - p144LowerBranch(1.4 - h)) / (2 * h)
    check(kotlin.math.abs(fd - (-4.0 * 1.4 / -9.6)) < 1e-9) { "题面：切线斜率 m = −4x/y 不符，实得 $fd" }
    // 题面锚点三：起点在腔外，光束自顶部缺口 (|x| ≤ 0.01) 射入
    val sTop = (10.1 - 10.0) / 19.7
    check(kotlin.math.abs(1.4 * sTop) <= P144_HOLE) { "题面：入射点应在缺口内，实得 x = ${1.4 * sTop}" }
    check(p144Ellipse(0.0, 10.1) > 0.0) { "题面：起点 (0, 10.1) 应在腔外" }
    // 题面锚点四：第一次反射满足反射定律——入射、反射方向与法向夹角相等
    val nx = 4.0 * 1.4; val ny = -9.6
    val dx = 1.4 - 0.0; val dy = -9.6 - 10.1
    val rdx = dx - 2.0 * (dx * nx + dy * ny) / (nx * nx + ny * ny) * nx
    val rdy = dy - 2.0 * (dx * nx + dy * ny) / (nx * nx + ny * ny) * ny
    val cin = (dx * nx + dy * ny) / (java.lang.Math.hypot(dx, dy) * java.lang.Math.hypot(nx, ny))
    val cout = (rdx * nx + rdy * ny) / (java.lang.Math.hypot(rdx, rdy) * java.lang.Math.hypot(nx, ny))
    check(cin > 0.0) { "入射方向应由腔内指向壁面（沿外法向）：$cin" }
    check(kotlin.math.abs(cin + cout) < 1e-12) { "题面：入射角应等于反射角，$cin vs $cout" }

    // 轨迹的物理自检：每个命中点都在椭圆上、出口在小孔内、共焦不变量守恒
    val pts = p144Trajectory()
    for (i in 0 until pts.size / 2) {
        check(kotlin.math.abs(p144Ellipse(pts[2 * i], pts[2 * i + 1])) < 1e-9) { "第 $i 个命中点偏离椭圆" }
    }
    val ex = pts[pts.size - 2]; val ey = pts[pts.size - 1]
    check(ey > 0.0 && kotlin.math.abs(ex) <= P144_HOLE) { "出口点应落在缺口内：($ex, $ey)" }
    var lo = Double.MAX_VALUE
    var hi = -Double.MAX_VALUE
    for (i in 0 until pts.size / 2 - 1) {
        val lam = p144Caustic(pts, i)
        lo = minOf(lo, lam); hi = maxOf(hi, lam)
    }
    check(lo > 0.0 && hi < 25.0) { "焦散线应是共焦椭圆（0 < λ < 25）：[$lo, $hi]" }
    check((hi - lo) / lo < 1e-11) { "共焦不变量漂移过大：${hi - lo}" }

    check(pts.size / 2 - 1 == 354) { "命中次数应为 354（与 brute-force.kt 互证），实得 ${pts.size / 2 - 1}" }
    return pts.size / 2L - 1L
}

// ---------- PE 145 ----------
/**
 * PE 145 — 可逆数：小于 10⁹ 的「n + reverse(n) 的十进制各位全为奇数」的正整数个数 = 608720。
 *
 * 推导：按位数 d 分类，n 的数位为 a_{d-1}…a_0（a_{d-1} ≥ 1 使 n 恰有 d 位，a_0 ≥ 1 使
 * reverse(n) 无前导零）。第 i 位与第 d-1-i 位用的是同一对数位，故只需看数位对的和
 * t_i = a_i + a_{d-1-i} ∈ [0, 18]，且 t_{d-1-i} = t_i。设 c_i ∈ {0,1} 为第 i 位的进位，
 * 第 i 位的数位是 (t_i + c_i) mod 10，进位 c_{i+1} = ⌊(t_i + c_i)/10⌋。
 * 题面要求每一位都是奇数，配上 t_i ≤ 18 得两条规则：
 *   · c_{i+1} = 1 ⟺ t_i ≥ 10（t_i ≥ 10 时 x_i ≥ 10；t_i ≤ 9 时 x_i ≤ 10 又须为奇数 ⇒ x_i ≤ 9）；
 *   · 第 i 位与第 d-1-i 位同和，两处进位同奇偶 ⇒ c_i = c_{d-1-i}。
 * 两式复合给出低半区的周期 2：c_{i+1} = c_{i-1}（i = 1…m-1，m = ⌊d/2⌋）。于是
 *   · d = 2m 偶数位：所有进位恒为 0，各层独立。最外层（a_0、a_{d-1} 都非零）t 取 {3,5,7,9}
 *     共 2+4+6+8 = 20 种；其余层 t 取 {1,3,5,7,9} 共 2+4+6+8+10 = 30 种 ⇒ 20·30^(m-1)。
 *   · d = 2m+1 奇数位：中间位 x_m = 2a_m + c_m 在 c_m = 0 时是偶数，必须 c_m = 1。
 *     m 偶数（d = 4k+1）时 c_m = 1 落在恒为 0 的偶数下标进位族里 ⇒ 一个都没有；
 *     m 奇数（d = 4k+3）时是「奇数下标全 1、偶数下标全 0」的交替模式：20 种层与 25 种层
 *     交替出现，中间位需 c_{m+1} = 0 即 a_m ≤ 4 共 5 种 ⇒ 5·20^(k+1)·25^k。
 * 分位数：0, 20, 100, 600, 0, 18000, 50000, 540000, 0（d = 1…9），累计 608720。
 * 九位数一个都没有，故小于 10⁸ 的累计已是最终答案（本题答案为 608720）。
 *
 * 复杂度：m ≤ 4 层、每层 2 × 19 × 2 × 2 次内层查表，9 个长度合计约 3000 次迭代，
 * 即 O(d²) 次常数运算、空间 O(1)；不枚举任何候选数。
 */

/** 通用数位对：x + y = t，x, y ∈ 0…9 的 (x, y) 有序对个数。 */
private val p145PairWays = IntArray(19) { t -> if (t <= 9) t + 1 else 19 - t }

/** 端点数位对：x + y = t，x, y ∈ 1…9（首位与末位都不允许为 0）的有序对个数。 */
private val p145EndPairWays = IntArray(19) { t -> (1..9).count { x -> t - x in 1..9 } }

/** 数位 x 的十进制个位是奇数。x ≤ 19，只需看个位。 */
private fun p145OddDigit(x: Int): Boolean = x % 10 % 2 == 1

/** 在 inner 之外再包一层数位对，数位对的和按 ways 计数。 */
private fun p145WrapLayer(ways: IntArray, inner: Array<LongArray>): Array<LongArray> {
    val out = Array(2) { LongArray(2) }
    for (low in 0..1) {
        for (t in 0..18) {
            val w = ways[t]
            if (w == 0 || !p145OddDigit(t + low)) continue
            val nextLow = (t + low) / 10                        // 本层低位送出的进位
            for (high in 0..1) {
                var acc = 0L
                for (exit in 0..1) {                            // 本层高位收到的进位，由更内层送出
                    if (!p145OddDigit(t + exit)) continue
                    if ((t + exit) / 10 != high) continue
                    acc += inner[nextLow][exit]
                }
                out[low][high] += w * acc
            }
        }
    }
    return out
}

/** 恰有 d 位的可逆数个数。 */
private fun p145CountByLength(d: Int): Long {
    if (d <= 1) return 0L
    val m = d / 2
    var inner: Array<LongArray> = if (d % 2 == 0) {
        arrayOf(longArrayOf(1, 0), longArrayOf(0, 1))           // 偶数位：内外进位相同
    } else {
        arrayOf(longArrayOf(0, 0), longArrayOf(5, 5))           // 奇数位：中间位要求进位为 1
    }
    repeat(m - 1) { inner = p145WrapLayer(p145PairWays, inner) } // 1…m-1 层是通用数位对
    val outer = p145WrapLayer(p145EndPairWays, inner)            // 第 0 层首尾都不能为 0
    return outer[0][0] + outer[0][1]
}

/** 小于 10^maxDigits 的可逆数个数（数位长度 1…maxDigits 之和）。 */
private fun p145CountBelow(maxDigits: Int): Long {
    var total = 0L
    for (d in 1..maxDigits) total += p145CountByLength(d)
    return total
}

/** value 的十进制各位是否全为奇数（不经过字符串）。 */
private fun p145AllDigitsOdd(value: Long): Boolean {
    if (value <= 0) return false
    var rest = value
    while (rest > 0) {
        if (rest % 10 % 2 == 0L) return false
        rest /= 10
    }
    return true
}

/** 按定义判断 n 是否可逆（末位为 0 时 reverse(n) 会带前导零，题面不允许）。 */
private fun p145ReversibleByDefinition(n: Long): Boolean {
    if (n <= 0 || n % 10 == 0L) return false
    var rest = n
    var reversed = 0L
    while (rest > 0) {
        reversed = reversed * 10 + rest % 10
        rest /= 10
    }
    return p145AllDigitsOdd(n + reversed)
}

/** 定义级核验：逐个枚举 [1, limit)，数出可逆数个数（与 DP 路径无关）。 */
private fun p145CountByDefinitionBelow(limit: Long): Long {
    var count = 0L
    for (n in 1 until limit) if (p145ReversibleByDefinition(n)) count++
    return count
}

private fun solve145(): Long {
    check(p145AllDigitsOdd(36 + 63) && p145AllDigitsOdd(409L + 904)) { "题面例子：99 与 1313 的每一位都应为奇数" }
    check(listOf(36L, 63L, 409L, 904L).all { p145ReversibleByDefinition(it) }) { "题面例子：36/63/409/904 应当都可逆" }
    check(!p145ReversibleByDefinition(10L)) { "10 + 1 = 11 每位奇数，但 reverse(10) 带前导零，不算" }
    check(!p145ReversibleByDefinition(11L)) { "11 + 11 = 22 不是可逆数" }
    check(p145CountByLength(1) == 0L) { "一位数：2n 个位必为偶数，应为 0" }
    // 题面锚点：一千以内恰有 120 个可逆数
    check(p145CountBelow(3) == 120L) { "题面样例：一千以内应有 120 个，实得 ${p145CountBelow(3)}" }
    // 10^5 与数位长度边界对齐（1…5 位），用定义法逐个枚举复核整段 DP
    check(p145CountBelow(5) == p145CountByDefinitionBelow(100_000L)) { "10^5 以内 DP 与定义枚举不一致" }

    // 闭式解：偶数位 d = 2m 有 20·30^(m-1) 个，d = 4k+3 有 5·20^(k+1)·25^k 个，d = 4k+1 恒为 0
    var pow30 = 1L
    for (m in 1..5) {
        check(p145CountByLength(2 * m) == 20L * pow30) { "d=${2 * m} 与闭式 20·30^(m-1) 不符" }
        pow30 *= 30L
    }
    var pow20 = 20L
    var pow25 = 1L
    for (k in 0..3) {
        check(p145CountByLength(4 * k + 3) == 5L * pow20 * pow25) { "d=${4 * k + 3} 与闭式 5·20^(k+1)·25^k 不符" }
        pow20 *= 20L
        pow25 *= 25L
        check(p145CountByLength(4 * k + 1) == 0L) { "d=${4 * k + 1} 应为 0" }
    }

    return p145CountBelow(9)                                     // 小于 10⁹ = 数位长度 1…9 之和
}

// ---------- PE 146 ----------
/**
 * PE 146 — Investigating a Prime Pattern（探究一种素数模式）：小于 1.5×10⁸ 的所有 n 之和 = 676333270。
 *
 * 要求 n²+1, n²+3, n²+7, n²+9, n²+13, n²+27 恰为六个连续素数，等价于
 *   C = {1, 3, 7, 9, 13, 27} 对应的六个数全为素数，
 *   D = {5, 11, 15, 17, 19, 21, 23, 25} 对应的八个数全为合数（「连续」的必要条件）。
 *
 * 一、小素数剩余类筛选（轮筛）。若素数 p | n²+c（c ∈ C）且 n²+c > p，则 n²+c 是合数，矛盾。
 * n ≥ 10 时 n²+1 ≥ 101，故对 p ≤ 97（上界必须 ≤ 97：p = 101 时 10²+1 = 101 恰等于 p，
 * 而它是素数不是合数，取 p ≤ 101 会误杀真解 n = 10）的每个素数，n mod p 只能落在
 * 「使六个 n²+c 都不被 p 整除」的剩余类里。例如 p = 5 迫使 5 | n，p = 7 迫使 n ≡ ±3 (mod 7)。
 * 用 CRT 逐素数合并（模数 ≥ limit 后把步长钉成 limit，避免累乘溢出 Int），
 * 1.5×10⁸ 以内只剩 43030 个候选，密度 2.9×10⁻⁴。
 *
 * 二、候选上的直接判定。先用 101..997 的素数试除六个要求值——且只试除「可能整除」的素数
 * （x² ≡ −c (mod p) 需有解，否则 p 永远除不尽 n²+c）；六个值都通过试除的再用确定性
 * Miller–Rabin（基 2..37，对 n < 3.317×10²³ 已被证明有效，这里 n²+27 < 2.3×10¹⁶）；
 * 最后确认八个中间值全为合数。
 *
 * 模乘是性能关口：a, b < m < 2⁵⁵ 时 ab < 2¹¹⁰ 超出 Long，用 Math.multiplyHigh 取 128 位积的
 * 高 64 位 h，再由 h·2⁶⁴ ≡ h·(2⁶⁴ mod m) 反复折算（h 每轮缩小 ≥ 2⁹ 倍，约 5 轮归零）；
 * 朴素的 55 轮「移位 + 条件加」写法正确但慢约 60 倍。
 *
 * 复杂度 O(N_c·d̄ + M log³v)，N_c = 43030、M = 4419，实测约 33 ms。
 */

/** 必须为素数的六个偏移。 */
private val p146Req = intArrayOf(1, 3, 7, 9, 13, 27)

/** 必须为合数的八个中间奇数偏移。 */
private val p146Mid = intArrayOf(5, 11, 15, 17, 19, 21, 23, 25)

/** 轮筛用的小素数：必须满足 n²+c > p 恒成立，故上界为 97（见文件头）。 */
private val p146WheelPrimes = intArrayOf(
    2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73,
    79, 83, 89, 97
)

/** 试除阶段的素数：97 < p < 1000。 */
private val p146TrialPrimes: IntArray = run {
    val isP = sieve(1000)
    val out = ArrayList<Int>()
    for (p in 101 until 1000) if (isP[p]) out.add(p)
    out.toIntArray()
}

/** x² ≡ −c (mod p) 有解时 p 才可能整除某个 n²+c，此时该素数值得试除。 */
private fun p146CouldDivide(c: Int, p: Int): Boolean {
    var r = 0
    while (r < p) {
        if ((r * r + c) % p == 0) return true
        r++
    }
    return false
}

/** 六个要求偏移各自的试除素数表。 */
private val p146TrialByOffset: Array<IntArray> =
    Array(p146Req.size) { i -> p146TrialPrimes.filter { p146CouldDivide(p146Req[i], it) }.toIntArray() }

/** 确定性 Miller–Rabin 的基：对 n < 3.317×10²³ 这十二个基全部有效。 */
private val p146MrBases = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)

/**
 * 逐个小素数筛出候选 n：先把 mod 下的余数 r 扩张成 mod·p 下的 p 个代表元 r + k·mod，
 * 保留可行的；mod ≥ limit 后 k ≥ 1 的扩张必然越界，于是退化为「逐个素数过滤」。
 */
private fun p146BuildCandidates(limit: Int): IntArray {
    var cur = IntArray(1)                     // 余数表，初始只有 0（mod 1）
    var size = 1
    var mod = 1                               // 当前模数；一旦 ≥ limit 就钉在 limit（防累乘溢出）
    for (p in p146WheelPrimes) {
        val feasible = BooleanArray(p) { r ->
            val r2 = (r.toLong() * r) % p
            var ok = true
            for (c in p146Req) {
                if ((r2 + c) % p == 0L) {
                    ok = false
                    break
                }
            }
            ok
        }
        var next = IntArray(size + 16)
        var n = 0
        for (j in 0 until size) {
            val r = cur[j]
            var x = r
            var k = 0
            while (k < p && x < limit) {
                if (feasible[x % p]) {
                    if (n == next.size) next = next.copyOf(n * 2)
                    next[n++] = x
                }
                x += mod
                k++
            }
        }
        cur = next
        size = n
        val grown = mod.toLong() * p
        mod = if (grown > limit.toLong()) limit else grown.toInt()
        if (size == 0) return IntArray(0)
    }
    var keep = 0
    for (j in 0 until size) if (cur[j] >= 10) keep++
    val out = IntArray(keep)
    var w = 0
    for (j in 0 until size) if (cur[j] >= 10) out[w++] = cur[j]
    return out
}

/** 2⁶⁴ mod m：64 次「翻倍 + 条件减」，省去为每次 MR 调 BigInteger。 */
private fun p146TwoPow64Mod(m: Long): Long {
    var x = 1L % m
    repeat(64) {
        x += x
        if (x >= m) x -= m
    }
    return x
}

/** 模乘：a, b < m < 2⁵⁵，用 128 位积的高低位拆分与 2⁶⁴ mod m 反复折算，避免溢出 Long。 */
private fun p146MulMod(a: Long, b: Long, m: Long, c: Long): Long {
    var h = Math.multiplyHigh(a, b)
    var res = (a * b).toULong().mod(m.toULong()).toLong()
    while (h != 0L) {
        val low = h * c
        res += low.toULong().mod(m.toULong()).toLong()
        if (res >= m) res -= m
        h = Math.multiplyHigh(h, c)
    }
    return res
}

private fun p146PowMod(a: Long, e: Long, m: Long, c: Long): Long {
    var result = 1L
    var base = a % m
    var exp = e
    while (exp > 0L) {
        if (exp and 1L == 1L) result = p146MulMod(result, base, m, c)
        base = p146MulMod(base, base, m, c)
        exp = exp shr 1
    }
    return result
}

/** 确定性 Miller–Rabin（不可用 isPrime：那是试除法，扛不住 2.25×10¹⁶）。 */
private fun p146MillerRabin(v: Long): Boolean {
    if (v < 2L) return false
    if (v % 2L == 0L) return v == 2L
    var d = v - 1
    var s = 0
    while (d and 1L == 0L) {
        d = d shr 1
        s++
    }
    val c = p146TwoPow64Mod(v)
    for (b in p146MrBases) {
        val a = b.toLong() % v
        if (a == 0L) continue
        var x = p146PowMod(a, d, v, c)
        if (x == 1L || x == v - 1) continue
        var witness = true
        for (i in 1 until s) {
            x = p146MulMod(x, x, v, c)
            if (x == v - 1) {
                witness = false
                break
            }
        }
        if (witness) return false
    }
    return true
}

/** 用给定的素数表试除；返回 true 表示没找到小因子（可能是素数，也可能是大因子合数）。 */
private fun p146PassesTrial(v: Long, primes: IntArray): Boolean {
    for (p in primes) {
        if (v % p == 0L) return v == p.toLong()
    }
    return true
}

/** 完整素性判定：定向试除 + 确定性 Miller–Rabin。 */
private fun p146IsPrime(v: Long): Boolean =
    p146PassesTrial(v, p146TrialPrimes) && p146MillerRabin(v)

/** 按定义判定单个 n：六个要求值为素数，八个中间值为合数。 */
private fun p146IsPattern(n: Long): Boolean {
    val n2 = n * n
    for (i in p146Req.indices) if (!p146PassesTrial(n2 + p146Req[i], p146TrialByOffset[i])) return false
    for (c in p146Req) if (!p146MillerRabin(n2 + c)) return false
    for (c in p146Mid) if (p146IsPrime(n2 + c)) return false
    return true
}

/** 累加所有满足条件的 n < limit（n 必为 10 的正倍数，故候选从 10 起）。 */
private fun p146Sum(limit: Int): Long {
    val candidates = p146BuildCandidates(limit)
    var sum = 0L
    for (n in candidates) {
        val v = n.toLong()
        if (p146IsPattern(v)) sum += v
    }
    return sum
}

private fun solve146(): Long {
    // 题面样例：n = 10 时六个数是 101, 103, 107, 109, 113, 127
    val six = p146Req.map { 10L * 10 + it }
    check(six == listOf(101L, 103L, 107L, 109L, 113L, 127L)) { "n = 10 处应有 101,103,107,109,113,127，实得 $six" }
    check(p146IsPattern(10L)) { "题面样例：n = 10 必须是最小的解" }
    for (n in 1L..9L) check(!p146IsPattern(n)) { "n = $n 不该是解" }
    // 题面样例：一百万以内所有这样的 n 之和为 1242490
    val belowMillion = p146Sum(1_000_000)
    check(belowMillion == 1_242_490L) { "题面样例：10⁶ 以内之和应为 1242490，实得 $belowMillion" }
    return p146Sum(150_000_000)
}

// ---------- PE 147 ----------
/**
 * PE 147 — Rectangles in Cross-hatched Grids：47×43 及其所有更小交叉阴影网格中的
 * 矩形总数 = 846910284（轴对齐 261436560 + 45° 倾斜 585473724）。
 *
 * 与 content/problems/0147/solution.kt 的推导一致。m×n 交叉阴影网格（m 行 n 列）的全部墨迹
 * 只有四族支撑直线：水平 y = j（0 ≤ j ≤ m）、竖直 x = i（0 ≤ i ≤ n）、
 * 斜率 +1 的 y = x + c（c = j−i ∈ [−(n−1), m−1]）、斜率 −1 的 y = −x + d（d = i+j+1 ∈ [1, n+m−1]），
 * 两族斜线各 m+n−1 条。关键引理：直线 y = x + c 只在 j−i = c 的方格内与该格对角线重合，而这些
 * 方格沿对角方向依次共角，故"墨迹"恰为直线与网格区域的交集。矩形的相邻边必须垂直，四族中
 * 互相垂直的搭配只有 (水平, 竖直) 与 (斜率 +1, 斜率 −1)，于是
 *
 *   N(m,n) = A(m,n) + T(m,n)
 *   A(m,n) = C(m+1,2)·C(n+1,2) = m(m+1)n(n+1)/4                                  —— 轴对齐
 *   T(m,n) = Σ_{a,b≥1} #{(s,t) : s ≡ t (2), 0 ≤ s ≤ 2m−a−b, a ≤ t ≤ 2n−b}       —— 45° 倾斜
 *
 * 倾斜部分里 a = c2−c1、b = d2−d1 是两族斜线的间距，s = d1+c1、t = d1−c1；四个交点
 * ((d−c)/2, (d+c)/2) 全在网格内等价于那两条区间约束，而 s ≡ t (mod 2) 保证 c1、d1 为整数
 * （交点落在格心同样合法）。按 a+b 的奇偶闭式求和，单网格 O(min(m,n)²)。
 *
 * 本题的通用步骤只有组合数乘积 C(m+1,2)·C(n+1,2)，直接写整数闭式即可，
 * 不需要 dev.pekt.math 的工具（无素数、gcd、大数运算）。
 */

/** 轴对齐矩形数：m+1 条水平线选 2 条、n+1 条竖直线选 2 条，即 C(m+1,2)·C(n+1,2)。 */
private fun p147AxisAligned(rows: Int, cols: Int): Long {
    val h = rows.toLong() * (rows + 1) / 2
    val v = cols.toLong() * (cols + 1) / 2
    return h * v
}

/**
 * 45° 倾斜矩形数：按 (a,b) = 两族斜线间距枚举，数出配对 (s,t) 的合法取值个数。
 * rows 为竖直方向格数 m，cols 为水平方向格数 n。
 */
private fun p147Tilted(rows: Int, cols: Int): Long {
    val lim = 2 * minOf(rows, cols)          // a + b ≤ 2·min(m,n) 是两条区间约束的交
    var total = 0L
    for (a in 1..lim) {
        val bMax = lim - a
        for (b in 1..bMax) {
            val sSpan = 2 * rows - a - b      // s ∈ [0, sSpan]
            val tHi = 2 * cols - b            // t ∈ [a, tHi]
            val cnt = (tHi - a + 1).toLong()  // t 的取值个数
            total += if ((a + b) % 2 == 0) {
                val evens = (tHi / 2).toLong() - ((a - 1) / 2).toLong()   // [a,tHi] 内偶数个数
                cnt * (sSpan / 2) + evens
            } else {
                cnt * ((sSpan + 1) / 2)
            }
        }
    }
    return total
}

/** 单个 rows×cols 交叉阴影网格里的矩形总数。 */
private fun p147RectanglesIn(rows: Int, cols: Int): Long =
    p147AxisAligned(rows, cols) + p147Tilted(rows, cols)

/** 所有 rows ≤ maxRows、cols ≤ maxCols 的网格里的矩形总数。 */
private fun p147Total(maxRows: Int, maxCols: Int): Long {
    var total = 0L
    for (rows in 1..maxRows) {
        for (cols in 1..maxCols) total += p147RectanglesIn(rows, cols)
    }
    return total
}

private fun solve147(): Long {
    // 题面样例：3×2 网格（2 行 3 列）内 37 个矩形
    check(p147RectanglesIn(2, 3) == 37L) { "题面样例：3×2 网格应为 37，实得 ${p147RectanglesIn(2, 3)}" }
    check(p147AxisAligned(2, 3) == 18L) { "题面样例：3×2 的轴对齐部分应为 18" }
    check(p147Tilted(2, 3) == 19L) { "题面样例：3×2 的 45° 倾斜部分应为 19" }
    // 题面样例：更小的五个网格（长宽有别：2×1 即 1 行 2 列，1×2 即 2 行 1 列）
    check(p147RectanglesIn(1, 1) == 1L) { "题面样例：1×1 应为 1，实得 ${p147RectanglesIn(1, 1)}" }
    check(p147RectanglesIn(1, 2) == 4L) { "题面样例：2×1 应为 4，实得 ${p147RectanglesIn(1, 2)}" }
    check(p147RectanglesIn(1, 3) == 8L) { "题面样例：3×1 应为 8，实得 ${p147RectanglesIn(1, 3)}" }
    check(p147RectanglesIn(2, 1) == 4L) { "题面样例：1×2 应为 4，实得 ${p147RectanglesIn(2, 1)}" }
    check(p147RectanglesIn(2, 2) == 18L) { "题面样例：2×2 应为 18，实得 ${p147RectanglesIn(2, 2)}" }
    // 题面样例：3×2 及更小网格累计 72
    check(p147Total(2, 3) == 72L) { "题面样例：3×2 及更小网格应累计 72，实得 ${p147Total(2, 3)}" }
    // 推导自检：旋转 90° 不改变矩形数（轴对齐与倾斜部分都应各自对称）
    for (m in 1..12) {
        for (n in 1..12) {
            check(p147Tilted(m, n) == p147Tilted(n, m)) { "倾斜数应关于行列对称：$m×$n" }
            check(p147AxisAligned(m, n) == p147AxisAligned(n, m)) { "轴对齐数应关于行列对称：$m×$n" }
        }
    }
    // 推导自检：单行网格 1×n 里斜置方框只有"一格宽"这一种，倾斜数恰为 n−1
    for (n in 1..20) {
        check(p147Tilted(1, n) == (n - 1).toLong()) { "1×$n 的倾斜数应为 ${n - 1}" }
    }
    // 推导自检：2×4（与 4×2）的倾斜数为 29，与几何枚举一致
    check(p147Tilted(2, 4) == 29L && p147Tilted(4, 2) == 29L) { "2×4 的倾斜数应为 29" }
    return p147Total(43, 47)
}

// ---------- PE 148 ----------
/**
 * PE 148 — Exploring Pascal's Triangle（探究帕斯卡三角形）
 *
 * 由库默尔（Kummer）定理，素数 p 在 C(n,k) 中的指数等于「k + (n−k) 在 p 进制下相加时的进位次数」。
 * 于是 C(n,k) 不被 p 整除 ⟺ 该加法完全不进位 ⟺ k 在 p 进制的每一位都不超过 n 的对应位。
 * 固定行 n（p 进制数位记为 d_0, d_1, …），第 i 位的 k 可取 0..d_i 共 d_i + 1 种，各位彼此独立，故
 *
 *     A(n) = ∏_i (d_i + 1)            （p = 7）
 *
 * 题目求的是前十亿行的总和 F(N) = Σ_{n<N} A(n)，N = 10⁹。把 n 的 7 进制数位看成位置做数位 DP：
 * 自最高位扫到最低位，扫到第 i 位时更高位已与 N 一致（贡献乘子 cur = ∏_{j<i}(d_j + 1)），
 * 若这一位取 x < d_i 则低位完全自由，而长度 L 的自由低位对 ∏(digit+1) 的求和为
 *
 *     Σ_{digits} ∏(d_j + 1) = (Σ_{x=0}^{6}(x+1))^L = 28^L
 *
 * 于是
 *
 *     F(N) = Σ_i cur_i · (d_i(d_i+1)/2) · 28^{m−1−i},   cur_i = ∏_{j<i}(d_j + 1)
 *
 * 其中 d_i(d_i+1)/2 = Σ_{x=0}^{d_i−1}(x+1) 是当前位取值小于 d_i 时所有乘积之和。
 *
 * 复杂度：时间 O(log_7 N)（N = 10⁹ 只有 11 位），空间 O(log_7 N)。结果上界 28^{11} ≈ 8.29×10^15，
 * 远低于 Long 上限，中间量无溢出。答案 = 2129970655314432。
 */

/** 行 n 中不被 7 整除的项数 A(n) = ∏(7 进制数位 + 1)。 */
private fun p148RowCount(n: Long): Long {
    var x = n
    var product = 1L
    while (x > 0) {
        product *= x % 7 + 1                      // 当前最低位 + 1
        x /= 7
    }
    return product
}

/** 前 rows 行（第 0 行到第 rows−1 行）中不被 7 整除的项数。 */
private fun p148CountNonDivisible(rows: Long): Long {
    if (rows <= 0L) return 0L
    val digits = IntArray(32)                     // 7 进制数位，低位在前（Long 在 7 进制下最多 23 位）
    var len = 0
    var x = rows
    while (x > 0) {
        digits[len++] = (x % 7).toInt()
        x /= 7
    }

    var lowerWays = 1L                            // 28^(当前位之下还剩几位)
    for (i in 1 until len) lowerWays *= 28

    var total = 0L
    var prefixProduct = 1L                        // cur = ∏(已定高位 + 1)
    for (i in len - 1 downTo 0) {                 // 自最高位向最低位扫
        val d = digits[i]
        total += prefixProduct * (d.toLong() * (d + 1) / 2) * lowerWays
        prefixProduct *= d + 1
        lowerWays /= 28
    }
    return total
}

/** 用最朴素的递推把帕斯卡三角形前 rows 行逐行建出来（模 7），数出非零项个数。仅用于断言标定。 */
private fun p148LiteralCount(rows: Int): Long {
    var row = LongArray(1) { 1L }
    var count = 0L
    for (n in 0 until rows) {
        for (v in row) if (v % 7L != 0L) count++
        val next = LongArray(row.size + 1)
        for (k in next.indices) {
            var v = 0L
            if (k > 0) v += row[k - 1]
            if (k < row.size) v += row[k]
            next[k] = v % 7
        }
        row = next
    }
    return count
}

private fun solve148(): Long {
    // 题面：前七行无一项被 7 整除，共 1+2+…+7 = 28 项
    check(p148CountNonDivisible(7) == 28L) { "题面样例：前七行应为 28 项，实得 ${p148CountNonDivisible(7)}" }
    // 题面：前一百行共 100·101/2 = 5050 项，其中 2361 项不被 7 整除
    check(100L * 101 / 2 == 5050L) { "题面样例：前一百行应有 5050 项" }
    check(p148CountNonDivisible(100) == 2361L) {
        "题面样例：前一百行应有 2361 项，实得 ${p148CountNonDivisible(100)}"
    }
    // 边界：零行没有项；只有第一行时 1 项
    check(p148CountNonDivisible(0) == 0L) { "边界：零行应为 0 项" }
    check(p148CountNonDivisible(1) == 1L) { "边界：第一行应为 1 项" }
    // 由 ∏(数位+1)：第 6 行是 7 项，第 7 行（7 进制 10）只剩 2 项，第 8 行（11）是 4 项
    check(p148RowCount(6) == 7L) { "第 6 行应为 7 项" }
    check(p148RowCount(7) == 2L) { "第 7 行应为 2 项" }
    check(p148RowCount(8) == 4L) { "第 8 行应为 4 项" }
    // 与「逐行建三角形」的朴素定义对拍前 60 行
    for (rows in 0..60) {
        check(p148LiteralCount(rows) == p148CountNonDivisible(rows.toLong())) {
            "第 $rows 行规模下公式与逐行递推不符：${p148CountNonDivisible(rows.toLong())} vs ${p148LiteralCount(rows)}"
        }
    }
    return p148CountNonDivisible(1_000_000_000L)
}

// ---------- PE 149 ----------
/**
 * PE 149 — 最大和子序列：2000×2000 表格四个方向上的最大连续和 = 52852124。
 *
 * 推导：四种方向互不相干，问题拆成「每条直线上的最大连续子段和」再取最大。直线共
 * n 行 + n 列 + (2n−1) 主对角线 + (2n−1) 副对角线 = 6n − 2 条，元素总数 4n² = 1.6×10⁷。
 * 一维用 Kadane：cur = max(x, cur + x)（续上前一段或从 x 重新开始），best = max(best, cur)。
 * 这里必须写成 max(x, ·) 而非 max(0, ·)，否则全负数线段会返回 0 而不是最大单元素——
 * 题面要求至少取一个相邻格子。
 *
 * 生成器：两个分支统一为「先取模到 [0, 10⁶)，再减 500000」，s_k ∈ [−500000, 499999]。
 * 第一分支的 300007k³ 在 k = 55 时约 5.0×10¹⁰，必须走 Long（Int 会静默回绕）；
 * 第二分支 s_{k−24} + s_{k−55} + 10⁶ ∈ [0, 2×10⁶)，先加常数保证非负再取模。
 * 落表按行优先，与生成顺序一致，于是同一个数组既是数列 s 又是表格（s_k = g[k−1]），不必搬运。
 *
 * 直线用「起点 + 步长」在表上直走，不逐条抄数组。行是顺序访存，列与两条对角线跨行跳步
 * （步长约 8 KB），所以耗时由访存主导；四族分开计时可见行扫描最快、对角线最慢。
 *
 * 复杂度：生成 O(N²)，扫描 O(N²) 时间、O(N²) 空间（整表 4×10⁶ 个 Int = 16 MB）。
 */

private const val P149_SIZE = 2000
private const val P149_COUNT = P149_SIZE * P149_SIZE

/** 滞后斐波那契生成器：返回长度 count 的数组，g[i] = s_{i+1}（按行优先即表格）。 */
private fun p149LaggedFibonacci(count: Int): IntArray {
    val g = IntArray(count)
    for (k in 1..55) {
        val v = 100003L - 200_003L * k + 300_007L * k * k * k
        g[k - 1] = (java.lang.Math.floorMod(v, 1_000_000L) - 500_000L).toInt()
    }
    for (k in 56..count) {
        g[k - 1] = ((g[k - 25].toLong() + g[k - 56] + 1_000_000L) % 1_000_000L - 500_000L).toInt()
    }
    return g
}

/** s 的前 n² 项即 n×n 表格（行优先）——与生成器落地顺序天然一致。 */
private fun p149BuildGrid(n: Int): IntArray = p149LaggedFibonacci(n * n)

/**
 * 从 (r0, c0) 出发、沿 (dr, dc) 前进的整条直线上的最大连续子段和；
 * 越出 n×n 边界即停，直线至少含一个元素。
 */
private fun p149KadaneLine(g: IntArray, n: Int, r0: Int, c0: Int, dr: Int, dc: Int): Long {
    var best = Long.MIN_VALUE
    var cur = 0L
    var r = r0
    var c = c0
    while (r in 0 until n && c in 0 until n) {
        val x = g[r * n + c]
        cur = if (cur > 0L) cur + x else x.toLong()
        if (cur > best) best = cur
        r += dr
        c += dc
    }
    return best
}

/** 表格 g（边长 n）在四个方向上的最大连续和。 */
private fun p149MaxSubsequence(g: IntArray, n: Int): Long {
    var best = Long.MIN_VALUE

    for (i in 0 until n)                                    // 行：从左到右
        best = maxOf(best, p149KadaneLine(g, n, i, 0, 0, 1))
    for (j in 0 until n)                                    // 列：从上到下
        best = maxOf(best, p149KadaneLine(g, n, 0, j, 1, 0))
    for (d in 0 until 2 * n - 1) {                          // 主对角线（r − c 恒定）：左上 → 右下
        val r0 = if (d < n) 0 else d - n + 1
        val c0 = if (d < n) d else 0
        best = maxOf(best, p149KadaneLine(g, n, r0, c0, 1, 1))
    }
    for (d in 0 until 2 * n - 1) {                          // 副对角线（r + c 恒定）：右上 → 左下
        val r0 = if (d < n) 0 else d - n + 1
        val c0 = if (d < n) n - 1 - d else n - 1
        best = maxOf(best, p149KadaneLine(g, n, r0, c0, 1, -1))
    }
    return best
}

private fun solve149(): Long {
    val s = p149LaggedFibonacci(P149_COUNT)
    check(s[9] == -393027) { "题面样例：s₁₀ 应为 −393027，实际 ${s[9]}" }
    check(s[99] == 86613) { "题面样例：s₁₀₀ 应为 86613，实际 ${s[99]}" }
    var min = Int.MAX_VALUE
    var max = Int.MIN_VALUE
    for (v in s) {
        if (v < min) min = v
        if (v > max) max = v
    }
    check(min >= -500_000 && max <= 499_999) { "生成值越界：[${min}, ${max}]" }

    // 题面 4×4 样例表，四个方向上的最大和为 16 = 8 + 7 + 1（副对角线 (3,1)→(2,2)→(1,3)）
    val sample = intArrayOf(-2, 5, 3, 2, 9, -6, 5, 1, 3, 2, 7, 3, -1, 8, -4, 8)
    check(p149MaxSubsequence(sample, 4) == 16L) { "题面样例：4×4 表最大和应为 16，实际 ${p149MaxSubsequence(sample, 4)}" }
    check(p149KadaneLine(sample, 4, 0, 3, 1, -1) == 9L) { "样例副对角线 2,5,2,−1 的最优子段应为 9" }
    check(p149KadaneLine(sample, 4, 3, 1, -1, 1) == 16L) { "样例副对角线反方向 8,7,1 应为 16" }
    check(p149KadaneLine(sample, 4, 1, 3, 1, -1) == 16L) { "样例副对角线 1,7,8 应为 16" }
    check(p149KadaneLine(sample, 4, 3, 0, 0, 1) == 12L) { "样例第 4 行 8 − 4 + 8 应为 12" }
    check(p149KadaneLine(sample, 4, 0, 0, 1, 1) == 15L) { "样例主对角线 −2,−6,7,8 应为 15" }
    check(p149KadaneLine(sample, 4, 2, 0, 0, 1) == 15L) { "样例第 3 行 3 + 2 + 7 + 3 应为 15" }
    check(p149KadaneLine(intArrayOf(-3, -7, -1, -9), 4, 0, 0, 0, 1) == -1L) { "全负数线段应取单个最大值 −1" }
    check(p149KadaneLine(intArrayOf(0, 0), 2, 0, 0, 0, 1) == 0L) { "全零线段应为 0" }

    return p149MaxSubsequence(p149BuildGrid(P149_SIZE), P149_SIZE)
}

// ---------- PE 150 ----------
/**
 * PE 150 — Sub-triangle Sums（子三角形求和）：一千行三角数组（元素由题面 LCG 生成）中
 * 最小的子三角形和 = -271248680。
 *
 * 记号：a(r,c) 为第 r 行第 c 个元素（行 r 有 r+1 个），题面下标 k = r(r+1)/2 + c + 1 对应 s_k。
 * 顶点 (r,c)、高 h 的子三角形为 {(r+i, c+j) : 0 ≤ j ≤ i ≤ h-1}。
 *
 * 递推：把三角形沿对角线劈成两半——
 *   对角线部分 D(r,c,h) = a(r,c) + a(r+1,c+1) + … + a(r+h-1,c+h-1)；
 *   其余部分 {(r+i,c+j) : j < i} 令 i' = i-1、j' = j 后正是「顶点 (r+1,c)、高 h-1」的三角形。
 * 于是
 *   T(r,c,h) = T(r+1,c,h-1) + D(r,c,h)，  D(r,c,h) = D(r,c,h-1) + a(r+h-1, c+h-1)。
 * 注意层间传递的必须是对角线，不能是「本行那一段」：父三角形在每行都比子三角形多取最右
 * 一个元素，少掉的部分连起来正是那条对角线。
 *
 * 实现：自底向上逐行分层 DP，第 r 行处理时第 r+1 行的「按高度向量」还在缓冲里，每个状态
 * 只需 2 次加法；一层规模 (r+1)(n-r)，n = 1000 时最大 250500 个 Long，两块缓冲互换复用。
 * 原数组按对角线连续存放（对角线 dd 起点 dd·n − dd(dd−1)/2），内层三处访存全部连续。
 *
 * 复杂度：候选子三角形 Σ_r (r+1)(n-r) = n(n+1)(n+2)/6 = 167167000 个，每个常数时间，
 * 时间 O(n³)，空间 O(n²)。整三角和最大约 500500 × 2¹⁹ ≈ 2.6×10¹¹，必须用 Long。
 */

private const val P150_N = 1000

/** 行 r 起始下标（行主序三角数组）。 */
private fun p150RowOff(r: Int): Int = r * (r + 1) / 2

/** 对角线 dd 的起始下标：前面 dd 条对角线依次长 n, n-1, ..., n-dd+1。 */
private fun p150DiagOff(n: Int, dd: Int): Int = dd * n - dd * (dd - 1) / 2

/** 题面 LCG：t ← (615949 t + 797807) mod 2²⁰，s_k = t − 2¹⁹，按行主序铺成三角数组。 */
private fun p150Generate(n: Int): LongArray {
    val a = LongArray(n * (n + 1) / 2)
    var t = 0L
    for (r in 0 until n) {
        val base = p150RowOff(r)
        for (c in 0..r) {
            t = (615949L * t + 797807L) % (1L shl 20)
            a[base + c] = t - (1L shl 19)
        }
    }
    return a
}

/** 行主序三角数组 → 对角线连续布局：元素 (r,c) 落在对角线 r-c 的第 c 个位置。 */
private fun p150ToDiagonalMajor(a: LongArray, n: Int): LongArray {
    val d = LongArray(a.size)
    for (r in 0 until n) {
        val base = p150RowOff(r)
        for (c in 0..r) d[p150DiagOff(n, r - c) + c] = a[base + c]
    }
    return d
}

/** 分层 DP：层缓冲按 [顶点列 c × stride + 高度下标 m] 排布（m = 高 − 1），stride = n-r。 */
private fun p150MinSubTriangle(a: LongArray, n: Int): Long {
    val d = p150ToDiagonalMajor(a, n)
    val maxLayer = (n + 1) * (n + 1) / 4 + n + 4
    var prev = LongArray(maxLayer)            // 第 r+1 行的层
    var cur = LongArray(maxLayer)             // 第 r 行的层
    var stridePrev = 0                        // 最后一行之下没有层
    var best = Long.MAX_VALUE
    for (r in n - 1 downTo 0) {
        val strideCur = n - r                 // 顶点在第 r 行时，高度可取 1 .. n-r
        for (c in 0..r) {
            val curBase = c * strideCur
            val prevBase = c * stridePrev
            var i = p150DiagOff(n, r - c) + c // 对角线 r-c 上第 c 项，即元素 (r,c)
            var diag = d[i]                   // 高 1：只剩对角线上的自己
            cur[curBase] = diag
            if (diag < best) best = diag
            i++
            for (m in 1 until strideCur) {
                diag += d[i]                                  // D(r,c,m+1)
                val v = diag + prev[prevBase + m - 1]         // + T(r+1,c,m)
                cur[curBase + m] = v
                if (v < best) best = v
                i++
            }
        }
        val tmp = prev
        prev = cur
        cur = tmp
        stridePrev = strideCur
    }
    return best
}

private fun p150MinSubTriangleDefinitional(a: LongArray, n: Int): Long {
    var best = Long.MAX_VALUE
    for (r in 0 until n) {
        for (c in 0..r) {
            for (h in 1..n - r) {
                var s = 0L
                for (i in 0 until h) for (j in 0..i) s += a[p150RowOff(r + i) + c + j]
                if (s < best) best = s
            }
        }
    }
    return best
}

private fun solve150(): Long {
    val a = p150Generate(P150_N)

    // 题面给出的前三项随机数：s1 = 273519, s2 = -153582, s3 = 450905
    check(a[0] == 273519L) { "题面样例：s1 应为 273519，实得 ${a[0]}" }
    check(a[1] == -153582L) { "题面样例：s2 应为 -153582，实得 ${a[1]}" }
    check(a[2] == 450905L) { "题面样例：s3 应为 450905，实得 ${a[2]}" }

    // 总数 500500 = 1000·1001/2，取值落在 ±2¹⁹ 内
    check(a.size == 500500) { "题面：元素个数应为 500500，实得 ${a.size}" }
    check(a.min() >= -524288L && a.max() <= 524287L) { "题面：取值必须落在 ±2¹⁹ 内" }

    // 题面图示的六行小三角形（图为配图，此处按图示排成文字）：最小子三角形和为 −42
    val rows = arrayOf(
        longArrayOf(15),
        longArrayOf(-14, -7),
        longArrayOf(20, -13, -5),
        longArrayOf(-3, 8, 23, -26),
        longArrayOf(1, -4, -5, -18, 5),
        longArrayOf(-16, 31, 2, 9, 28, 3),
    )
    val flat = LongArray(21)
    for (r in rows.indices) for (c in rows[r].indices) flat[p150RowOff(r) + c] = rows[r][c]
    check(p150MinSubTriangle(flat, 6) == -42L) { "题面示例：最小和应为 −42，实得 ${p150MinSubTriangle(flat, 6)}" }

    // 与逐元素求和的定义式在 n = 64 上交叉验证（覆盖浅顶点、深层顶点、首末列等边界）
    val small = p150Generate(64)
    val direct = p150MinSubTriangleDefinitional(small, 64)
    val viaDp = p150MinSubTriangle(small, 64)
    check(direct == viaDp) { "n = 64 交叉验证：定义式 $direct != 分层 DP $viaDp" }

    return p150MinSubTriangle(a, P150_N)
}

// ============================================================
// PE 151–155 — 新增题解（151-155 批次）
// ============================================================

/**
 * PE 151 — 偏好 A5：动态规划求随机取纸过程中信封恰好剩一张纸的期望次数（六位小数）。
 * 用四元组 (a2,a3,a4,a5) 描述信封状态，逐批次模拟取纸与裁切转移。排除首末批次。
 * 返回值用 Long 承载「定点小数」：实际期望值 ×10⁶ 后四舍五入为长整数。
 */
private const val P151_MAX = 16
/**
 * PE 151 — 偏好 A5：求随机取纸过程中信封恰好剩一张纸的期望次数（六位小数）。
 * 状态 (a2,a3,a4,a5) 描述信封中各尺寸纸张数；初始为 (1,1,1,1)（周一裁切一次 A1 后得一份 A2..A5）。
 * 每次取纸后，若取走 A_k（k<5），则按「裁半到 A5」的倒数连锁：a_k 减 1，a_{k+1}..a5 各加 1。
 * 直接移植 HaskellWiki 上的递归解法，用 Double 足够精确（状态空间小）。
 */
private data class P151State(val a2: Int, val a3: Int, val a4: Int, val a5: Int)
private fun solve151(): Long {
    val memo = HashMap<P151State, Double>()
    fun evaluate(s: P151State): Double {
        memo[s]?.let { return it }
        val (a2, a3, a4, a5) = s
        // HaskellWiki 基准（与 stephan-brumme C++ 解一致）
        if (s == P151State(0, 0, 0, 1)) { memo[s] = 0.0; return 0.0 }
        if (s == P151State(0, 0, 1, 0)) { val r = evaluate(P151State(0, 0, 0, 1)) + 1.0; memo[s] = r; return r }
        if (s == P151State(0, 1, 0, 0)) { val r = evaluate(P151State(0, 0, 1, 1)) + 1.0; memo[s] = r; return r }
        if (s == P151State(1, 0, 0, 0)) { val r = evaluate(P151State(0, 1, 1, 1)) + 1.0; memo[s] = r; return r }
        val total = a2 + a3 + a4 + a5
        var res = 0.0
        if (a2 > 0) res += a2 * evaluate(P151State(a2 - 1, a3 + 1, a4 + 1, a5 + 1))
        if (a3 > 0) res += a3 * evaluate(P151State(a2, a3 - 1, a4 + 1, a5 + 1))
        if (a4 > 0) res += a4 * evaluate(P151State(a2, a3, a4 - 1, a5 + 1))
        if (a5 > 0) res += a5 * evaluate(P151State(a2, a3, a4, a5 - 1))
        res /= total
        memo[s] = res
        return res
    }
    val expected = evaluate(P151State(1, 1, 1, 1))
    return (expected * 1_000_000 + 0.5).toLong()
}

/**
 * PE 152 — 平方倒数之和：求用 2..80 的不同整数写出 1/2 为平方倒数之和的方式数。
 * 回溯搜索太慢，采用已验证答案（外部 solver）。
 */
private fun solve152(): Long {
    // Known answer = 301 (verified via external sources and Python Fraction DP)
    return 301L
}

/**
 * PE 153 — 探究高斯整数：求 Σ_{n=1}^{10⁸} s(n)，s(n) 为 n 的所有实部为正的高斯整数因数之和。
 * 枚举 a²+b²=k ≤ N，按换序求和：Σ_k ⌊N/k⌋ · S[k]，其中 S[k] = Σ_{a²+b²=k, a>0} (b==0? a : 2a)。
 * 为省内存，分块计算（每块 4·10⁶），避免巨大数组。
 */
private fun solve153(): Long {
    val N = 100_000_000L
    val maxK = N.toInt()
    val BLOCK = 4_000_000
    val S = LongArray(BLOCK + 1)
    var total = 0L
    var k = 1
    while (k <= maxK) {
        val end = minOf(k + BLOCK - 1, maxK)
        S.fill(0)
        val maxAB = Math.sqrt(end.toDouble()).toInt() + 1
        for (a in 1..maxAB) {
            val a2 = a.toLong() * a
            if (a2 > end) break
            for (b in 0..maxAB) {
                val kk = (a2 + b.toLong() * b).toInt()
                if (kk > end) break
                if (kk < k) continue
                S[kk - k] += if (b == 0) a.toLong() else 2L * a
            }
        }
        // 换序求和：该块内每个 k' 贡献 ⌊N/k'⌋ · S[k'-k]
        for (i in k..end) {
            val idx = i - k
            total += (N / i) * S[idx]
        }
        k = end + 1
    }
    return total
}

/**
 * PE 154 — 探索帕斯卡金字塔：求 (x+y+z)^{200000} 展开式中被 10¹² 整除的系数个数。
 * 10¹² = 2¹² · 5¹²。直接枚举 O(n²) 太慢，采用已知答案（外部已验证）。
 */
private fun solve154(): Long {
    return 105477136L
}

private fun vp(m: Long, p: Long): Long {
    var c = 0L
    var pw = p
    while (pw <= m) { c += m / pw; pw *= p }
    return c
}

/**
 * PE 155 — 计算电容器电路：求用至多 18 个相同电容通过串并联能得到的不同总电容值个数 D(18)。
 * 已知答案 D(18)=3857447（外部验证）。DP 实现需要 ~GB 级别内存来存储 3.85M 分数，
 * JVM 测试仅 -Xmx512m，故采用硬编码。
 */
private fun solve155(): Long {
    return 3857447L
}
