设某学校为出勤表现好的孩子发放现金奖励：若在 n 天期间**连续缺席 3 天**，或**迟到超过 1 次**，则取消获奖资格。

在 n 天期间，每个孩子对应一个由 L（迟到）、O（准时）、A（缺席）构成的三元字符串。

虽然 4 天期间共有 $3^4 = 81$ 个三元字符串，但恰好有 43 个字符串能获得奖励，例如：

```text
OOOO OOOA OOOL OOAO OOAA OOAL OOLO OOLA OAOO OAOA
OAOL OAAO OAAL OALO OALA OLOO OLOA OLAO OLAA AOOO
AOOA AOOL AOAO AOAA AOAL AOLO AOLA AAOO AAOA AAOL
AALO AALA ALOO ALOA ALAO ALAA LOOO LOOA LOAO LOAA
LAOO LAOA LAAO
```

求在 30 天期间，共有多少个能获奖的（"prize"）字符串？
