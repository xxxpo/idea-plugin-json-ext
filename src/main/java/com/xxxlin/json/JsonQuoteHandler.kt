// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json

import com.intellij.codeInsight.editorActions.MultiCharQuoteHandler
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.xxxlin.json.editor.JsonTypedHandler.Companion.processPairedBracesComma
import com.xxxlin.json.psi.JsonStringLiteral

/**
 * @author Mikhail Golubev
 */
class JsonQuoteHandler : SimpleTokenSetQuoteHandler(JsonTokenSets.STRING_LITERALS), MultiCharQuoteHandler {
    override fun getClosingQuote(iterator: HighlighterIterator, offset: Int): CharSequence {
        val tokenType = iterator.tokenType
        if (tokenType === TokenType.WHITE_SPACE) {
            val index = iterator.start - 1
            if (index >= 0) {
                return iterator.document.charsSequence[index].toString()
            }
        }
        return if (tokenType === JsonElementTypes.SINGLE_QUOTED_STRING) "'" else "\""
    }

    override fun insertClosingQuote(editor: Editor, offset: Int, file: PsiFile, closingQuote: CharSequence) {
        val element = file.findElementAt(offset - 1)
        val parent = element?.parent
        if (parent is JsonStringLiteral) {
            PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)
            val range = parent.getTextRange()
            if (offset - 1 != range.startOffset || !"\"".contentEquals(closingQuote)) {
                val endOffset = range.endOffset
                if (offset < endOffset) return
                if (offset == endOffset && !StringUtil.isEmpty(parent.value)) return
            }
        }
        editor.document.insertString(offset, closingQuote)
        processPairedBracesComma(closingQuote[0], editor, file)
    }
}