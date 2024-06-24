// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.editor

import com.intellij.codeInsight.editorActions.EnterHandler
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ObjectUtils
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.JsonLanguage
import com.xxxlin.json.editor.JsonEditorOptions.Companion.instance
import com.xxxlin.json.psi.*

class JsonEnterHandler : EnterHandlerDelegateAdapter() {
    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffsetRef: Ref<Int>,
        caretAdvanceRef: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?
    ): EnterHandlerDelegate.Result {
        if (!instance.COMMA_ON_ENTER) {
            return EnterHandlerDelegate.Result.Continue
        }

        val language = EnterHandler.getLanguage(dataContext) as? JsonLanguage
            ?: return EnterHandlerDelegate.Result.Continue

        val caretOffset = caretOffsetRef.get()
        val psiAtOffset = file.findElementAt(caretOffset) ?: return EnterHandlerDelegate.Result.Continue

        if (psiAtOffset is LeafPsiElement && handleComma(caretOffsetRef, psiAtOffset, editor)) {
            return EnterHandlerDelegate.Result.Continue
        }

        val literal = ObjectUtils.tryCast(psiAtOffset.parent, JsonValue::class.java)
        if (literal != null && (literal !is JsonStringLiteral || !language.hasPermissiveStrings())) {
            handleJsonValue(literal, editor, caretOffsetRef)
        }

        return EnterHandlerDelegate.Result.Continue
    }

    companion object {
        private fun handleComma(caretOffsetRef: Ref<Int>, psiAtOffset: PsiElement, editor: Editor): Boolean {
            var nextSibling = psiAtOffset
            var hasNewlineBefore = false
            while (nextSibling is PsiWhiteSpace) {
                hasNewlineBefore = nextSibling.getText().contains("\n")
                nextSibling = nextSibling.getNextSibling()
            }

            val leafPsiElement = ObjectUtils.tryCast(nextSibling, LeafPsiElement::class.java)
            val elementType = leafPsiElement?.elementType
            if (elementType === JsonElementTypes.COMMA || elementType === JsonElementTypes.R_CURLY) {
                var prevSibling = nextSibling.prevSibling
                while (prevSibling is PsiWhiteSpace) {
                    prevSibling = prevSibling.getPrevSibling()
                }

                if (prevSibling is JsonProperty && prevSibling.value != null) {
                    var offset =
                        if (elementType === JsonElementTypes.COMMA) nextSibling.textRange.endOffset else prevSibling.getTextRange().endOffset
                    if (offset < editor.document.textLength) {
                        if (elementType === JsonElementTypes.R_CURLY && hasNewlineBefore) {
                            editor.document.insertString(offset, ",")
                            offset++
                        }
                        caretOffsetRef.set(offset)
                    }
                    return true
                }
                return false
            }

            if (nextSibling is JsonProperty) {
                var prevSibling = nextSibling.getPrevSibling()
                while (prevSibling is PsiWhiteSpace || prevSibling is PsiErrorElement) {
                    prevSibling = prevSibling.prevSibling
                }

                if (prevSibling is JsonProperty) {
                    val offset = prevSibling.getTextRange().endOffset
                    if (offset < editor.document.textLength) {
                        editor.document.insertString(offset, ",")
                        caretOffsetRef.set(offset + 1)
                    }
                    return true
                }
            }

            return false
        }

        private fun handleJsonValue(literal: JsonValue, editor: Editor, caretOffsetRef: Ref<Int>) {
            val parent = literal.parent
            if (parent !is JsonProperty || parent.value !== literal) {
                return
            }

            var nextSibling = parent.getNextSibling()
            while (nextSibling is PsiWhiteSpace || nextSibling is PsiErrorElement) {
                nextSibling = nextSibling.nextSibling
            }

            var offset = literal.textRange.endOffset

            if (literal is JsonObject || literal is JsonArray) {
                if (nextSibling is LeafPsiElement && nextSibling.elementType === JsonElementTypes.COMMA
                    || nextSibling !is JsonProperty
                ) {
                    return
                }
                val document = editor.document
                if (offset < document.textLength) {
                    document.insertString(offset, ",")
                }
                return
            }

            if (nextSibling is LeafPsiElement && nextSibling.elementType === JsonElementTypes.COMMA) {
                offset = nextSibling.getTextRange().endOffset
            } else {
                val document = editor.document
                if (offset < document.textLength) {
                    document.insertString(offset, ",")
                }
                offset++
            }

            if (offset < editor.document.textLength) {
                caretOffsetRef.set(offset)
            }
        }
    }
}
