// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.surroundWith

import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.SmartList
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.psi.JsonProperty
import com.xxxlin.json.psi.JsonValue

/**
 * @author Mikhail Golubev
 */
class JsonSurroundDescriptor : SurroundDescriptor {
    override fun getElementsToSurround(file: PsiFile, startOffset: Int, endOffset: Int): Array<PsiElement> {
        var startOffset = startOffset
        var endOffset = endOffset
        var firstElement = file.findElementAt(startOffset)
        var lastElement = file.findElementAt(endOffset - 1)

        // Extend selection beyond possible delimiters
        while (firstElement != null &&
            (firstElement is PsiWhiteSpace || firstElement.node.elementType === JsonElementTypes.COMMA)
        ) {
            firstElement = firstElement.nextSibling
        }
        while (lastElement != null &&
            (lastElement is PsiWhiteSpace || lastElement.node.elementType === JsonElementTypes.COMMA)
        ) {
            lastElement = lastElement.prevSibling
        }
        if (firstElement != null) {
            startOffset = firstElement.textRange.startOffset
        }
        if (lastElement != null) {
            endOffset = lastElement.textRange.endOffset
        }

        val property = PsiTreeUtil.findElementOfClassAtRange(file, startOffset, endOffset, JsonProperty::class.java)
        if (property != null) {
            return collectElements(endOffset, property, JsonProperty::class.java)
        }

        val value = PsiTreeUtil.findElementOfClassAtRange(file, startOffset, endOffset, JsonValue::class.java)
        if (value != null) {
            return collectElements(endOffset, value, JsonValue::class.java)
        }
        return PsiElement.EMPTY_ARRAY
    }

    override fun getSurrounders(): Array<Surrounder> {
        return ourSurrounders
    }

    override fun isExclusive(): Boolean {
        return false
    }

    companion object {
        private val ourSurrounders = arrayOf<Surrounder>(
            JsonWithObjectLiteralSurrounder(),
            JsonWithArrayLiteralSurrounder(),
            JsonWithQuotesSurrounder()
        )

        private fun <T : PsiElement> collectElements(endOffset: Int, property: T, kind: Class<T>): Array<PsiElement> {
            val properties: MutableList<T> = SmartList(property)
            var nextSibling = property.nextSibling
            while (nextSibling != null && nextSibling.textRange.endOffset <= endOffset) {
                if (kind.isInstance(nextSibling)) {
                    properties.add(kind.cast(nextSibling))
                }
                nextSibling = nextSibling.nextSibling
            }
            return properties.toTypedArray()
        }
    }
}
