// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.highlighting

import com.intellij.codeInsight.daemon.RainbowVisitor
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.xxxlin.json.psi.*

class JsonRainbowVisitor : RainbowVisitor() {
    private object Holder {
        val blacklist: Map<String, Set<String>> = createBlacklist()

        private fun createBlacklist(): Map<String, Set<String>> {
            val blacklist: MutableMap<String, Set<String>> = HashMap()
            blacklist["package.json"] = setOf(
                "/dependencies",
                "/devDependencies",
                "/peerDependencies",
                "/scripts",
                "/directories",
                "/optionalDependencies"
            )
            return blacklist
        }
    }

    override fun suitableForFile(file: PsiFile): Boolean {
        return file is JsonFile
    }

    override fun visit(element: PsiElement) {
        if (element is JsonProperty) {
            val file = element.getContainingFile()
            val fileName = file.name
            if (Holder.blacklist.containsKey(fileName)) {
                // todo
//                JsonPointerPosition position = JsonOriginalPsiWalker.INSTANCE.findPosition(element, false);
//                if (position != null && Holder.blacklist.get(fileName).contains(position.toJsonPointer())) return;
            }
            val name = element.name
            addInfo(getInfo(file, element.nameElement, name, JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY))
            val value = element.value
            if (value is JsonObject) {
                addInfo(getInfo(file, value.getFirstChild(), name, JsonSyntaxHighlighterFactory.JSON_BRACES))
                addInfo(getInfo(file, value.getLastChild(), name, JsonSyntaxHighlighterFactory.JSON_BRACES))
            } else if (value is JsonArray) {
                addInfo(getInfo(file, value.getFirstChild(), name, JsonSyntaxHighlighterFactory.JSON_BRACKETS))
                addInfo(getInfo(file, value.getLastChild(), name, JsonSyntaxHighlighterFactory.JSON_BRACKETS))
                for (jsonValue in value.valueList) {
                    addSimpleValueInfo(name, file, jsonValue)
                }
            } else {
                addSimpleValueInfo(name, file, value)
            }
        }
    }

    private fun addSimpleValueInfo(name: String, file: PsiFile, value: JsonValue?) {
        if (value is JsonStringLiteral) {
            addInfo(getInfo(file, value, name, JsonSyntaxHighlighterFactory.JSON_STRING))
        } else if (value is JsonNumberLiteral) {
            addInfo(getInfo(file, value, name, JsonSyntaxHighlighterFactory.JSON_NUMBER))
        } else if (value is JsonLiteral) {
            addInfo(getInfo(file, value, name, JsonSyntaxHighlighterFactory.JSON_KEYWORD))
        }
    }

    override fun clone(): HighlightVisitor {
        return JsonRainbowVisitor()
    }
}
