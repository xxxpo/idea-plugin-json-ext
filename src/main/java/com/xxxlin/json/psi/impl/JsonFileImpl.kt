// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.psi.impl

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.util.PsiTreeUtil
import com.xxxlin.json.psi.JsonFile
import com.xxxlin.json.psi.JsonValue

class JsonFileImpl(fileViewProvider: FileViewProvider?, language: Language?) : PsiFileBase(
    fileViewProvider!!, language!!
), JsonFile {
    override fun getFileType(): FileType {
        return viewProvider.fileType
    }

    override val topLevelValue: JsonValue?
        get() = PsiTreeUtil.getChildOfType(this, JsonValue::class.java)

    override val allTopLevelValues: List<JsonValue>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, JsonValue::class.java)

    override fun toString(): String {
        return "JsonFile: $name"
    }
}
