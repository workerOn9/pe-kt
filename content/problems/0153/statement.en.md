# Problem 153: Investigating Gaussian Integers

As we all know the equation $x^2 = -1$ has no solutions for real $x$.

If we however introduce the imaginary number $i$, this equation has two solutions: $x = i$ and $x = -i$.

If we go a step further, the equation $(x + iy)^2 = -1$ has two complex solutions: $x + iy = 1 + i\cdot 0$ and $x + iy = -1 + i\cdot 0$ ...

A Gaussian Integer is a complex number $a + bi$ such that both $a$ and $b$ are integers.

The regular integers are also Gaussian integers (with $b = 0$).

To distinguish them from Gaussian integers with $b \neq 0$, we call such integers "rational integers."

A Gaussian integer $a + bi$ is called a divisor of a rational integer $n$ if the result $\frac{n}{a + bi}$ is also a Gaussian integer.

For example, dividing $10$ by $2 + i$: multiply numerator and denominator by the complex conjugate of $2 + i$, which is $2 - i$:

$$\frac{10}{2 + i} = \frac{10(2 - i)}{(2 + i)(2 - i)} = \frac{20 - 10i}{4 + 1} = \frac{20 - 10i}{5} = 4 - 2i$$

So $2 + i$ is a divisor of $10$.

Note that $3 + 2i$ is not a divisor of $7$ because $\frac{7}{3 + 2i} = \frac{7(3 - 2i)}{9 + 4} = \frac{21 - 14i}{13}$ is not a Gaussian integer.

Also note that if $a + bi$ is a divisor of a rational integer $n$, then its complex conjugate $a - bi$ is also a divisor of $n$.

In fact, $10$ has six divisors such that the real part is positive: $1+i, 1-i, 2+2i, 2-2i, 5, 1$ and their conjugates, plus $10$.

For each positive integer $n$, let $s(n)$ be the sum of all Gaussian integer divisors of $n$ with positive real parts.

What is $\sum_{n=1}^{10^8} s(n)$?

Source: https://projecteuler.net/problem=153
