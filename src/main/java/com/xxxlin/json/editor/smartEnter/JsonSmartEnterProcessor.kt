// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.editor.smartEnter

import com.intellij.lang.SmartEnterProcessorWithFixers
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import com.xxxlin.json.JsonDialectUtil
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.psi.*

/**
 * This processor allows
 *
 *  * Insert colon after key inside object property
 *  * Insert comma after array element or object property
 *
 *
 * @author Mikhail Golubev
 */
class JsonSmartEnterProcessor : SmartEnterProcessorWithFixers() {
    private var myShouldAddNewline = false

    init {
        addFixers(JsonObjectPropertyFixer(), JsonArrayElementFixer())
        addEnterProcessors(JsonEnterProcessor())
    }

    override fun collectAdditionalElements(element: PsiElement, result: MutableList<PsiElement>) {
        // include all parents as well
        var parent = element.parent
        while (parent != null && parent !is JsonFile) {
            result.add(parent)
            parent = parent.parent
        }
    }

    private class JsonArrayElementFixer : Fixer<JsonSmartEnterProcessor>() {
        @Throws(IncorrectOperationException::class)
        override fun apply(editor: Editor, processor: JsonSmartEnterProcessor, element: PsiElement) {
            if (element is JsonValue && element.getParent() is JsonArray) {
                if (terminatedOnCurrentLine(editor, element) && !isFollowedByTerminal(
                        element,
                        JsonElementTypes.COMMA
                    )
                ) {
                    editor.document.insertString(element.getTextRange().endOffset, ",")
                    processor.myShouldAddNewline = true
                }
            }
        }
    }

    private class JsonObjectPropertyFixer : Fixer<JsonSmartEnterProcessor>() {
        @Throws(IncorrectOperationException::class)
        override fun apply(editor: Editor, processor: JsonSmartEnterProcessor, element: PsiElement) {
            if (element is JsonProperty) {
                val propertyValue = element.value
                if (propertyValue != null) {
                    if (terminatedOnCurrentLine(editor, propertyValue) && !isFollowedByTerminal(
                            propertyValue,
                            JsonElementTypes.COMMA
                        )
                    ) {
                        editor.document.insertString(propertyValue.textRange.endOffset, ",")
                        processor.myShouldAddNewline = true
                    }
                } else {
                    val propertyKey = element.nameElement
                    val keyRange = propertyKey.textRange
                    val keyStartOffset = keyRange.startOffset
                    var keyEndOffset = keyRange.endOffset
                    //processor.myFirstErrorOffset = keyEndOffset;
                    if (terminatedOnCurrentLine(editor, propertyKey) && !isFollowedByTerminal(
                            propertyKey,
                            JsonElementTypes.COLON
                        )
                    ) {
                        val shouldQuoteKey =
                            propertyKey is JsonReferenceExpression && JsonDialectUtil.isStandardJson(propertyKey)
                        if (shouldQuoteKey) {
                            editor.document.insertString(keyStartOffset, "\"")
                            keyEndOffset++
                            editor.document.insertString(keyEndOffset, "\"")
                            keyEndOffset++
                        }
                        processor.myFirstErrorOffset = keyEndOffset + 2
                        editor.document.insertString(keyEndOffset, ": ")
                    }
                }
            }
        }
    }

    private inner class JsonEnterProcessor : FixEnterProcessor() {
        override fun doEnter(atCaret: PsiElement, file: PsiFile, editor: Editor, modified: Boolean): Boolean {
            if (myShouldAddNewline) {
                try {
                    plainEnter(editor)
                } finally {
                    myShouldAddNewline = false
                }
            }
            return true
        }
    }

    companion object {
        val LOG: Logger = Logger.getInstance(JsonSmartEnterProcessor::class.java)

        private fun terminatedOnCurrentLine(editor: Editor, element: PsiElement): Boolean {
            val document = editor.document
            val caretOffset = editor.caretModel.currentCaret.offset
            val elementEndOffset = element.textRange.endOffset
            if (document.getLineNumber(elementEndOffset) != document.getLineNumber(caretOffset)) {
                return false
            }
            // Skip empty PsiError elements if comma is missing
            val nextLeaf = PsiTreeUtil.nextLeaf(element, true)
            return nextLeaf == null || (nextLeaf is PsiWhiteSpace && nextLeaf.getText().contains("\n"))
        }

        private fun isFollowedByTerminal(element: PsiElement, type: IElementType): Boolean {
            val nextLeaf = PsiTreeUtil.nextVisibleLeaf(element)
            return nextLeaf != null && nextLeaf.node.elementType === type
        }
    }
}
