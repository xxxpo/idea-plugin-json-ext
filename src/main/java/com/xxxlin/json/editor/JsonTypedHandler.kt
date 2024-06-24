// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.editor

import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.xxxlin.json.JsonDialectUtil
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.editor.JsonEditorOptions.Companion.instance
import com.xxxlin.json.psi.*

class JsonTypedHandler : TypedHandlerDelegate() {
    private var myWhitespaceAdded = false

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (file is JsonFile) {
            processPairedBracesComma(c, editor, file)
            addWhiteSpaceAfterColonIfNeeded(c, editor, file)
            removeRedundantWhitespaceIfAfterColon(c, editor, file)
            handleMoveOutsideQuotes(c, editor, file)
        }
        return Result.CONTINUE
    }

    private fun removeRedundantWhitespaceIfAfterColon(c: Char, editor: Editor, file: PsiFile) {
        if (!myWhitespaceAdded || c != ' ' || !instance.AUTO_WHITESPACE_AFTER_COLON) {
            if (c != ':') {
                myWhitespaceAdded = false
            }
            return
        }
        val offset = editor.caretModel.offset
        PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)
        val element = file.findElementAt(offset)
        if (element is PsiWhiteSpace) {
            editor.document.deleteString(offset - 1, offset)
        }
        myWhitespaceAdded = false
    }

    override fun beforeCharTyped(
        c: Char,
        project: Project,
        editor: Editor,
        file: PsiFile,
        fileType: FileType
    ): Result {
        if (file is JsonFile) {
            addPropertyNameQuotesIfNeeded(c, editor, file)
        }
        return Result.CONTINUE
    }

    private fun addWhiteSpaceAfterColonIfNeeded(
        c: Char,
        editor: Editor,
        file: PsiFile
    ) {
        if (c != ':' || !instance.AUTO_WHITESPACE_AFTER_COLON) {
            if (c != ' ') {
                myWhitespaceAdded = false
            }
            return
        }
        val offset = editor.caretModel.offset
        PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)
        val element: PsiElement? = PsiTreeUtil.getParentOfType(
            PsiTreeUtil.skipWhitespacesBackward(file.findElementAt(offset)),
            JsonProperty::class.java,
            false
        )
        if (element == null) {
            myWhitespaceAdded = false
            return
        }
        val children = element.node.getChildren(TokenSet.create(JsonElementTypes.COLON))
        if (children.isEmpty()) {
            myWhitespaceAdded = false
            return
        }
        val colon = children[0]
        val next = colon.treeNext
        val text = next.text
        if (text.isEmpty() || !StringUtil.isEmptyOrSpaces(text) || StringUtil.isLineBreak(
                text[0]
            )
        ) {
            val insOffset = colon.startOffset + 1
            editor.document.insertString(insOffset, " ")
            editor.caretModel.moveToOffset(insOffset + 1)
            myWhitespaceAdded = true
        } else {
            myWhitespaceAdded = false
        }
    }

    companion object {
        private fun handleMoveOutsideQuotes(c: Char, editor: Editor, file: PsiFile) {
            val options = instance
            if (c == ':' && options.COLON_MOVE_OUTSIDE_QUOTES || c == ',' && options.COMMA_MOVE_OUTSIDE_QUOTES) {
                val offset = editor.caretModel.offset
                val sequence = editor.document.charsSequence
                val length = sequence.length
                if (offset >= length || offset < 0) return
                val charAtOffset = sequence[offset]
                if (charAtOffset != '"') return
                if (offset + 1 < length && sequence[offset + 1] == c) return
                val element = file.findElementAt(offset) ?: return
                if (!validatePositionToMoveOutOfQuotes(c, element)) return
                PsiDocumentManager.getInstance(file.project).commitDocument(editor.document)
                editor.document.deleteString(offset - 1, offset)
                editor.document.insertString(offset, c.toString())
                val newSequence = editor.document.charsSequence
                var nextOffset = offset + 1
                if (c == ':' && options.AUTO_WHITESPACE_AFTER_COLON) {
                    val nextChar = if (nextOffset >= newSequence.length) 'a' else newSequence[nextOffset]
                    if (!Character.isWhitespace(nextChar) || nextChar == '\n') {
                        editor.document.insertString(nextOffset, " ")
                        nextOffset++
                    }
                }
                editor.caretModel.moveToOffset(nextOffset)
            }
        }

        private fun validatePositionToMoveOutOfQuotes(c: Char, element: PsiElement): Boolean {
            // comma can be after the element, but only the comma
            if (PsiUtilCore.getElementType(element) === JsonElementTypes.R_CURLY) {
                return c == ',' && element.prevSibling is JsonProperty
            }
            if (PsiUtilCore.getElementType(element) === JsonElementTypes.R_BRACKET) {
                return c == ',' && element.prevSibling is JsonStringLiteral
            }

            // we can have a whitespace in the position, but again - only for the comma
            val parent = element.parent
            if (element is PsiWhiteSpace && c == ',') {
                val sibling = element.getPrevSibling()
                return sibling is JsonProperty || sibling is JsonStringLiteral
            }

            // the most ordinary case - literal property key or value
            val grandParent = if (parent is JsonStringLiteral) parent.getParent() else null
            return (grandParent is JsonProperty
                    && (c != ':' || grandParent.nameElement === parent)
                    && (c != ',' || grandParent.value === parent))
        }

        private fun addPropertyNameQuotesIfNeeded(
            c: Char,
            editor: Editor,
            file: PsiFile
        ) {
            if (c != ':' || !JsonDialectUtil.isStandardJson(file) || !instance.AUTO_QUOTE_PROP_NAME) return
            val offset = editor.caretModel.offset
            val element = PsiTreeUtil.skipWhitespacesBackward(file.findElementAt(offset)) as? JsonProperty ?: return
            val nameElement = element.nameElement
            if (nameElement is JsonReferenceExpression) {
                element.setName(nameElement.getText())
                PsiDocumentManager.getInstance(file.project).doPostponedOperationsAndUnblockDocument(editor.document)
            }
        }

        @JvmStatic
        fun processPairedBracesComma(
            c: Char,
            editor: Editor,
            file: PsiFile
        ) {
            if (!instance.COMMA_ON_MATCHING_BRACES) return
            if (c != '[' && c != '{' && c != '"' && c != '\'') return
            SmartEnterProcessor.commitDocument(editor)
            val offset = editor.caretModel.offset
            val element = file.findElementAt(offset) ?: return
            val parent = element.parent
            val codeInsightSettings = CodeInsightSettings.getInstance()
            if ((c == '[' && parent is JsonArray
                        || c == '{' && parent is JsonObject) && codeInsightSettings.AUTOINSERT_PAIR_BRACKET
                || (c == '"' || c == '\'') && parent is JsonStringLiteral && codeInsightSettings.AUTOINSERT_PAIR_QUOTE
            ) {
                if (shouldAddCommaInParentContainer(parent as JsonValue)) {
                    editor.document.insertString(parent.getTextRange().endOffset, ",")
                }
            }
        }

        private fun shouldAddCommaInParentContainer(item: JsonValue): Boolean {
            val parent = item.parent
            if (parent is JsonArray || parent is JsonProperty) {
                val nextElement = PsiTreeUtil.skipWhitespacesForward(if (parent is JsonProperty) parent else item)
                if (nextElement is PsiErrorElement) {
                    val forward = PsiTreeUtil.skipWhitespacesForward(nextElement)
                    return if (parent is JsonProperty) forward is JsonProperty else forward is JsonValue
                }
            }
            return false
        }
    }
}
