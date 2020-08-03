@file:Suppress(
    "UNCHECKED_CAST", "GradleDynamicVersion", "UnstableApiUsage",
    "SpellCheckingInspection", "SafeCastWithReturn",
    "NestedLambdaShadowedImplicitParameter"
)

import de.fayard.versions.bootstrapRefreshVersions

buildscript {
    // 从 versions.properties 文件中查找最新依赖
    val versions = File(rootDir, "versions.properties").readLines()
        .filter { it.contains("=") && !it.startsWith("#") }
        .map { it.substringBeforeLast("=") to it.substringAfterLast("=") }
        .toMap()

    fun dep(group: String, artifact: String, versionKey: String? = null) =
        "$group:$artifact:" + versions[versionKey ?: "version.$group..$artifact"]

    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        maven("https://dl.bintray.com/kotlin/kotlin-eap/")
    }

    listOf(
        "de.fayard:refreshVersions:0.9.4",
        dep("org.jetbrains.kotlin", "kotlin-gradle-plugin")
    ).forEach { dependencies.classpath(it) }
}

bootstrapRefreshVersions()
