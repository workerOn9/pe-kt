import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// 开发服务器：/api 与 /health 代理到 Ktor 后端（localhost:8080）
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/health': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
