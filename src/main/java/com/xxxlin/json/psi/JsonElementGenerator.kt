// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.xxxlin.json.JsonFileType

/**
 * @author Mikhail Golubev
 */
class JsonElementGenerator(private val myProject: Project) {
    /**
     * Create lightweight in-memory [JsonFile] filled with `content`.
     *
     * @param content content of the file to be created
     * @return created file
     */
    fun createDummyFile(content: String): PsiFile {
        val psiFileFactory = PsiFileFactory.getInstance(myProject)
        return psiFileFactory.createFileFromText(
            "dummy." + JsonFileType.INSTANCE.defaultExtension,
            JsonFileType.INSTANCE,
            content
        )
    }

    /**
     * Create JSON value from supplied content.
     *
     * @param content properly escaped text of JSON value, e.g. Java literal `"\"new\\nline\""` if you want to create string literal
     * @param <T>     type of the JSON value desired
     * @return element created from given text
     *
     * @see .createStringLiteral
    </T> */
    fun <T : JsonValue?> createValue(content: String): T {
        val file = createDummyFile("{\"foo\": $content}")
        return (file.firstChild as JsonObject).propertyList[0].value as T
    }

    fun createObject(content: String): JsonObject {
        val file = createDummyFile("{$content}")
        return file.firstChild as JsonObject
    }

    fun createEmptyArray(): JsonArray {
        val file = createDummyFile("[]")
        return file.firstChild as JsonArray
    }

    fun createArrayItemValue(content: String): JsonValue {
        val file = createDummyFile("[$content]")
        val array = file.firstChild as JsonArray
        return array.valueList[0]
    }

    /**
     * Create JSON string literal from supplied *unescaped* content.
     *
     * @param unescapedContent unescaped content of string literal, e.g. Java literal `"new\nline"` (compare with [.createValue]).
     * @return JSON string literal created from given text
     */
    fun createStringLiteral(unescapedContent: String): JsonStringLiteral {
        return createValue('"'.toString() + StringUtil.escapeStringCharacters(unescapedContent) + '"')
    }

    fun createProperty(name: String, value: String): JsonProperty {
        val file = createDummyFile("{\"$name\": $value}")
        return (file.firstChild as JsonObject).propertyList[0]
    }

    fun createComma(): PsiElement {
        val jsonArray1 = createValue<JsonArray>("[1, 2]")
        return jsonArray1.valueList[0].nextSibling
    }
}
