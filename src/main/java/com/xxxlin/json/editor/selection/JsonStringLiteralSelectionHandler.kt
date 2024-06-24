// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.editor.selection

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.codeInsight.editorActions.SelectWordUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lexer.StringLiteralLexer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.psi.JsonStringLiteral

/**
 * @author Mikhail Golubev
 */
class JsonStringLiteralSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.parent !is JsonStringLiteral) {
            return false
        }
        return !InjectedLanguageManager.getInstance(e.project).isInjectedFragment(e.containingFile)
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange> {
        val type = e.node.elementType
        val lexer = StringLiteralLexer(
            if (type === JsonElementTypes.SINGLE_QUOTED_STRING) '\'' else '"',
            type,
            false,
            "/",
            false,
            false
        )
        val result: MutableList<TextRange> = ArrayList()
        SelectWordUtil.addWordHonoringEscapeSequences(editorText, e.textRange, cursorOffset, lexer, result)

        val parent = e.parent
        result.add(ElementManipulators.getValueTextRange(parent).shiftRight(parent.textOffset))
        return result
    }
}
