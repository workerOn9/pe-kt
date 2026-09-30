# 339 · 佩雷杜尔之子

> 中文意译。英文原文见 [Project Euler Problem 339](https://projecteuler.net/problem=339)；抓取底稿见 `statement.en.md`。

> 「他向着一处山谷走来，一条河穿过谷中；谷的两侧林木葱郁，河流两岸是平坦的草地。他看见河的一侧有一群白羊，另一侧有一群黑羊。每当一只白羊叫一声，就有一只黑羊渡过河来变成白色；每当一只黑羊叫一声，就有一只白羊渡过河来变成黑色。」
>
> ——摘自 [The Mabinogion (1877): Peredur son of Efrawg](https://en.wikisource.org/w/index.php?title=The_Mabinogion_(Guest_1877)/Peredur_the_Son_of_Evrawc&oldid=15034286) 第 108–109 页

初始时每群各有 $n$ 只羊。每次叫声出现时，两群中每只羊（无论颜色）被选中的概率相同。某只羊叫过、另一群的一只羊渡过河变色之后，佩雷杜尔可以移走若干只白羊，以最大化最终黑羊数量的期望。

记 $E(n)$ 为佩雷杜尔采取最优策略时最终黑羊数量的期望。

已知 $E(5) \approx 6.871346$（四舍五入到六位小数）。

求 $E(10\,000)$，结果四舍五入到六位小数。
