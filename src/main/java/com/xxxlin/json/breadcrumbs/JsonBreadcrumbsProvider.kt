// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.breadcrumbs

import com.intellij.lang.Language
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import com.jetbrains.jsonSchema.impl.JsonSchemaDocumentationProvider
import com.xxxlin.json.JsonBundle
import com.xxxlin.json.JsonLanguage
import com.xxxlin.json.JsonUtil
import com.xxxlin.json.navigation.JsonQualifiedNameKind
import com.xxxlin.json.navigation.JsonQualifiedNameProvider.Companion.generateQualifiedName
import com.xxxlin.json.psi.JsonProperty
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action

/**
 * @author Mikhail Golubev
 */
class JsonBreadcrumbsProvider : BreadcrumbsProvider {

    private val languages = arrayOf<Language>(
        JsonLanguage.INSTANCE
    )

    override fun getLanguages(): Array<Language> {
        return languages
    }

    override fun acceptElement(e: PsiElement): Boolean {
        return e is JsonProperty || JsonUtil.isArrayElement(e)
    }

    override fun getElementInfo(e: PsiElement): String {
        if (e is JsonProperty) {
            return e.name
        } else if (JsonUtil.isArrayElement(e)) {
            val i = JsonUtil.getArrayIndexOfItem(e)
            if (i != -1) {
                return i.toString()
            }
        }
        throw AssertionError("Breadcrumbs can be extracted only from JsonProperty elements or JsonArray child items")
    }

    override fun getElementTooltip(e: PsiElement): String? {
        return JsonSchemaDocumentationProvider.findSchemaAndGenerateDoc(e, null, true, null)
    }

    override fun getContextActions(element: PsiElement): List<Action> {
        val values: Array<JsonQualifiedNameKind> =
            JsonQualifiedNameKind.entries.toTypedArray<JsonQualifiedNameKind>()

        val actions: MutableList<Action> = ArrayList(values.size)
        for (kind in values) {
            actions.add(object : AbstractAction(JsonBundle.message("json.copy.to.clipboard", kind.toString())) {
                override fun actionPerformed(e: ActionEvent) {
                    CopyPasteManager.getInstance().setContents(StringSelection(generateQualifiedName(element, kind)))
                }
            })
        }
        return actions
    }

    override fun isShownByDefault(): Boolean {
        return false
    }

}
