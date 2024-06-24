package com.xxxlin.json.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.xxxlin.json.JsonLanguageUtil
import com.xxxlin.json.highlighting.JsonSyntaxHighlighterFactory

/**
 * JAVA字符串注解
 */
class JavaAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiLiteralExpression) {
            annotateString(element, holder)
        }
    }

    /**
     * 引用规则标色
     */
    private fun annotateString(element: PsiLiteralExpression, holder: AnnotationHolder) {
        val value = element.value as String? ?: return
        if (value.isEmpty()) {
            return
        }
        val jsonProperty = JsonLanguageUtil.hasJsonKey(element.project, value)
        if (jsonProperty != null) {
            val keyRange = TextRange(element.textRange.startOffset + 1, element.textRange.endOffset - 1)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(keyRange)
                .textAttributes(JsonSyntaxHighlighterFactory.JSON_NUMBER)
                .create()
        }
    }
}