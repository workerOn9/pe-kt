import ReactMarkdown, { type Components } from 'react-markdown'
import remarkGfm from 'remark-gfm'
import remarkMath from 'remark-math'
import rehypeKatex from 'rehype-katex'
import { highlightCode } from '../lib/highlight'
import { normalizeDisplayMath } from '../lib/displayMath'

interface MarkdownProps {
  children: string
}

/**
 * 围栏代码块走 highlight.js 高亮（语言未注册或行内 code 按纯文本渲染）。
 * react-markdown 会把围栏块包成 <pre><code class="language-xxx">，这里只替换 <code>。
 */
const components: Components = {
  code({ node: _node, className, children, ...rest }) {
    const language = /language-([\w-]+)/.exec(className ?? '')?.[1] ?? null
    const html = highlightCode(String(children).replace(/\n$/, ''), language)
    if (html === null) {
      return (
        <code className={className} {...rest}>
          {children}
        </code>
      )
    }
    return (
      <code
        className={`hljs ${className ?? ''}`.trim()}
        // highlight.js 输出为可信的转义 HTML（仅 <span class> 包裹）
        dangerouslySetInnerHTML={{ __html: html }}
      />
    )
  },
}

/**
 * Markdown 渲染：支持 GFM 表格、带高亮的围栏代码块与 KaTeX 数学公式（$...$ / $$...$$）。
 * 表格是 GFM 扩展，react-markdown 默认不解析，必须挂 remark-gfm。
 * 公式先经 normalizeDisplayMath 规范围栏写法，再交给 remark-math 解析。
 * KaTeX 样式在 main.tsx 全局引入。
 */
export function Markdown({ children }: MarkdownProps) {
  return (
    <div className="markdown">
      <ReactMarkdown
        remarkPlugins={[remarkGfm, remarkMath]}
        rehypePlugins={[rehypeKatex]}
        components={components}
      >
        {normalizeDisplayMath(children)}
      </ReactMarkdown>
    </div>
  )
}
