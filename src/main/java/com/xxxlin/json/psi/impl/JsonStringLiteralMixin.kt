// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement

/**
 * @author Konstantin.Ulitin
 */
abstract class JsonStringLiteralMixin(
    node: ASTNode
) : JsonLiteralImpl(
    node
), PsiLanguageInjectionHost {
    override fun isValidHost(): Boolean {
        return true
    }

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val valueNode = node.firstChildNode
        assert(valueNode is LeafElement)
        (valueNode as LeafElement).replaceWithText(text)
        return this
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> {

        return object : JSStringLiteralEscaper<PsiLanguageInjectionHost>(this as PsiLanguageInjectionHost) {
            override val isRegExpLiteral: Boolean
                get() = false
        }
    }

    override fun subtreeChanged() {
        putUserData(JsonPsiImplUtils.STRING_FRAGMENTS, null)
    }
}
