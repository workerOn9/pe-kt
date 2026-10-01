#!/usr/bin/env node
/**
 * 从 Project Euler 官网批量抓取题目底稿（经 Kimi WebBridge 操作用户已登录的 Chrome）。
 *
 * 用法（在仓库根目录）：
 *   node scripts/fetch-pe.mjs --from 301 --to 325 [--dry-run] [--force]
 *
 * 每题只产出"机械抓取"部分，中文翻译由后续步骤补齐：
 *   content/problems/XXXX/statement.en.md   英文原文底稿（不进入渲染路径）
 *   content/problems/XXXX/meta.json         草稿 meta（status="draft"，titleZh 待翻译）
 *   content/problems/XXXX/<image>           题面引用的图片（若有，文件名取 PE 资源名）
 *
 * 红线与节奏（AGENTS.md）：
 *   - 逐题 navigate + 读 DOM，每题间隔 3.5s；禁止用页面内 fetch() 批量拉题（触发 403）；
 *   - 图片走页面内 fetch（同源静态资源，单张取回），不属于批量拉题；
 *   - 未登录 / 403 / 题号不存在时页面没有 .problem_content，脚本立即中止；
 *   - 已存在 statement.en.md 的题目默认跳过（断点续抓），--force 覆盖重抓。
 */
import { existsSync, mkdirSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const DAEMON = process.env.PEKT_WEBBRIDGE ?? 'http://127.0.0.1:10086'
const SESSION = 'pe-fetch-problems'
const GROUP_TITLE = 'PE 题库预抓取'
const INTERVAL_MS = 3500

const args = process.argv.slice(2)
const getArg = (name, fallback) => {
  const i = args.indexOf(name)
  return i >= 0 && i + 1 < args.length ? args[i + 1] : fallback
}
const from = Number(getArg('--from', '0'))
const to = Number(getArg('--to', '0'))
const dryRun = args.includes('--dry-run')
const force = args.includes('--force')

if (!Number.isInteger(from) || !Number.isInteger(to) || from <= 0 || to < from) {
  console.error('用法：node scripts/fetch-pe.mjs --from 301 --to 325 [--dry-run] [--force]')
  process.exit(1)
}

const repoRoot = join(dirname(fileURLToPath(import.meta.url)), '..')
const problemsDir = join(repoRoot, 'content', 'problems')

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

async function call(action, callArgs) {
  const res = await fetch(`${DAEMON}/command`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ action, args: callArgs, session: SESSION }),
  })
  const body = await res.json()
  if (!body.ok) {
    throw new Error(`${action} 失败：${body.error?.code ?? 'unknown'} ${body.error?.message ?? ''}`)
  }
  return body.data
}

/** 页面内抽取：题面（MathJax → $/$$ LaTeX、图片 → 本地文件名引用）、标题、官方难度与解题人数。 */
const EXTRACT_JS = `(() => {
  const c = document.querySelector('.problem_content');
  if (!c) return JSON.stringify({ error: 'no_content', body: document.body.innerText.slice(0, 200) });
  const imgs = [];
  const nameBySrc = new Map();
  const usedNames = new Set();
  c.querySelectorAll('img').forEach((img, i) => {
    const raw = img.getAttribute('src') || '';
    if (!raw || raw.startsWith('data:')) return;
    const abs = new URL(raw, location.href).href;
    let name = nameBySrc.get(abs);
    if (!name) {
      const base = (new URL(abs).pathname.split('/').pop() || 'image').replace(/[^A-Za-z0-9._-]/g, '_');
      name = base;
      let k = 1;
      while (usedNames.has(name)) {
        const dot = base.lastIndexOf('.');
        name = dot > 0 ? base.slice(0, dot) + '_' + k + base.slice(dot) : base + '_' + k;
        k++;
      }
      usedNames.add(name);
      nameBySrc.set(abs, name);
      imgs.push({ src: abs, name: name });
    }
    const alt = img.getAttribute('alt') || '';
    img.replaceWith(document.createTextNode('\\n\\n![' + alt + '](' + name + ')\\n\\n'));
  });
  c.querySelectorAll('mjx-container').forEach((m) => {
    const math = m.querySelector('[data-mml-node="math"]');
    const latex = math ? math.getAttribute('data-latex') : '';
    if (m.getAttribute('display') === 'true') {
      // 独立公式：插入 pre-wrap 块级节点，innerText 才会保留换行（否则会被 CSS 空白折叠吃成空格）
      const block = document.createElement('div');
      block.style.whiteSpace = 'pre-wrap';
      block.textContent = '\\n$$\\n' + latex + '\\n$$\\n';
      m.replaceWith(block);
    } else {
      m.replaceWith(document.createTextNode('$' + latex + '$'));
    }
  });
  // 纯 HTML 的上下标与链接按原文标记保留（innerText 会抹掉这些标记）
  c.querySelectorAll('sup, sub').forEach((el) => {
    const tag = el.tagName.toLowerCase();
    el.replaceWith(document.createTextNode('<' + tag + '>' + el.textContent + '</' + tag + '>'));
  });
  c.querySelectorAll('a[href]').forEach((a) => {
    const text = (a.innerText || '').trim();
    const href = a.getAttribute('href') || '';
    if (!href || href.startsWith('javascript:')) return;
    const abs = href.startsWith('http') ? href : new URL(href, location.href).href;
    a.replaceWith(document.createTextNode('[' + text + '](' + abs + ')'));
  });
  const tips = Array.from(document.querySelectorAll('.tooltiptext_right')).map((t) => t.textContent);
  const tip = tips.find((t) => t.includes('Published on')) || '';
  const diff = tip.match(/Difficulty:\\s*Level\\s*(\\d+)\\s*\\[(\\d+)%\\]/);
  const solved = tip.match(/solved by (\\d+)/);
  const h = document.querySelector('h2');
  return JSON.stringify({
    title: h ? h.innerText.trim() : null,
    level: diff ? Number(diff[1]) : null,
    percent: diff ? Number(diff[2]) : null,
    solvedBy: solved ? Number(solved[1]) : null,
    imgs: imgs,
    stmt: c.innerText.replace(/\\u00a0/g, ' ').replace(/[ \\t]+\\n/g, '\\n').replace(/\\n{3,}/g, '\\n\\n').trim(),
  });
})()`

/** 页面内取静态资源为 base64（同源带登录态；单张图片，非批量拉题）。 */
const downloadJs = (url) => `(async () => {
  try {
    const r = await fetch(${JSON.stringify(url)}, { credentials: 'include' });
    if (!r.ok) return JSON.stringify({ error: 'http_' + r.status });
    const bytes = new Uint8Array(await r.arrayBuffer());
    let bin = '';
    for (let i = 0; i < bytes.length; i += 0x8000) {
      bin += String.fromCharCode.apply(null, bytes.subarray(i, i + 0x8000));
    }
    return JSON.stringify({ b64: btoa(bin), size: bytes.length });
  } catch (e) {
    return JSON.stringify({ error: String(e) });
  }
})()`

const localDate = () => new Intl.DateTimeFormat('sv-SE').format(new Date())

let firstNav = true
let fetched = 0

for (let id = from; id <= to; id++) {
  const dir = join(problemsDir, String(id).padStart(4, '0'))
  const enPath = join(dir, 'statement.en.md')
  if (existsSync(enPath) && !force) {
    console.log(`[${id}] 已存在 statement.en.md，跳过`)
    continue
  }

  await call('navigate', firstNav
    ? { url: `https://projecteuler.net/problem=${id}`, newTab: true, group_title: GROUP_TITLE }
    : { url: `https://projecteuler.net/problem=${id}` })
  firstNav = false
  await sleep(INTERVAL_MS)

  const data = await call('evaluate', { code: EXTRACT_JS })
  const page = JSON.parse(data.value)
  if (page.error) {
    console.error(`[${id}] 中止：页面无题面内容（未登录 / 403 / 题号不存在）：${page.body}`)
    console.error('已抓取的题目不受影响；排查后从当前题号续抓。')
    process.exit(2)
  }

  const stat = [`[${id}]`, page.title, `Level ${page.level} [${page.percent}%]`, `solved ${page.solvedBy}`, `${page.stmt.length} 字符`]
  if (page.imgs.length > 0) stat.push(`图片 ${page.imgs.length} 张`)

  if (dryRun) {
    console.log(stat.join(' | '))
    console.log(page.stmt.slice(0, 300))
    continue
  }

  mkdirSync(dir, { recursive: true })
  for (const img of page.imgs) {
    const res = JSON.parse((await call('evaluate', { code: downloadJs(img.src) })).value)
    if (res.error) {
      console.error(`[${id}] 图片下载失败：${img.src} -> ${res.error}`)
      process.exit(3)
    }
    writeFileSync(join(dir, img.name), Buffer.from(res.b64, 'base64'))
    console.log(`[${id}] 图片 ${img.name}（${res.size} 字节）`)
  }
  writeFileSync(enPath, page.stmt + '\n')

  const meta = {
    id,
    title: page.title,
    titleZh: '',
    difficulty: page.percent,
    difficultyLevel: `Level ${page.level}`,
    tags: [],
    solvedBy: page.solvedBy,
    hasVisualization: false,
    sourceUrl: `https://projecteuler.net/problem=${id}`,
    fetchedAt: localDate(),
    status: 'draft',
  }
  writeFileSync(join(dir, 'meta.json'), JSON.stringify(meta, null, 2) + '\n')
  fetched++
  console.log(stat.join(' | '))
}

console.log(`完成：新抓取 ${fetched} 题（${from}-${to}，dry-run=${dryRun}）。`)
console.log('下一步：翻译 statement.md / titleZh，跑 ./gradlew :server:test 校验后提交。')
