// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.codeinsight

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.*
import com.intellij.util.PlatformIcons
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.MultiMap
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import com.xxxlin.json.JsonBundle
import com.xxxlin.json.psi.JsonElementVisitor
import com.xxxlin.json.psi.JsonObject
import org.jetbrains.annotations.Nls
import java.util.stream.Collectors
import javax.swing.Icon

class JsonDuplicatePropertyKeysInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val isSchemaFile = JsonSchemaService.isSchemaFile(holder.file)
        return object : JsonElementVisitor() {
            override fun visitObject(o: JsonObject) {
                val keys = MultiMap<String, PsiElement>()
                for (property in o.propertyList) {
                    keys.putValue(property.name, property.nameElement)
                }
                visitKeys(keys, isSchemaFile, holder)
            }
        }
    }

    class NavigateToDuplicatesFix internal constructor(
        sameNamedKeys: Collection<PsiElement>,
        element: PsiElement,
        private val myEntryKey: String
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
        private val mySameNamedKeys: Collection<SmartPsiElementPointer<PsiElement>> =
            ContainerUtil.map(sameNamedKeys) { k: PsiElement -> SmartPointerManager.createPointer(k) }

        override fun getText(): String {
            return JsonBundle.message("navigate.to.duplicates")
        }

        override fun getFamilyName(): @Nls(capitalization = Nls.Capitalization.Sentence) String {
            return text
        }

        override fun invoke(
            project: Project,
            file: PsiFile,
            editor: Editor?,
            startElement: PsiElement,
            endElement: PsiElement
        ) {
            if (editor == null) return

            if (mySameNamedKeys.size == 2) {
                val iterator = mySameNamedKeys.iterator()
                val next = iterator.next().element
                val toNavigate = if (next !== startElement) next else iterator.next().element
                if (toNavigate == null) return
                navigateTo(editor, toNavigate)
            } else {
                val allElements =
                    mySameNamedKeys.stream()
                        .map { k: SmartPsiElementPointer<PsiElement> -> k.element }
                        .filter { k: PsiElement? -> k !== startElement }
                        .collect(Collectors.toList())
                JBPopupFactory.getInstance().createListPopup(
                    object : BaseListPopupStep<PsiElement>(
                        JsonBundle.message("navigate.to.duplicates.header", myEntryKey),
                        allElements
                    ) {
                        override fun getIconFor(aValue: PsiElement): Icon {
                            return PlatformIcons.PROPERTY_ICON
                        }

                        override fun getTextFor(value: PsiElement): String {
                            return JsonBundle
                                .message(
                                    "navigate.to.duplicates.desc",
                                    myEntryKey,
                                    editor.document.getLineNumber(value.textOffset)
                                )
                        }

                        override fun getDefaultOptionIndex(): Int {
                            return 0
                        }

                        override fun onChosen(selectedValue: PsiElement, finalChoice: Boolean): PopupStep<*>? {
                            navigateTo(editor, selectedValue)
                            return FINAL_CHOICE
                        }

                        override fun isSpeedSearchEnabled(): Boolean {
                            return true
                        }
                    }).showInBestPositionFor(editor)
            }
        }

        companion object {
            private fun navigateTo(editor: Editor, toNavigate: PsiElement) {
                editor.caretModel.moveToOffset(toNavigate.textOffset)
                editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
            }
        }
    }

    companion object {
        private const val COMMENT = "\$comment"

        protected fun visitKeys(keys: MultiMap<String, PsiElement>, isSchemaFile: Boolean, holder: ProblemsHolder) {
            for ((entryKey, sameNamedKeys) in keys.entrySet()) {
                if (sameNamedKeys.size > 1 && (!isSchemaFile || !COMMENT.equals(entryKey, ignoreCase = true))) {
                    for (element in sameNamedKeys) {
                        holder.registerProblem(
                            element, JsonBundle.message("inspection.duplicate.keys.msg.duplicate.keys", entryKey),
                            getNavigateToDuplicatesFix(sameNamedKeys, element, entryKey)
                        )
                    }
                }
            }
        }

        protected fun getNavigateToDuplicatesFix(
            sameNamedKeys: Collection<PsiElement>,
            element: PsiElement,
            entryKey: String
        ): NavigateToDuplicatesFix {
            return NavigateToDuplicatesFix(sameNamedKeys, element, entryKey)
        }
    }
}
