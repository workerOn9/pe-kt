# Problem 191: Prize Strings

A school offers cash incentive to its students for good attendance. A student is awarded a cash prize if they are NOT absent three or more times consecutively and NOT late more than once in any period.

Hence a prize string is a trinary string over {L, O, A} (Late, On time, Absent) containing no substring "AAA" and at most one "L".

Although there are 3^4 = 81 trinary strings for a period of 4 days, exactly 43 of them earn the prize:

```text
OOOO OOOA OOOL OOAO OOAA OOAL OOLO OOLA OAOO OAOA
OAOL OAAO OAAL OALO OALA OLOO OLOA OLAO OLAA AOOO
AOOA AOOL AOAO AOAA AOAL AOLO AOLA AAOO AAOA AAOL
AALO AALA ALOO ALOA ALAO ALAA LOOO LOOA LOAO LOAA
LAOO LAOA LAAO
```

How many "prize" strings are there over a period of 30 days?
