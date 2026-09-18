# pe-kt 一体化运行时镜像
# 构建策略：本地产物直接打包（不在容器内跑 Gradle/npm，避免容器内拉依赖的网络问题）。
# 构建前请先执行：
#   cd web && npm run build && cd ..
#   ./gradlew :server:installDist
FROM eclipse-temurin:21-jre

WORKDIR /app

# Ktor 后端（Gradle application 插件 installDist 产物：bin/server + lib/*.jar）
COPY server/build/install/server/ /app/
# 前端静态产物（Vite build 输出，由 Ktor 通配路由托管）
COPY web/dist/ /app/web-dist/
# 题目内容资产（各题 statement/analysis/applications/solution/meta 与数据文件）
COPY content/ /app/content/

ENV PEKT_CONTENT_DIR=/app/content \
    PEKT_WEB_DIST=/app/web-dist

EXPOSE 8080

ENTRYPOINT ["/app/bin/server"]
