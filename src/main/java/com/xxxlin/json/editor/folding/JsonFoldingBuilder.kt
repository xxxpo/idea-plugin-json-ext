// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Couple
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.psi.*

/**
 * @author Mikhail Golubev
 */
class JsonFoldingBuilder : FoldingBuilder, DumbAware {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val descriptors: MutableList<FoldingDescriptor> = ArrayList()
        collectDescriptorsRecursively(node, document, descriptors)
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        val type = node.elementType
        if (type === JsonElementTypes.OBJECT) {
            val `object` = node.getPsi(JsonObject::class.java)
            val properties = `object`.propertyList
            var candidate: JsonProperty? = null
            for (property in properties) {
                val name = property.name
                val value = property.value
                if (value is JsonLiteral) {
                    if ("id" == name || "name" == name) {
                        candidate = property
                        break
                    }
                    if (candidate == null) {
                        candidate = property
                    }
                }
            }
            return if (candidate != null) {
                "{\"" + candidate.name + "\": " + candidate.value!!.text + "...}"
            } else {
                "properties count=" + properties.size
            }
        } else if (type === JsonElementTypes.ARRAY && node.psi is JsonArray) {
            return "array size=" + (node.psi as JsonArray).getValueList().size
        } else if (type === JsonElementTypes.LINE_COMMENT1) {
            return "//..."
        } else if (type === JsonElementTypes.LINE_COMMENT2) {
            return "#..."
        } else if (type === JsonElementTypes.BLOCK_COMMENT) {
            return "/*...*/"
        }
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }

    companion object {
        private fun collectDescriptorsRecursively(
            node: ASTNode,
            document: Document,
            descriptors: MutableList<FoldingDescriptor>
        ) {
            val type = node.elementType
            if ((type === JsonElementTypes.OBJECT || type === JsonElementTypes.ARRAY) && spanMultipleLines(
                    node,
                    document
                )
            ) {
                descriptors.add(FoldingDescriptor(node, node.textRange))
            } else if (type === JsonElementTypes.BLOCK_COMMENT) {
                descriptors.add(FoldingDescriptor(node, node.textRange))
            } else if (type === JsonElementTypes.LINE_COMMENT1
                || type === JsonElementTypes.LINE_COMMENT2
            ) {
                val commentRange = expandLineCommentsRange(node.psi)
                val startOffset = commentRange.getFirst().textRange.startOffset
                val endOffset = commentRange.getSecond().textRange.endOffset
                if (document.getLineNumber(startOffset) != document.getLineNumber(endOffset)) {
                    descriptors.add(FoldingDescriptor(node, TextRange(startOffset, endOffset)))
                }
            }

            for (child in node.getChildren(null)) {
                collectDescriptorsRecursively(child, document, descriptors)
            }
        }

        fun expandLineCommentsRange(anchor: PsiElement): Couple<PsiElement> {
            return Couple.of(
                JsonPsiUtil.findFurthestSiblingOfSameType(anchor, false),
                JsonPsiUtil.findFurthestSiblingOfSameType(anchor, true)
            )
        }

        private fun spanMultipleLines(node: ASTNode, document: Document): Boolean {
            val range = node.textRange
            val endOffset = range.endOffset
            return (document.getLineNumber(range.startOffset)
                    < (if (endOffset < document.textLength) document.getLineNumber(endOffset) else document.lineCount - 1))
        }
    }
}
