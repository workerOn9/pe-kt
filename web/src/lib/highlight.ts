import hljs from 'highlight.js/lib/core'
import bash from 'highlight.js/lib/languages/bash'
import cpp from 'highlight.js/lib/languages/cpp'
import java from 'highlight.js/lib/languages/java'
import javascript from 'highlight.js/lib/languages/javascript'
import json from 'highlight.js/lib/languages/json'
import kotlin from 'highlight.js/lib/languages/kotlin'
import python from 'highlight.js/lib/languages/python'
import rust from 'highlight.js/lib/languages/rust'
import typescript from 'highlight.js/lib/languages/typescript'
import 'highlight.js/styles/github.css'

/**
 * 全站代码高亮入口（D-03：highlight.js）。按需注册语言，避免打包整包。
 * 别名（ts/js/py/c++ 等）由各语言定义自带，围栏块里写 ```ts 也能命中。
 */
const languages = { bash, cpp, java, javascript, json, kotlin, python, rust, typescript }

for (const [name, definition] of Object.entries(languages)) {
  hljs.registerLanguage(name, definition)
}

/**
 * 高亮代码；语言未注册时返回 null，调用方自行按纯文本渲染。
 * 返回值是 highlight.js 生成的转义 HTML（只含 <span class>），可安全注入。
 */
export function highlightCode(code: string, language: string | null | undefined): string | null {
  if (!language || !hljs.getLanguage(language)) return null
  return hljs.highlight(code, { language }).value
}
