package com.mars.classes.remapper

import com.mars.asm.visitors.AbstractAnnotationVisitor
import com.mars.asm.visitors.AbstractClassVisitor
import com.mars.asm.visitors.AbstractFieldVisitor
import com.mars.asm.visitors.AbstractMethodVisitor
import org.objectweb.asm.*

// FIXME 对 asm 的操作不成熟，待修复 kt class 相关映射
abstract class AbstractStringReplaceVisitor : AbstractClassVisitor() {

    abstract val replaceStrings: List<Pair<String, String>>

//    override fun visitField(
//        access: Int,
//        name: String?,
//        descriptor: String?,
//        signature: String?,
//        value: Any?
//    ): FieldVisitor {
//        return super.visitField(access, name, descriptor, signature, value.replace())
//    }
//
//    override fun visitMethod(
//        access: Int,
//        name: String?,
//        descriptor: String?,
//        signature: String?,
//        exceptions: Array<out String>?
//    ) = object : AbstractMethodVisitor(super.visitMethod(
//        access, name, descriptor, signature, exceptions
//    )) {
//        override fun visitLdcInsn(value: Any?) {
//            super.visitLdcInsn(value.replace())
//        }
//    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean) =
        getAnnotationVisitor(super.visitAnnotation(descriptor.replace() as? String, visible))

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ) = object : AbstractFieldVisitor(
        super.visitField(
            access,
            name,
            descriptor,
            signature,
            value.replace()
        )
    ) {
        override fun visitAnnotation(descriptor: String?, visible: Boolean) =
            getAnnotationVisitor(super.visitAnnotation(descriptor.replace() as? String, visible))

        override fun visitTypeAnnotation(
            typeRef: Int,
            typePath: TypePath?,
            descriptor: String?,
            visible: Boolean
        ) = getAnnotationVisitor(
            super.visitTypeAnnotation(
                typeRef,
                typePath,
                descriptor.replace() as? String,
                visible
            )
        )
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ) = object : AbstractMethodVisitor(
        super.visitMethod(
            access,
            name,
            descriptor,
            signature,
            exceptions
        )
    ) {
        override fun visitInvokeDynamicInsn(
            name: String?,
            descriptor: String?,
            bootstrapMethodHandle: Handle?,
            vararg bootstrapMethodArguments: Any?
        ) {
            super.visitInvokeDynamicInsn(
                name,
                descriptor,
                bootstrapMethodHandle,
                *(bootstrapMethodArguments.map { it.replace() }.toTypedArray())
            )
        }

        override fun visitLdcInsn(value: Any?) {
            super.visitLdcInsn(value.replace())
        }

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? =
            getAnnotationVisitor(super.visitAnnotation(descriptor.replace() as? String, visible))

        override fun visitAnnotationDefault() =
            getAnnotationVisitor(super.visitAnnotationDefault())

        override fun visitInsnAnnotation(
            typeRef: Int,
            typePath: TypePath?,
            descriptor: String?,
            visible: Boolean
        ) = getAnnotationVisitor(
            super.visitInsnAnnotation(
                typeRef,
                typePath,
                descriptor.replace() as? String,
                visible
            )
        )

        override fun visitLocalVariableAnnotation(
            typeRef: Int,
            typePath: TypePath?,
            start: Array<out Label>?,
            end: Array<out Label>?,
            index: IntArray?,
            descriptor: String?,
            visible: Boolean
        ) = getAnnotationVisitor(
            super.visitLocalVariableAnnotation(
                typeRef,
                typePath,
                start,
                end,
                index,
                descriptor.replace() as? String,
                visible
            )
        )

        override fun visitTryCatchAnnotation(
            typeRef: Int,
            typePath: TypePath?,
            descriptor: String?,
            visible: Boolean
        ) = getAnnotationVisitor(
            super.visitTryCatchAnnotation(
                typeRef,
                typePath,
                descriptor.replace() as? String,
                visible
            )
        )

        override fun visitParameterAnnotation(
            parameter: Int,
            descriptor: String?,
            visible: Boolean
        ) = getAnnotationVisitor(super.visitParameterAnnotation(parameter, descriptor.replace() as? String, visible))

        override fun visitTypeAnnotation(
            typeRef: Int,
            typePath: TypePath?,
            descriptor: String?,
            visible: Boolean
        ) = getAnnotationVisitor(
            super.visitTypeAnnotation(
                typeRef,
                typePath,
                descriptor.replace() as? String,
                visible
            )
        )
    }

    fun getAnnotationVisitor(base: AnnotationVisitor) = object : AbstractAnnotationVisitor(base) {
        override fun visit(name: String?, value: Any?) {
            super.visit(name, value.replace())
        }

        override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor {
            return super.visitAnnotation(name, descriptor.replace() as? String)
        }

        override fun visitEnum(name: String?, descriptor: String?, value: String?) {
            super.visitEnum(name, descriptor.replace() as? String, value.replace() as? String)
        }

        override fun visitArray(name: String?): AnnotationVisitor {
            return super.visitArray(name.replace() as? String)
        }
    }

    private fun String?.rename(): String? {
        var name = this
        replaceStrings.forEach { name?.apply { name = replace(it.first, it.second) } }
        return name
    }

    private fun Any?.replace(): Any? {
        if (this is Type) mapType()
        if (this is String) return rename()
        if (this is Array<*>) return this.map { it.replace() }.toTypedArray()
        return this
    }

    private fun Type.mapType(): Type {
        return when (sort) {
            Type.ARRAY -> {
                val remappedDescriptor = StringBuilder()
                var i = 0
                while (i < dimensions) {
                    remappedDescriptor.append('[')
                    ++i
                }
                remappedDescriptor.append(elementType.mapType().descriptor)
                return Type.getType(remappedDescriptor.toString().rename())
            }
            else -> this
        }
    }
}