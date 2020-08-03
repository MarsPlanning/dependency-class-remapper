import extensions.*
import dependencies.local.*
import dependencies.cloud.*

buildscript {
    repositories {
        // 本地 Gradle 插件
        maven("${System.getenv("MARS_PROJECT_ROOT")}/internal-api/.release/")
        // 网络 Maven 仓库
        google()
    }
    dependencies.classpath("com.mars.gradle.plugin:global:1.0")
}

plugins { kotlin; `java-gradle-plugin` }

allprojects {
    setupRepositories()
    configInject()
}

sourceSets["main"].java.srcDir("kotlin")

dependencies.implementationOf(
    Kotlin.stdlib.jdk8,
    Deps.asm.base,
    Libraries.asm,
    Libraries.kzipper,
    Libraries.tools.jvm
)