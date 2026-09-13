// 根项目：仅做聚合，依赖源统一在 settings.gradle.kts 管理（见 docs/05-build-toolchain.md）。
// 子项目不要再声明 repositories。

plugins {
    kotlin("jvm") version "2.1.21" apply false
    kotlin("plugin.serialization") version "2.1.21" apply false
}

allprojects {
    group = "dev.pekt"
    version = "0.1.0-SNAPSHOT"
}
