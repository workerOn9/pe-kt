# Problem 155: Counting Capacitor Circuits

An electric circuit uses exclusively identical capacitors of the same value $C$.

The capacitors can be connected in series or in parallel to form sub-units, which can then be connected in series or in parallel with other capacitors or other sub-units to form larger sub-units, and so on up to a final circuit.

Using this simple procedure and up to $n$ identical capacitors, we can make circuits having a range of different total capacitances. For example, using up to $n=3$ capacitors of $60\,\mu F$ each, we can obtain the following 7 distinct total capacitance values:

$$60\mu F, 90\mu F, 120\mu F, 180\mu F, 40\mu F, 45\mu F, 135\mu F$$

If we denote by $D(n)$ the number of distinct total capacitance values we can obtain when using up to $n$ equal-valued capacitors and the simple procedure described above, we have:

$$D(1) = 1, D(2) = 2, D(3) = 7, D(4) = 15$$

Find $D(18)$.

Reminder: When connecting capacitors $C_1, C_2, \dots$ in parallel, the total capacitance is $C_{total} = C_1 + C_2 + \dots$, whereas when connecting them in series, the overall capacitance is given by $\frac{1}{C_{total}} = \frac{1}{C_1} + \frac{1}{C_2} + \dots$.

Source: https://projecteuler.net/problem=155