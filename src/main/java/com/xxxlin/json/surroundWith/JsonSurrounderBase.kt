// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.surroundWith

import com.intellij.lang.surroundWith.Surrounder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import com.xxxlin.json.psi.JsonElementGenerator
import com.xxxlin.json.psi.JsonPsiUtil
import com.xxxlin.json.psi.JsonValue

abstract class JsonSurrounderBase : Surrounder {
    override fun isApplicable(elements: Array<PsiElement>): Boolean {
        return elements.isNotEmpty() && elements[0] is JsonValue && !JsonPsiUtil.isPropertyKey(
            elements[0]
        )
    }

    @Throws(IncorrectOperationException::class)
    override fun surroundElements(project: Project, editor: Editor, elements: Array<PsiElement>): TextRange? {
        if (!isApplicable(elements)) {
            return null
        }

        val generator = JsonElementGenerator(project)

        if (elements.size == 1) {
            val replacement = generator.createValue<JsonValue>(createReplacementText(elements[0].text))
            elements[0].replace(replacement)
        } else {
            val propertiesText = getTextAndRemoveMisc(
                elements[0], elements[elements.size - 1]
            )
            val replacement = generator.createValue<JsonValue>(createReplacementText(propertiesText))
            elements[0].replace(replacement)
        }
        return null
    }

    protected abstract fun createReplacementText(textInRange: String): String

    companion object {
        @JvmStatic
        protected fun getTextAndRemoveMisc(firstProperty: PsiElement, lastProperty: PsiElement): String {
            val replacedRange = TextRange(firstProperty.textOffset, lastProperty.textRange.endOffset)
            val propertiesText = replacedRange.substring(firstProperty.containingFile.text)
            if (firstProperty !== lastProperty) {
                val parent = firstProperty.parent
                parent.deleteChildRange(firstProperty.nextSibling, lastProperty)
            }
            return propertiesText
        }
    }
}
