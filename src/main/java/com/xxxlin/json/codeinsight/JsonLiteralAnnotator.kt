// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.codeinsight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.xxxlin.json.JsonBundle
import com.xxxlin.json.highlighting.JsonSyntaxHighlighterFactory
import com.xxxlin.json.psi.JsonNumberLiteral
import com.xxxlin.json.psi.JsonPsiUtil
import com.xxxlin.json.psi.JsonReferenceExpression
import com.xxxlin.json.psi.JsonStringLiteral

/**
 * @author Mikhail Golubev
 */
class JsonLiteralAnnotator : Annotator {
    private object Holder {
        val DEBUG: Boolean = ApplicationManager.getApplication().isUnitTestMode
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val extensions = JsonExtLiteralChecker.EP_NAME.extensionList
        if (element is JsonReferenceExpression) {
            highlightPropertyKey(element, holder)
        } else if (element is JsonStringLiteral) {
            val elementOffset = element.getTextOffset()
            highlightPropertyKey(element, holder)
            val text = JsonPsiUtil.getElementTextWithoutHostEscaping(element)
            val length = text.length

            // Check that string literal is closed properly
            if (length <= 1 || text[0] != text[length - 1] || JsonPsiUtil.isEscapedChar(text, length - 1)) {
                holder.newAnnotation(HighlightSeverity.ERROR, JsonBundle.message("syntax.error.missing.closing.quote"))
                    .create()
            }

            // Check escapes
            val fragments: List<Pair<TextRange, String>> = element.textFragments
            for (fragment in fragments) {
                for (checker in extensions) {
                    if (!checker.isApplicable(element)) continue
                    val error = checker.getErrorForStringFragment(fragment, element)
                    if (error != null) {
                        holder.newAnnotation(HighlightSeverity.ERROR, error.second)
                            .range(error.getFirst().shiftRight(elementOffset)).create()
                    }
                }
            }
        } else if (element is JsonNumberLiteral) {
            var text: String? = null
            for (checker in extensions) {
                if (!checker.isApplicable(element)) continue
                if (text == null) {
                    text = JsonPsiUtil.getElementTextWithoutHostEscaping(element)
                }
                val error = checker.getErrorForNumericLiteral(text)
                if (error != null) {
                    holder.newAnnotation(HighlightSeverity.ERROR, error).create()
                }
            }
        }
    }

    companion object {
        private fun highlightPropertyKey(element: PsiElement, holder: AnnotationHolder) {
            if (JsonPsiUtil.isPropertyKey(element)) {
                if (Holder.DEBUG) {
                    holder.newAnnotation(HighlightSeverity.INFORMATION, JsonBundle.message("annotation.property.key"))
                        .textAttributes(
                            JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY
                        ).create()
                } else {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(
                        JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY
                    ).create()
                }
            }
        }
    }
}
