rootProject.name = "pe-kt"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// web/ 为独立 Vite 子项目（npm 构建），content/ 为纯资产目录，均不纳入 Gradle 模块。
// 后续如需 Gradle task 集成前端构建产物，再在此处 include。
include("server")
