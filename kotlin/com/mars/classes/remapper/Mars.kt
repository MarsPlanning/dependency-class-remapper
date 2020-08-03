package com.mars.classes.remapper

import com.mars.asm.visitors.AbstractClassRemapper

class MarsClassRemapper: AbstractClassRemapper() {
    override val replaceTypes = listOf(
        "com.venom.candy" to "com.mars",
        "com.venom" to "com.mars",
    )
}

class MarsStringRenamer: AbstractStringReplaceVisitor() {
    override val replaceStrings = listOf(
        "com.venom.candy" to "com.mars",
        "com.venom.library" to "com.mars.library",
        "com.venom" to "com.mars",
        "com/venom/candy" to "com/mars",
        "com/venom" to "com/mars",
    )
}