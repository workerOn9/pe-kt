#!/usr/bin/env node
/**
 * 全库公式校验：题面/解析/现实应用与基准报告，都按前端同一条管线
 * （normalizeDisplayMath → remark-math → KaTeX）跑一遍，任何 KaTeX 解析错误都以
 * 「文件:行 + 报错 + 片段」列出并非零退出。
 *
 * ContentValidationTest 管的是内容资产的 Markdown 侧写法（围栏、字段、答案一致性），
 * LaTeX 层面的错字（`\*` 应为 `^{*}`、环境名笔误……）只有 KaTeX 能发现——页面上的表现是
 * 红字原文，构建期不拦就会漏到线上。顺带查 `\begin{align}`：它会按行自动编号，统一改用 `aligned`。
 *
 * 用法：npm run check:math（已挂在 npm run build 前面，CI 的前端构建同样会跑到）
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import katex from 'katex'
import remarkMath from 'remark-math'
import remarkParse from 'remark-parse'
import remarkGfm from 'remark-gfm'
import { unified } from 'unified'
import { normalizeDisplayMath } from '../src/lib/displayMath.ts'

const webDir = path.resolve(fileURLToPath(new URL('.', import.meta.url)), '..')

/** 与 server 的 ContentIndex.resolveContentDir 保持同一套候选路径 */
function resolveContentDir() {
  const candidates = [
    process.env.PEKT_CONTENT_DIR,
    path.join(webDir, '../content'),
    path.join(webDir, '../../content'),
    path.join(webDir, 'content'),
  ].filter(Boolean)
  for (const dir of candidates) {
    if (fs.existsSync(path.join(dir, 'problems'))) return dir
  }
  console.error(`未找到 content 目录（已尝试：${candidates.join('、')}）`)
  process.exit(2)
}

/** 收集参与渲染的 markdown；statement.en.md 等原文存档前端不渲染，跳过 */
function collectMarkdown(contentDir) {
  const files = []
  const problemsDir = path.join(contentDir, 'problems')
  for (const entry of fs.readdirSync(problemsDir, { withFileTypes: true })) {
    if (!entry.isDirectory()) continue
    const dir = path.join(problemsDir, entry.name)
    for (const name of fs.readdirSync(dir)) {
      if (name.endsWith('.md') && !name.endsWith('.en.md')) files.push(path.join(dir, name))
    }
  }
  const benchmarksDir = path.join(contentDir, 'benchmarks')
  if (fs.existsSync(benchmarksDir)) {
    for (const name of fs.readdirSync(benchmarksDir)) {
      if (name.endsWith('.md') && !name.endsWith('.en.md')) files.push(path.join(benchmarksDir, name))
    }
  }
  return files.sort()
}

const contentDir = resolveContentDir()
const targets = collectMarkdown(contentDir)
const failures = []
let displayCount = 0
let inlineCount = 0

/** 多行对齐公式统一用 aligned：align 会按行自动编号，编号挂在内容列最右侧很难看 */
const AUTO_NUMBERED_ENV = /\\begin\{align\}/

for (const file of targets) {
  const raw = fs.readFileSync(file, 'utf8')
  const originalLines = raw.split('\n')

  originalLines.forEach((text, index) => {
    if (AUTO_NUMBERED_ENV.test(text)) {
      failures.push({
        file: path.relative(webDir, file),
        line: index + 1,
        message: '多行对齐公式请用 \\begin{aligned}（align 会对每行自动编号）',
        snippet: text.trim().slice(0, 70),
      })
    }
  })

  const tree = unified()
    .use(remarkParse)
    .use(remarkGfm)
    .use(remarkMath)
    .parse(normalizeDisplayMath(raw))

  const visit = (node) => {
    if (node.type === 'math' || node.type === 'inlineMath') {
      node.type === 'math' ? displayCount++ : inlineCount++
      try {
        // 与 rehype-katex 默认行为等价：解析失败只会渲染成红字，这里显式抛出来拦住
        katex.renderToString(node.value, {
          displayMode: node.type === 'math',
          throwOnError: true,
          strict: false,
        })
      } catch (err) {
        const first = node.value.split('\n').find((line) => line.trim() !== '')?.trim() ?? ''
        const line = originalLines.findIndex((l) => l.includes(first.slice(0, 24))) + 1
        failures.push({
          file: path.relative(webDir, file),
          line,
          message: err.message,
          snippet: first.slice(0, 70),
        })
      }
    }
    for (const child of node.children ?? []) visit(child)
  }
  for (const child of tree.children) visit(child)
}

if (failures.length === 0) {
  console.log(
    `公式校验通过：${targets.length} 个文件、${displayCount} 条 display 公式、${inlineCount} 条行内公式，无 KaTeX 报错`,
  )
  process.exit(0)
}

console.error(`公式校验失败：${failures.length} 处\n`)
for (const f of failures) {
  console.error(`  ${f.file}${f.line ? `:${f.line}` : ''}`)
  console.error(`    ${f.message}`)
  console.error(`    片段：${f.snippet}\n`)
}
process.exit(1)
