// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.navigation

import com.intellij.ide.actions.QualifiedNameProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.jsonSchema.JsonPointerUtil
import com.xxxlin.json.JsonUtil
import com.xxxlin.json.psi.JsonArray
import com.xxxlin.json.psi.JsonElement
import com.xxxlin.json.psi.JsonProperty

/**
 * @author Mikhail Golubev
 */
class JsonQualifiedNameProvider : QualifiedNameProvider {
    override fun adjustElementToCopy(element: PsiElement): PsiElement? {
        return null
    }

    override fun getQualifiedName(element: PsiElement): String? {
        return generateQualifiedName(element, JsonQualifiedNameKind.Qualified)
    }

    override fun qualifiedNameToElement(fqn: String, project: Project): PsiElement? {
        return null
    }

    companion object {
        @JvmStatic
        fun generateQualifiedName(element: PsiElement, qualifiedNameKind: JsonQualifiedNameKind): String? {
            var element: PsiElement = element as? JsonElement ?: return null
            var parentProperty =
                PsiTreeUtil.getNonStrictParentOfType(element, JsonProperty::class.java, JsonArray::class.java)
            val builder = StringBuilder()
            while (parentProperty != null) {
                if (parentProperty is JsonProperty) {
                    var name = parentProperty.name
                    if (qualifiedNameKind == JsonQualifiedNameKind.JsonPointer) {
                        name = JsonPointerUtil.escapeForJsonPointer(name)
                    }
                    builder.insert(0, name)
                    builder.insert(0, if (qualifiedNameKind == JsonQualifiedNameKind.JsonPointer) "/" else ".")
                } else {
                    val index =
                        JsonUtil.getArrayIndexOfItem(if (element is JsonProperty) element.getParent() else element)
                    if (index == -1) return null
                    builder.insert(
                        0,
                        if (qualifiedNameKind == JsonQualifiedNameKind.JsonPointer) ("/$index") else ("[$index]")
                    )
                }
                element = parentProperty
                parentProperty =
                    PsiTreeUtil.getParentOfType(parentProperty, JsonProperty::class.java, JsonArray::class.java)
            }

            if (builder.length == 0) return null

            // if the first operation is array indexing, we insert the 'root' element $
            if (builder[0] == '[') {
                builder.insert(0, "$")
            }

            return StringUtil.trimStart(builder.toString(), ".")
        }
    }
}
