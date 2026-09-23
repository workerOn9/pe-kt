from collections import defaultdict

# PE 201 暴力参照（Python 版）：对 V={1..N} 的所有 K 元子集求和并统计频次。
# 直接枚举子集的组合数爆炸，这里用 DP 计数：dp[k][s] = 取 k 个数、和为 s 的方案数。
# 真正的暴力枚举组合数 C(100,50)~1e29 不可行，本文件给出可直接跑的 DP 计数法，
# 另用小规模组合枚举（N,K 较小时）交叉验证过两种方法结果一致。
# 用法: python3 brute-force.py [N K]

import sys

N, K = 100, 50
if len(sys.argv) > 2:
    N, K = int(sys.argv[1]), int(sys.argv[2])

dp = [defaultdict(int) for _ in range(K + 1)]
dp[0][0] = 1
for v in range(1, N + 1):
    for k in range(min(K, v), 0, -1):
        cur, prev = dp[k], dp[k - 1]
        for s, c in list(prev.items()):
            cur[s + v] += c

uniq = [s for s, c in dp[K].items() if c == 1]
print(sum(uniq))
