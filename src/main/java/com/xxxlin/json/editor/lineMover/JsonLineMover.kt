// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.editor.lineMover

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.xxxlin.json.psi.*
import kotlin.math.max
import kotlin.math.min

class JsonLineMover : LineMover() {
    private object Direction {
        var Same = 1
        var Inside = 2
        var Outside = 3
    }

    private var myDirection = Direction.Same

    override fun checkAvailable(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean): Boolean {
        myDirection = Direction.Same

        if (file !is JsonFile || !super.checkAvailable(editor, file, info, down)) {
            return false
        }

        var movedElementRange = getElementRange(editor, file, info.toMove)
        if (!isValidElementRange(movedElementRange)) {
            return false
        }

        // Tweak range to move if it's necessary
        movedElementRange = expandCommentsInRange(
            movedElementRange!!
        )

        val movedSecond = movedElementRange.getSecond()
        val movedFirst = movedElementRange.getFirst()

        info.toMove = LineRange(movedFirst, movedSecond)

        // Adjust destination range to prevent illegal offsets
        val lineCount = editor.document.lineCount
        if (down) {
            info.toMove2 =
                LineRange(info.toMove.endLine, min((info.toMove.endLine + 1), lineCount))
        } else {
            info.toMove2 = LineRange(max((info.toMove.startLine - 1), 0), info.toMove.startLine)
        }

        if (movedFirst is PsiComment && movedSecond is PsiComment) {
            return true
        }

        // Check whether additional comma is needed
        val destElementRange = getElementRange(editor, file, info.toMove2)

        if (destElementRange != null) {
            val destFirst = destElementRange.getFirst()
            val destSecond = destElementRange.getSecond()

            if (destFirst === destSecond && destFirst !is JsonProperty && destFirst !is JsonValue) {
                val parent = destFirst.parent
                if ((parent.containingFile as JsonFile).topLevelValue === parent) {
                    info.prohibitMove()
                    return true
                }
            }

            val firstParent = destFirst.parent
            val secondParent = destSecond.parent

            val firstParentParent: JsonValue? =
                PsiTreeUtil.getParentOfType(firstParent, JsonObject::class.java, JsonArray::class.java)
            if (firstParentParent === secondParent) {
                myDirection = if (down) Direction.Outside else Direction.Inside
            }
            val secondParentParent: JsonValue? =
                PsiTreeUtil.getParentOfType(secondParent, JsonObject::class.java, JsonArray::class.java)
            if (firstParent === secondParentParent) {
                myDirection = if (down) Direction.Inside else Direction.Outside
            }
        }
        return true
    }

    override fun afterMove(editor: Editor, file: PsiFile, info: MoveInfo, down: Boolean) {
        val diff = (info.toMove.endLine - info.toMove.startLine) - (info.toMove2.endLine - info.toMove2.startLine)
        when (myDirection) {
            Direction.Same -> {
                addCommaIfNeeded(
                    editor.document,
                    if (down) info.toMove.endLine - 1 - diff else info.toMove2.endLine - 1 + diff
                )
                trimCommaIfNeeded(editor.document, file, if (down) info.toMove.endLine else info.toMove2.endLine + diff)
            }

            Direction.Inside -> {
                if (!down) {
                    addCommaIfNeeded(editor.document, info.toMove2.startLine - 1)
                }
                trimCommaIfNeeded(editor.document, file, if (down) info.toMove.startLine else info.toMove2.startLine)
                trimCommaIfNeeded(editor.document, file, if (down) info.toMove.endLine else info.toMove2.endLine + diff)
            }

            Direction.Outside -> {
                addCommaIfNeeded(editor.document, if (down) info.toMove.startLine else info.toMove2.startLine)
                trimCommaIfNeeded(editor.document, file, if (down) info.toMove.endLine else info.toMove2.endLine + diff)
                if (down) {
                    trimCommaIfNeeded(editor.document, file, info.toMove.startLine - 1)
                    addCommaIfNeeded(editor.document, info.toMove.endLine)
                    trimCommaIfNeeded(editor.document, file, info.toMove.endLine)
                }
            }
        }
    }

    companion object {
        private fun expandCommentsInRange(range: Pair<PsiElement, PsiElement>): Pair<PsiElement, PsiElement> {
            val upper = JsonPsiUtil.findFurthestSiblingOfSameType(range.getFirst(), false)
            val lower = JsonPsiUtil.findFurthestSiblingOfSameType(range.getSecond(), true)
            return Pair.create(upper, lower)
        }

        private fun getForwardLineNumber(document: Document, element: PsiElement?): Int {
            var element = element
            while (element is PsiWhiteSpace || element is PsiComment) {
                element = element.nextSibling
            }
            if (element == null) return -1

            val range = element.textRange
            return document.getLineNumber(range.endOffset)
        }

        private fun getBackwardLineNumber(document: Document, element: PsiElement?): Int {
            var element = element
            while (element is PsiWhiteSpace || element is PsiComment) {
                element = element.prevSibling
            }
            if (element == null) return -1

            val range = element.textRange
            return document.getLineNumber(range.endOffset)
        }

        private fun trimCommaIfNeeded(document: Document, file: PsiFile, line: Int) {
            val offset = document.getLineEndOffset(line)
            if (doTrimComma(document, offset + 1, offset)) return

            val element = file.findElementAt(offset - 1)
            val forward = getForwardLineNumber(document, element)
            val backward = getBackwardLineNumber(document, element)
            if (forward < 0 || backward < 0) return
            doTrimComma(document, document.getLineEndOffset(forward) - 1, document.getLineEndOffset(backward))
        }

        private fun doTrimComma(document: Document, forwardOffset: Int, backwardOffset: Int): Boolean {
            val charSequence = document.charsSequence
            if (backwardOffset <= 0) return true
            if (charSequence[backwardOffset - 1] == ',') {
                val offsetAfter = skipWhitespaces(charSequence, forwardOffset)
                if (offsetAfter >= charSequence.length) return true
                val ch = charSequence[offsetAfter]

                if (ch == ']' || ch == '}') {
                    document.deleteString(backwardOffset - 1, backwardOffset)
                }
                if (ch != '/') return true
            }
            return false
        }

        private fun skipWhitespaces(charSequence: CharSequence, offset2: Int): Int {
            var offset2 = offset2
            while (offset2 < charSequence.length && Character.isWhitespace(charSequence[offset2])) {
                offset2++
            }
            return offset2
        }

        private fun addCommaIfNeeded(document: Document, line: Int) {
            val offset = document.getLineEndOffset(line)
            if (offset > 0 && document.charsSequence[offset - 1] != ',') {
                document.insertString(offset, ",")
            }
        }

        private fun isValidElementRange(elementRange: Pair<PsiElement, PsiElement>?): Boolean {
            if (elementRange == null) {
                return false
            }
            return elementRange.getFirst().parent === elementRange.getSecond().parent
        }
    }
}
