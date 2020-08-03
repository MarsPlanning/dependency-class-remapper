package com.mars.classes.remapper

import com.mars.kzipper.model.asZip
import com.mars.kzipper.operate
import com.mars.tools.ktx.forEach
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry

fun main(args: Array<String>) {
    // 列出所有 jar/aar
    val folder = File(System.getenv("MARS_PROJECT_ROOT"), "communal/.release")
    folder.forEach(recursively = true) {
        if (it.isFile &&
            !it.name.endsWith("-sources.jar") &&
            !it.name.endsWith("-javadoc.jar") &&
            it.name.endsWith(".jar") || it.name.endsWith(".aar")
        ) it.asZip.operate {
            level = 0
            // 覆盖
            outputFile = it
            modifyEntry { entry ->
                ZipEntry(
                    entry.name
                        .replace("com/venom/candy", "com/mars")
                        .replace("com/venom", "com/mars")
                )
            }
            hookCompression { entry, inputStream, outputStream ->
                when {
                    // 获取新的 class 内容并写入
                    entry.name.endsWith(".class") -> remapping(inputStream, outputStream)
                    // 复制原来的条目
                    else -> copyOriginalEntry(inputStream, outputStream)
                }
            }
        }

        if (it.isFile &&
            it.name.endsWith(".pom") ||
            it.name.endsWith(".xml") ||
            it.name.endsWith(".module")
        ) it.writeText(it.readText()
            .replace("com.venom.library", "com.mars.library")
            .replace("com.venom.gradle", "com.mars.gradle")
        )

        if (it.isFile && it.name.endsWith(".plugin")) it.renameTo(
            File(it.parent, it.name.replace("com.venom", "com.mars"))
        )
    }

}

fun remapping(inputStream: InputStream, outputStream: OutputStream) {
    var inputBytes = inputStream.readBytes()
    arrayOf(
        MarsClassRemapper(),
        MarsStringRenamer(),
    ).forEach {
        val classReader = ClassReader(inputBytes)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        classReader.accept(it.apply { classVisitor = classWriter }, ClassReader.EXPAND_FRAMES)
        inputBytes = classWriter.toByteArray()
    }
    outputStream.write(inputBytes)
    outputStream.flush()
}