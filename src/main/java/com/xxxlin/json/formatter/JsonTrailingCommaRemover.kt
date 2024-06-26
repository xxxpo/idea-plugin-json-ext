// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor
import com.intellij.util.DocumentUtil
import com.intellij.util.ObjectUtils
import com.intellij.util.containers.ContainerUtil
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.JsonLanguage
import com.xxxlin.json.psi.JsonArray
import com.xxxlin.json.psi.JsonObject
import com.xxxlin.json.psi.impl.JsonRecursiveElementVisitor

class JsonTrailingCommaRemover : PreFormatProcessor {
    override fun process(element: ASTNode, range: TextRange): TextRange {
        val rootPsi = element.psi
        if (rootPsi.language !== JsonLanguage.INSTANCE) {
            return range
        }
        val settings = CodeStyle.getCustomSettings(rootPsi.containingFile, JsonCodeStyleSettings::class.java)
        if (settings.KEEP_TRAILING_COMMA) {
            return range
        }
        val psiDocumentManager = PsiDocumentManager.getInstance(rootPsi.project)
        val document = psiDocumentManager.getDocument(rootPsi.containingFile) ?: return range
        DocumentUtil.executeInBulk(document, true) {
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document)
            val visitor: PsiElementVisitor = Visitor(document)
            rootPsi.accept(visitor)
            psiDocumentManager.commitDocument(document)
        }
        return range
    }

    private class Visitor(private val myDocument: Document) : JsonRecursiveElementVisitor() {
        private var myOffsetDelta = 0

        override fun visitArray(o: JsonArray) {
            super.visitArray(o)
            val lastChild = o.lastChild
            if (lastChild == null || lastChild.node.elementType !== JsonElementTypes.R_BRACKET) {
                return
            }
            deleteTrailingCommas(ObjectUtils.coalesce(ContainerUtil.getLastItem(o.valueList), o.firstChild))
        }

        override fun visitObject(o: JsonObject) {
            super.visitObject(o)
            val lastChild = o.lastChild
            if (lastChild == null || lastChild.node.elementType !== JsonElementTypes.R_CURLY) {
                return
            }
            deleteTrailingCommas(ObjectUtils.coalesce(ContainerUtil.getLastItem(o.propertyList), o.firstChild))
        }

        private fun deleteTrailingCommas(lastElementOrOpeningBrace: PsiElement?) {
            var element = lastElementOrOpeningBrace?.nextSibling

            while (element != null) {
                if (element.node.elementType === JsonElementTypes.COMMA ||
                    element is PsiErrorElement && "," == element.getText()
                ) {
                    deleteNode(element.node)
                } else if (!(element is PsiComment || element is PsiWhiteSpace)) {
                    break
                }
                element = element.nextSibling
            }
        }

        private fun deleteNode(node: ASTNode) {
            val length = node.textLength
            myDocument.deleteString(node.startOffset + myOffsetDelta, node.startOffset + length + myOffsetDelta)
            myOffsetDelta -= length
        }
    }
}
