// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.xxxlin.json.psi.impl.JsonFileImpl

open class JsonParserDefinition : ParserDefinition {
    override fun createLexer(project: Project): Lexer {
        return JsonLexer()
    }

    override fun createParser(project: Project): PsiParser {
        return JsonParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun getCommentTokens(): TokenSet {
        return JsonTokenSets.JSON_COMMENTARIES
    }

    override fun getStringLiteralElements(): TokenSet {
        return JsonTokenSets.STRING_LITERALS
    }

    override fun createElement(astNode: ASTNode): PsiElement {
        return JsonElementTypes.Factory.createElement(astNode)
    }

    override fun createFile(fileViewProvider: FileViewProvider): PsiFile {
        return JsonFileImpl(fileViewProvider, JsonLanguage.INSTANCE)
    }

    override fun spaceExistenceTypeBetweenTokens(
        astNode: ASTNode,
        astNode2: ASTNode
    ): ParserDefinition.SpaceRequirements {
        return ParserDefinition.SpaceRequirements.MAY
    }

    companion object {
        val FILE: IFileElementType = IFileElementType(JsonLanguage.INSTANCE)
    }
}
