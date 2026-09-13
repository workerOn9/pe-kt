/**
 * remark-math 沿用 micromark 的代码围栏语义，公式块两端要求 `$$` 独占一行：
 * `$$` 同一行后面若带内容，那段内容会被当作围栏信息串丢弃，公式块还会一路吞掉后续正文
 * （整页渲染成红字原文）；单行 `$$…$$` 则被解析成行内公式，`\begin{align}`、`\tag`
 * 这类只能用于 display 的命令随之报错。这里在解析前把两种写法规范成竖排围栏：
 *   - 单行 `$$x$$` → 三行 `$$` / `x` / `$$`
 *   - `$$` 后带内容的开头或收尾 → 把 `$$` 拆到独占一行
 * 本来就独占一行的 `$$` 与围栏代码块内的内容保持原样。
 * scripts/check-math.mjs 复用同一份实现跑全库公式校验，改动时两边自动一致。
 */
export function normalizeDisplayMath(markdown: string): string {
  const lines: string[] = []
  let codeFence: string | null = null
  let inDisplayMath = false
  for (const raw of markdown.split('\n')) {
    const line = raw.trim()
    if (codeFence) {
      lines.push(raw)
      if (line.startsWith(codeFence)) codeFence = null
      continue
    }
    if (line.startsWith('```') || line.startsWith('~~~')) {
      codeFence = line.slice(0, 3)
      lines.push(raw)
      continue
    }
    if (inDisplayMath) {
      if (!line.endsWith('$$')) {
        lines.push(raw)
        continue
      }
      if (line !== '$$') lines.push(line.slice(0, -2))
      lines.push('$$')
      inDisplayMath = false
      continue
    }
    if (!line.startsWith('$$')) {
      lines.push(raw)
      continue
    }
    if (line === '$$') {
      lines.push('$$')
      inDisplayMath = true
      continue
    }
    if (line.length > 4 && line.endsWith('$$')) {
      lines.push('$$', line.slice(2, -2), '$$')
      continue
    }
    lines.push('$$', line.slice(2))
    inDisplayMath = true
  }
  return lines.join('\n')
}
