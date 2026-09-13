import { useMemo } from 'react'
import { highlightCode } from '../lib/highlight'

interface CodeBlockProps {
  code: string
  title?: string
}

/** Kotlin 代码高亮块（浅色 github 主题，适合阅读场景） */
export function CodeBlock({ code, title }: CodeBlockProps) {
  const html = useMemo(() => highlightCode(code, 'kotlin') ?? code, [code])

  return (
    <figure className="code-block">
      {title && <figcaption className="code-block__title">{title}</figcaption>}
      <pre>
        <code
          className="hljs language-kotlin"
          // highlight.js 输出为可信的转义 HTML（仅 <span class> 包裹）
          dangerouslySetInnerHTML={{ __html: html }}
        />
      </pre>
    </figure>
  )
}
