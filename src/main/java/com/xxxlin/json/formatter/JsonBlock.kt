// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.JsonTokenSets
import com.xxxlin.json.psi.JsonArray
import com.xxxlin.json.psi.JsonObject
import com.xxxlin.json.psi.JsonProperty
import com.xxxlin.json.psi.JsonPsiUtil

/**
 * @author Mikhail Golubev
 */
class JsonBlock(
    private val myParent: JsonBlock?,
    private val myNode: ASTNode,
    private val myCustomSettings: JsonCodeStyleSettings,
    private val myAlignment: Alignment?,
    private val myIndent: Indent,
    private val myWrap: Wrap?,
    private val mySpacingBuilder: SpacingBuilder
) : ASTBlock {
    private val myPsiElement: PsiElement = myNode.psi

    // lazy initialized on first call to #getSubBlocks()
    private var mySubBlocks: MutableList<Block>? = null

    private val myPropertyValueAlignment: Alignment?
    private var myChildWrap: Wrap? = null

    init {
        myChildWrap = if (myPsiElement is JsonObject) {
            Wrap.createWrap(myCustomSettings.OBJECT_WRAPPING, true)
        } else if (myPsiElement is JsonArray) {
            Wrap.createWrap(myCustomSettings.ARRAY_WRAPPING, true)
        } else {
            null
        }

        myPropertyValueAlignment = if (myPsiElement is JsonObject) Alignment.createAlignment(true) else null
    }

    override fun getNode(): ASTNode {
        return myNode
    }

    override fun getTextRange(): TextRange {
        return myNode.textRange
    }

    override fun getSubBlocks(): List<Block> {
        if (mySubBlocks == null) {
            val propertyAlignment = myCustomSettings.PROPERTY_ALIGNMENT
            val children = myNode.getChildren(null)
            mySubBlocks = ArrayList(children.size)
            for (child in children) {
                if (isWhitespaceOrEmpty(child)) continue
                mySubBlocks?.add(makeSubBlock(child, propertyAlignment))
            }
        }
        return mySubBlocks!!
    }

    private fun makeSubBlock(childNode: ASTNode, propertyAlignment: Int): Block {
        var indent = Indent.getNoneIndent()
        var alignment: Alignment? = null
        var wrap: Wrap? = null

        if (JsonPsiUtil.hasElementType(myNode, JsonTokenSets.JSON_CONTAINERS)) {
            if (JsonPsiUtil.hasElementType(childNode, JsonElementTypes.COMMA)) {
                wrap = Wrap.createWrap(WrapType.NONE, true)
            } else if (!JsonPsiUtil.hasElementType(childNode, JSON_ALL_BRACES)) {
                checkNotNull(myChildWrap)
                wrap = myChildWrap
                indent = Indent.getNormalIndent()
            } else if (JsonPsiUtil.hasElementType(childNode, JSON_OPEN_BRACES)) {
                if (JsonPsiUtil.isPropertyValue(myPsiElement) && propertyAlignment == JsonCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE) {
                    // WEB-13587 Align compound values on opening brace/bracket, not the whole block
                    assert(myParent?.myParent?.myPropertyValueAlignment != null)
                    alignment = myParent!!.myParent!!.myPropertyValueAlignment
                }
            }
        } else if (JsonPsiUtil.hasElementType(myNode, JsonElementTypes.PROPERTY)) {
            assert(myParent?.myPropertyValueAlignment != null)
            if (JsonPsiUtil.hasElementType(
                    childNode,
                    JsonElementTypes.COLON
                ) && propertyAlignment == JsonCodeStyleSettings.ALIGN_PROPERTY_ON_COLON
            ) {
                alignment = myParent!!.myPropertyValueAlignment
            } else if (JsonPsiUtil.isPropertyValue(childNode.psi) && propertyAlignment == JsonCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE) {
                if (!JsonPsiUtil.hasElementType(childNode, JsonTokenSets.JSON_CONTAINERS)) {
                    alignment = myParent!!.myPropertyValueAlignment
                }
            }
        }
        return JsonBlock(this, childNode, myCustomSettings, alignment, indent, wrap, mySpacingBuilder)
    }

    override fun getWrap(): Wrap? {
        return myWrap
    }

    override fun getIndent(): Indent {
        return myIndent
    }

    override fun getAlignment(): Alignment? {
        return myAlignment
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return mySpacingBuilder.getSpacing(this, child1, child2)
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        if (JsonPsiUtil.hasElementType(myNode, JsonTokenSets.JSON_CONTAINERS)) {
            // WEB-13675: For some reason including alignment in child attributes causes
            // indents to consist solely of spaces when both USE_TABS and SMART_TAB
            // options are enabled.
            return ChildAttributes(Indent.getNormalIndent(), null)
        } else if (myNode.psi is PsiFile) {
            return ChildAttributes(Indent.getNoneIndent(), null)
        }
        // Will use continuation indent for cases like { "foo"<caret>  }
        return ChildAttributes(null, null)
    }

    override fun isIncomplete(): Boolean {
        val lastChildNode = myNode.lastChildNode
        if (JsonPsiUtil.hasElementType(myNode, JsonElementTypes.OBJECT)) {
            return lastChildNode != null && lastChildNode.elementType !== JsonElementTypes.R_CURLY
        } else if (JsonPsiUtil.hasElementType(myNode, JsonElementTypes.ARRAY)) {
            return lastChildNode != null && lastChildNode.elementType !== JsonElementTypes.R_BRACKET
        } else if (JsonPsiUtil.hasElementType(myNode, JsonElementTypes.PROPERTY)) {
            return (myPsiElement as JsonProperty).value == null
        }
        return false
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }

    companion object {
        private val JSON_OPEN_BRACES = TokenSet.create(JsonElementTypes.L_BRACKET, JsonElementTypes.L_CURLY)
        private val JSON_CLOSE_BRACES = TokenSet.create(JsonElementTypes.R_BRACKET, JsonElementTypes.R_CURLY)
        private val JSON_ALL_BRACES = TokenSet.orSet(JSON_OPEN_BRACES, JSON_CLOSE_BRACES)

        private fun isWhitespaceOrEmpty(node: ASTNode): Boolean {
            return node.elementType === TokenType.WHITE_SPACE || node.textLength == 0
        }
    }
}
