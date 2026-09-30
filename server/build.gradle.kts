plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

val ktorVersion = "3.1.3"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("dev.pekt.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
    // ContentValidationTest 直接读取仓库根的 content/；不声明为输入的话，
    // 只改内容资产的批次（如预抓取）会让 test 误判 UP-TO-DATE 而跳过校验。
    inputs.dir(rootProject.layout.projectDirectory.dir("content"))
        .withPropertyName("contentAssets")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
