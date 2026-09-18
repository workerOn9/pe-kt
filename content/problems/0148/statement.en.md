# Exploring Pascal's Triangle

> ⚠️ 编写底稿（WebBridge 抓取自 PE 官网，2026-09-18，登录账号会话）。仅供翻译与解析参考，**不对外公开渲染**。
> Source: https://projecteuler.net/problem=148

We can easily verify that none of the entries in the first seven rows of Pascal's triangle are divisible by $7$:

						$1$
					$1$		$1$
				$1$		$2$		$1$
			$1$		$3$		$3$		$1$
		$1$		$4$		$6$		$4$		$1$
	$1$		$5$		$10$		$10$		$5$		$1$
$1$		$6$		$15$		$20$		$15$		$6$		$1$

However, if we check the first one hundred rows, we will find that only $2361$ of the $5050$ entries are not divisible by $7$.

Find the number of entries which are not divisible by $7$ in the first one billion ($10^9$) rows of Pascal's triangle.
