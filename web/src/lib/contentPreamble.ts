/**
 * 题面/解析正文的开头处理。
 *
 * AGENTS.md 的单题流水线把题目名称写在正文首行（`# 题目名称`），而详情页页头已经有大标题、
 * Tab 也已标明是题面还是解析，标题在正文里再出现一遍就是重复的，渲染前摘掉即可。
 * 紧随标题的「中文意译」引用块是题面内容的一部分，留在原处，只由 CSS 压一档字号。
 *
 * 只处理开头的第一行标题，正文中间的标题原样保留。
 */
export function stripLeadingHeading(markdown: string): string {
  const lines = markdown.split('\n')
  if (!lines[0]?.startsWith('# ')) return markdown
  const rest = lines.slice(1)
  if ((rest[0] ?? '').trim() === '') rest.shift()
  return rest.join('\n')
}
