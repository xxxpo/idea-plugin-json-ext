// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.codeinsight

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.xxxlin.json.psi.JsonArray
import com.xxxlin.json.psi.JsonProperty
import com.xxxlin.json.psi.JsonStringLiteral

/**
 * @author Mikhail Golubev
 */
class JsonCompletionContributor : CompletionContributor() {

    companion object {
        private val AFTER_COLON_IN_PROPERTY = PlatformPatterns.psiElement()
            .afterLeaf(":").withSuperParent(2, JsonProperty::class.java)
            .andNot(PlatformPatterns.psiElement().withParent(JsonStringLiteral::class.java))

        private val AFTER_COMMA_OR_BRACKET_IN_ARRAY = PlatformPatterns.psiElement()
            .afterLeaf("[", ",").withSuperParent(2, JsonArray::class.java)
            .andNot(PlatformPatterns.psiElement().withParent(JsonStringLiteral::class.java))
    }

    init {
        extend(CompletionType.BASIC, AFTER_COLON_IN_PROPERTY, MyKeywordsCompletionProvider.INSTANCE)
        extend(CompletionType.BASIC, AFTER_COMMA_OR_BRACKET_IN_ARRAY, MyKeywordsCompletionProvider.INSTANCE)
    }

    private class MyKeywordsCompletionProvider : CompletionProvider<CompletionParameters>() {
        companion object {
            val INSTANCE: MyKeywordsCompletionProvider = MyKeywordsCompletionProvider()
            private val KEYWORDS = arrayOf("null", "true", "false")
        }

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            for (keyword in KEYWORDS) {
                result.addElement(LookupElementBuilder.create(keyword).bold())
            }
        }
    }
}
