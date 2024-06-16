package com.xxxlin.json.reference

import com.xxxlin.json.psi.JsonProperty
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * 提供或查找 JSON 规则引用
 */
class JsonKeyReference(
    element: PsiElement,
    textRange: TextRange,
    private val jsonProperty: JsonProperty
) : PsiReferenceBase<PsiElement>(
    element, textRange
) {

    override fun resolve(): PsiElement {
        return jsonProperty
    }

}