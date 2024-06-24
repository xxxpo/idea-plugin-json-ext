// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.psi.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveVisitor
import com.xxxlin.json.psi.JsonElementVisitor

/**
 * @author Mikhail Golubev
 */
open class JsonRecursiveElementVisitor : JsonElementVisitor(), PsiRecursiveVisitor {
    override fun visitElement(element: PsiElement) {
        element.acceptChildren(this)
    }
}
