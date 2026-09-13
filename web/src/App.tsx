import { Route, Routes } from 'react-router-dom'
import { ProblemListPage } from './pages/ProblemListPage'
import { ProblemDetailPage } from './pages/ProblemDetailPage'
import { BenchmarkPage } from './pages/BenchmarkPage'
import { ErrorMessage } from './components/ErrorMessage'

export function App() {
  return (
    <div className="site">
      <header className="site-header">
        <div className="site-header__inner">
          <a href="/" className="site-title">
            Project Euler 学习笔记
          </a>
          <p className="site-subtitle">数学思路 → Kotlin 实现 → 运行验证</p>
        </div>
      </header>
      <main className="site-main">
        <Routes>
          <Route path="/" element={<ProblemListPage />} />
          <Route path="/benchmark" element={<BenchmarkPage />} />
          <Route path="/benchmark/:slug" element={<BenchmarkPage />} />
          <Route path="/problem/:id" element={<ProblemDetailPage />} />
          <Route
            path="*"
            element={<ErrorMessage title="页面不存在" message="你访问的地址没有对应的页面。" />}
          />
        </Routes>
      </main>
      <footer className="site-footer">
        <p>Kotlin 全栈驱动的 Project Euler 解析与展示</p>
        <p className="site-footer__note">
          题目解析与解法基本由 AI Agent 自行分析、计算得到，仅供参考。
        </p>
        <p className="site-footer__note">主要使用 Kimi 和 DeepSeek。</p>
      </footer>
    </div>
  )
}
