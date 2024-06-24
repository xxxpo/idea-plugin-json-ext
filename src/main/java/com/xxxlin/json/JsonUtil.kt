// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.util.ObjectUtils
import com.xxxlin.json.psi.*
import org.jetbrains.annotations.Contract

/**
 * @author Mikhail Golubev
 */
object JsonUtil {
    /**
     * Clone of C# "as" operator.
     * Checks if expression has correct type and casts it if it has. Returns null otherwise.
     * It saves coder from "instanceof / cast" chains.
     *
     *
     * Copied from PyCharm's `PyUtil`.
     *
     * @param expression expression to check
     * @param cls        class to cast
     * @param <T>        class to cast
     * @return expression casted to appropriate type (if could be casted). Null otherwise.
    </T> */
    fun <T> `as`(expression: Any?, cls: Class<T>): T? {
        if (expression == null) {
            return null
        }
        if (cls.isAssignableFrom(expression.javaClass)) {
            return expression as T
        }
        return null
    }

    fun <T : JsonElement?> getPropertyValueOfType(
        `object`: JsonObject, name: String,
        clazz: Class<T>
    ): T? {
        val property = `object`.findProperty(name) ?: return null
        return ObjectUtils.tryCast(property.value, clazz)
    }

    fun isArrayElement(element: PsiElement): Boolean {
        return element is JsonValue && element.getParent() is JsonArray
    }

    fun getArrayIndexOfItem(e: PsiElement): Int {
        val parent = e.parent as? JsonArray ?: return -1
        val elements = parent.valueList
        for (i in elements.indices) {
            if (e === elements[i]) {
                return i
            }
        }
        return -1
    }

    @Contract("null -> null")
    fun getTopLevelObject(jsonFile: JsonFile?): JsonObject? {
        return if (jsonFile != null) ObjectUtils.tryCast(jsonFile.topLevelValue, JsonObject::class.java) else null
    }

    fun isJsonFile(file: VirtualFile, project: Project?): Boolean {
        val type = file.fileType
        if (type is LanguageFileType && type.language is JsonLanguage) return true
        if (project == null || !ScratchUtil.isScratch(file)) return false
        val rootType = ScratchFileService.findRootType(file)
        return rootType != null && rootType.substituteLanguage(project, file) is JsonLanguage
    }
}
