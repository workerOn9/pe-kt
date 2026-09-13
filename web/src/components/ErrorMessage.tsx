import type { ReactNode } from 'react'

interface ErrorMessageProps {
  title?: string
  message: string
  children?: ReactNode
}

/** 可读的错误提示块（API 失败 / 路由错误等场景） */
export function ErrorMessage({ title = '出错了', message, children }: ErrorMessageProps) {
  return (
    <div className="error-box" role="alert">
      <p className="error-box__title">{title}</p>
      <p className="error-box__message">{message}</p>
      {children}
    </div>
  )
}
