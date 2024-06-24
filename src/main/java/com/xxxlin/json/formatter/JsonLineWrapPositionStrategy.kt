// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.formatter

import com.intellij.openapi.editor.DefaultLineWrapPositionStrategy
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.xxxlin.json.JsonElementTypes
import kotlin.math.max

class JsonLineWrapPositionStrategy : DefaultLineWrapPositionStrategy() {
    override fun calculateWrapPosition(
        document: Document,
        project: Project?,
        startOffset: Int,
        endOffset: Int,
        maxPreferredOffset: Int,
        allowToBeyondMaxPreferredOffset: Boolean,
        isSoftWrap: Boolean
    ): Int {
        if (isSoftWrap) {
            return super.calculateWrapPosition(
                document, project, startOffset, endOffset, maxPreferredOffset, allowToBeyondMaxPreferredOffset,
                true
            )
        }
        if (project == null) return -1
        val wrapPosition = getMinWrapPosition(document, project, maxPreferredOffset)
        if (wrapPosition == SKIP_WRAPPING) return -1
        val minWrapPosition = max(startOffset.toDouble(), wrapPosition.toDouble()).toInt()
        return super
            .calculateWrapPosition(
                document,
                project,
                minWrapPosition,
                endOffset,
                maxPreferredOffset,
                allowToBeyondMaxPreferredOffset,
                isSoftWrap
            )
    }

    companion object {
        private const val SKIP_WRAPPING = -2

        private fun getMinWrapPosition(document: Document, project: Project, offset: Int): Int {
            val manager = PsiDocumentManager.getInstance(project)
            if (manager.isUncommited(document)) manager.commitDocument(document)
            val psiFile = manager.getPsiFile(document)
            if (psiFile != null) {
                val currElement = psiFile.findElementAt(offset)
                val elementType = PsiUtilCore.getElementType(currElement)
                if (elementType === JsonElementTypes.DOUBLE_QUOTED_STRING || elementType === JsonElementTypes.SINGLE_QUOTED_STRING || elementType === JsonElementTypes.LITERAL || elementType === JsonElementTypes.BOOLEAN_LITERAL || elementType === JsonElementTypes.TRUE || elementType === JsonElementTypes.FALSE || elementType === JsonElementTypes.IDENTIFIER || elementType === JsonElementTypes.NULL_LITERAL || elementType === JsonElementTypes.NUMBER_LITERAL) {
                    return currElement!!.textRange.endOffset
                }
                if (elementType === JsonElementTypes.COLON) {
                    return SKIP_WRAPPING
                }
                if (currElement != null) {
                    if (currElement is PsiComment ||
                        PsiUtilCore.getElementType(PsiTreeUtil.skipWhitespacesForward(currElement)) === JsonElementTypes.COMMA
                    ) {
                        return SKIP_WRAPPING
                    }
                }
            }
            return -1
        }
    }
}
