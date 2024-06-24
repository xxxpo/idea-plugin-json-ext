package com.xxxlin.json.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil
import com.xxxlin.json.psi.JsonElement

/**
 * @author Mikhail Golubev
 */
open class JsonElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), JsonElement {
    override fun toString(): String {
        val className = javaClass.simpleName
        return StringUtil.trimEnd(className, "Impl")
    }
}
