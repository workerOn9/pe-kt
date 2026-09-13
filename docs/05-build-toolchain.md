# 构建与依赖源配置（Build Toolchain）

> 仓库内声明**官方默认源**，本地（国内网络）的加速靠**机器级配置**兜底，不进版本库。
> 这样 Vercel 构建机与 GitHub Actions 跑的是与仓库声明一致的上游源，而本机开发照样走国内镜像。

## 原则

1. **仓库只声明官方源**：Gradle 的 plugin/dependency 仓库用 `gradlePluginPortal()` + `mavenCentral()`，wrapper 发行版指向 `services.gradle.org`，npm 不做仓库级 registry 覆盖。Vercel 与 CI 都用这套，不依赖任何国内镜像的可达性。
2. **本机加速走机器级配置**：`~/.gradle/init.gradle.kts` 与 `~/.npmrc` 会在本机构建时把源改写成国内镜像。这两个文件不属于仓库，对海外构建机无影响。
3. **源变更只改仓库声明 + 本机配置两处**，不要往子项目里散落私有 `repositories`，也不要提交指向镜像的 lockfile。

## settings.gradle.kts（根项目）

```kotlin
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
```

注意：`dependencyResolutionManagement` 里不要再声明 `repositories { ... }` 于子项目（`server/build.gradle.kts`），否则两处都生效，子项目声明会绕过这里的统一管理。

## Gradle Wrapper 发行版

`gradle/wrapper/gradle-wrapper.properties`：

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10-bin.zip
```

（版本号以项目实际采用的为准。）

## 前端依赖（web/ 子项目）

仓库内**不放** `.npmrc`，npm 走官方默认 `https://registry.npmjs.org/`。
`web/package-lock.json` 的 `resolved` 字段同样全部指向 `registry.npmjs.org`——lockfile 里的 tarball 地址不会因本机用什么源而改变，海外构建机 `npm ci` 才不会去拉国内镜像。

本机（国内网络）的加速由 `~/.npmrc` 的 `registry=https://registry.npmmirror.com` 提供。npm 的 `replace-registry-host` 默认值（`npmjs`）会在安装时把 lockfile 里的官方源地址替换成本机配置的 registry，因此本机 `npm ci` 依然走 npmmirror，而 lockfile 本身保持官方源不变。

## 本机配置（不属于仓库，换机器需自建）

| 文件 | 作用 |
|------|------|
| `~/.gradle/init.gradle.kts` | 本机 Gradle 全局镜像：腾讯 → 阿里 → 官方，覆盖 pluginManagement 与 allprojects 仓库 |
| `~/.npmrc` | 本机 npm registry 指向 npmmirror |

两者的取值都只影响本机；删除后本机首构建会变慢，但不会改变构建结果。

## 变更纪律

- 新增任何依赖源前先确认本节是否已覆盖，避免子项目里散落私有 `repositories` 声明；
- 仓库声明的源改动（`settings.gradle.kts` / `gradle-wrapper.properties` / lockfile）只走官方源；
- 换机器或换 CI 平台时，先确认能否直连官方源，再决定是否需要自建本机镜像配置。
