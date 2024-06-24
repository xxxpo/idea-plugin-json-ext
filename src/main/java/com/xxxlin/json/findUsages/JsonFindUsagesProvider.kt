// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.findUsages

import com.intellij.lang.HelpID
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.xxxlin.json.JsonBundle
import com.xxxlin.json.psi.JsonProperty

/**
 * @author Mikhail Golubev
 */
class JsonFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner {
        return JsonWordScanner()
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is PsiNamedElement
    }

    override fun getHelpId(psiElement: PsiElement): String {
        return HelpID.FIND_OTHER_USAGES
    }

    override fun getType(element: PsiElement): String {
        if (element is JsonProperty) {
            return JsonBundle.message("json.property")
        }
        return ""
    }

    override fun getDescriptiveName(element: PsiElement): String {
        val name = if (element is PsiNamedElement) element.name else null
        return name ?: JsonBundle.message("unnamed.desc")
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return getDescriptiveName(element)
    }
}
