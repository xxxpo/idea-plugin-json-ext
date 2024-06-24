// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.structureView

import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.xxxlin.json.psi.JsonArray
import com.xxxlin.json.psi.JsonFile
import com.xxxlin.json.psi.JsonObject
import com.xxxlin.json.psi.JsonProperty

/**
 * @author Mikhail Golubev
 */
class JsonStructureViewModel(psiFile: PsiFile, editor: Editor?) : StructureViewModelBase(
    psiFile, editor, JsonStructureViewElement(
        (psiFile as JsonFile)
    )
), ElementInfoProvider {
    init {
        withSuitableClasses(
            JsonFile::class.java,
            JsonProperty::class.java,
            JsonObject::class.java,
            JsonArray::class.java
        )
        withSorters(Sorter.ALPHA_SORTER)
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        return false
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        return false
    }
}
