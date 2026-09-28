import { useMemo } from 'react'
import ReactMarkdown, { type Components } from 'react-markdown'
import remarkGfm from 'remark-gfm'
import remarkMath from 'remark-math'
import rehypeKatex from 'rehype-katex'
import { highlightCode } from '../lib/highlight'
import { normalizeDisplayMath } from '../lib/displayMath'

interface MarkdownProps {
  children: string
  /**
   * 相对图片资源的基地址（如 `/api/problems/244/assets`）。
   * 题面里的 `![...](start.png)` 会改写到 `${assetBase}/start.png`；
   * 绝对地址（http(s):、data:、以 / 开头）原样保留。缺省时不改写。
   */
  assetBase?: string
}

/** 绝对地址（含协议、协议相对、根相对）不做改写。 */
const ABSOLUTE_SRC = /^(?:[a-z][a-z0-9+.-]*:|\/\/|\/)/i

/**
 * 围栏代码块走 highlight.js 高亮（语言未注册或行内 code 按纯文本渲染）。
 * react-markdown 会把围栏块包成 <pre><code class="language-xxx">，这里只替换 <code>。
 */
function buildComponents(assetBase?: string): Components {
  return {
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
    img({ node: _node, src, alt, ...rest }) {
      const resolved = assetBase && src && !ABSOLUTE_SRC.test(src) ? `${assetBase}/${src}` : src
      return <img src={resolved} alt={alt ?? ''} {...rest} />
    },
  }
}

/**
 * Markdown 渲染：支持 GFM 表格、带高亮的围栏代码块与 KaTeX 数学公式（$...$ / $$...$$）。
 * 表格是 GFM 扩展，react-markdown 默认不解析，必须挂 remark-gfm。
 * 公式先经 normalizeDisplayMath 规范围栏写法，再交给 remark-math 解析。
 * KaTeX 样式在 main.tsx 全局引入。
 */
export function Markdown({ children, assetBase }: MarkdownProps) {
  const components = useMemo(() => buildComponents(assetBase), [assetBase])
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
