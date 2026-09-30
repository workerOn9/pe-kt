# 387 · Harshad 数

> 中文意译。英文原文见 [Project Euler Problem 387](https://projecteuler.net/problem=387)；抓取底稿见 `statement.en.md`。

**Harshad 数**（又称 Niven 数）是能被其数位和整除的数。$201$ 是 Harshad 数，因为它能被 $3$（数位和）整除。

把 $201$ 的最后一位截掉得到 $20$，$20$ 也是 Harshad 数；再截掉 $20$ 的最后一位得到 $2$，同样是 Harshad 数。

我们把这样**递归地截掉最后一位，始终得到 Harshad 数**的 Harshad 数称为**右截 Harshad 数**。

另外，$201/3=67$ 是素数。我们把「除以自身数位和之后结果是素数」的 Harshad 数称为**强 Harshad 数**。

现在取素数 $2011$：截掉最后一位得到 $201$，它既是强 Harshad 数、也是右截 Harshad 数。

我们把这样的素数称为**强右截 Harshad 素数**。

已知小于 $10000$ 的强右截 Harshad 素数之和为 $90619$。

求小于 $10^{14}$ 的强右截 Harshad 素数之和。
