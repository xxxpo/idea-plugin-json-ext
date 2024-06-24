// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.highlighting

import com.intellij.lexer.StringLiteralLexer
import com.intellij.psi.tree.IElementType

class JsonStringLiteralLexer(
    quoteChar: Char,
    originalLiteralToken: IElementType?,
    canEscapeEol: Boolean,
    private val myIsPermissiveDialect: Boolean
) : StringLiteralLexer(
    quoteChar, originalLiteralToken, canEscapeEol,
    if (myIsPermissiveDialect) PERMISSIVE_ESCAPES else "/", false, myIsPermissiveDialect
) {
    override fun handleSingleSlashEscapeSequence(): IElementType {
        return if (myIsPermissiveDialect) myOriginalLiteralToken else super.handleSingleSlashEscapeSequence()
    }

    override fun shouldAllowSlashZero(): Boolean {
        return myIsPermissiveDialect
    }

    companion object {
        private val PERMISSIVE_ESCAPES: String

        init {
            val escapesBuilder = StringBuilder("/")
            var c = '\u0001'
            while (c < '\u00ad') {
                if (c != 'x' && c != 'u' && !Character.isDigit(c) && c != '\n' && c != '\r') {
                    escapesBuilder.append(c)
                }
                c++
            }
            PERMISSIVE_ESCAPES = escapesBuilder.toString()
        }
    }
}
