// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.ContainerUtil
import com.xxxlin.json.psi.*

/**
 * @author Mikhail Golubev
 */
class JsonStructureViewElement(element: JsonElement) : StructureViewTreeElement {
    private val myElement: JsonElement

    init {
        assert(
            PsiTreeUtil.instanceOf(
                element,
                JsonFile::class.java,
                JsonProperty::class.java,
                JsonObject::class.java,
                JsonArray::class.java
            )
        )
        myElement = element
    }

    override fun getValue(): JsonElement {
        return myElement
    }

    override fun navigate(requestFocus: Boolean) {
        myElement.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean {
        return myElement.canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return myElement.canNavigateToSource()
    }

    override fun getPresentation(): ItemPresentation {
        val presentation = checkNotNull(myElement.presentation)
        return presentation
    }

    override fun getChildren(): Array<TreeElement> {
        var value: JsonElement? = null
        if (myElement is JsonFile) {
            value = myElement.topLevelValue
        } else if (myElement is JsonProperty) {
            value = myElement.value
        } else if (PsiTreeUtil.instanceOf(myElement, JsonObject::class.java, JsonArray::class.java)) {
            value = myElement
        }
        if (value is JsonObject) {
            return ContainerUtil.map2Array(
                value.propertyList,
                TreeElement::class.java
            ) { property: JsonProperty -> JsonStructureViewElement(property) }
        } else if (value is JsonArray) {
            val childObjects: List<TreeElement> =
                ContainerUtil.mapNotNull<JsonValue, TreeElement>(value.valueList) { value1: JsonValue? ->
                    if (value1 is JsonObject && value1.propertyList.isNotEmpty()) {
                        return@mapNotNull JsonStructureViewElement(value1)
                    } else if (value1 is JsonArray && PsiTreeUtil.findChildOfType<JsonProperty?>(
                            value1,
                            JsonProperty::class.java
                        ) != null
                    ) {
                        return@mapNotNull JsonStructureViewElement(value1)
                    }
                    null
                }
            return childObjects.toTypedArray()
        }
        return emptyArray()
    }
}
