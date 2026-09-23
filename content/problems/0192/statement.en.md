# Problem 192: Best Approximations

Let $x$ be a real number. A best approximation to $x$ for the denominator bound $d$ is a rational number $r/s$ in reduced form, with $s \le d$, such that any rational number which is closer to $x$ than $r/s$ has a denominator larger than $d$:

$$\left|\frac{p}{q} - x\right| < \left|\frac{r}{s} - x\right| \Rightarrow q > d$$

For example, the best approximation to $\sqrt{13}$ for the denominator bound 20 is $18/5$ and the best approximation to $\sqrt{13}$ for the denominator bound 30 is $101/28$.

Find the sum of all denominators of the best approximations to $\sqrt{n}$ for the denominator bound $10^{12}$, where $n$ is not a perfect square and $n \le 100000$, $n \ge 2$.
