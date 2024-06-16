// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.jsonLines

import com.xxxlin.json.JsonParserDefinition
import com.xxxlin.json.psi.impl.JsonFileImpl
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType

internal class JsonLinesParserDefinition : JsonParserDefinition() {

  override fun createFile(fileViewProvider: FileViewProvider): PsiFile {
    return JsonFileImpl(fileViewProvider, JsonLinesLanguage)
  }

  override fun getFileNodeType(): IFileElementType = FILE

  private val FILE = IFileElementType(JsonLinesLanguage)

}