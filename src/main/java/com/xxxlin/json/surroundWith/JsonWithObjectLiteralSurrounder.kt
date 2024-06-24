// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.surroundWith

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import com.xxxlin.json.JsonBundle.message
import com.xxxlin.json.psi.*

/**
 * This surrounder ported from JavaScript allows to wrap single JSON value or several consecutive JSON properties
 * in object literal.
 *
 *
 * Examples:
 *
 *  1. `[42]` converts to `[{"property": 42}]`
 *  1. <pre>
 * {
 * "foo": 42,
 * "bar": false
 * }
</pre> *  converts to <pre>
 * {
 * "property": {
 * "foo": 42,
 * "bar": false
 * }
 * }
</pre> *
 *
 *
 * @author Mikhail Golubev
 */
class JsonWithObjectLiteralSurrounder : JsonSurrounderBase() {
    override fun getTemplateDescription(): String {
        return message("surround.with.object.literal.desc")
    }

    override fun isApplicable(elements: Array<PsiElement>): Boolean {
        return !JsonPsiUtil.isPropertyKey(elements[0]) && (elements[0] is JsonProperty || elements.size == 1)
    }

    @Throws(IncorrectOperationException::class)
    override fun surroundElements(
        project: Project,
        editor: Editor,
        elements: Array<PsiElement>
    ): TextRange? {
        if (!isApplicable(elements)) {
            return null
        }

        val generator = JsonElementGenerator(project)

        val firstElement = elements[0]
        val newNameElement: JsonElement
        if (firstElement is JsonValue) {
            assert(elements.size == 1) { "Only single JSON value can be wrapped in object literal" }
            var replacement = generator.createValue<JsonObject>(createReplacementText(firstElement.getText()))
            replacement = firstElement.replace(replacement) as JsonObject
            newNameElement = replacement.propertyList[0].nameElement
        } else {
            assert(firstElement is JsonProperty)
            val propertiesText = getTextAndRemoveMisc(firstElement, elements[elements.size - 1])
            val tempJsonObject = generator.createValue<JsonObject>(
                """
    ${createReplacementText("{\n$propertiesText")}
    }
    """.trimIndent()
            )
            var replacement = tempJsonObject.propertyList[0]
            replacement = firstElement.replace(replacement) as JsonProperty
            newNameElement = replacement.nameElement
        }
        val rangeWithQuotes = newNameElement.getTextRange()
        return TextRange(rangeWithQuotes.startOffset + 1, rangeWithQuotes.endOffset - 1)
    }

    override fun createReplacementText(textInRange: String): String {
        return "{\n\"property\": $textInRange\n}"
    }
}
