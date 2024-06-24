// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.xxxlin.json.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.xxxlin.json.JsonElementTypes

object JsonPsiChangeUtils {
    @JvmStatic
    fun removeCommaSeparatedFromList(myNode: ASTNode, parent: ASTNode) {
        var from = myNode
        var to = myNode.treeNext

        var seenComma = false

        var toCandidate = to
        while (toCandidate != null && toCandidate.elementType === TokenType.WHITE_SPACE) {
            toCandidate = toCandidate.treeNext
        }

        if (toCandidate != null && toCandidate.elementType === JsonElementTypes.COMMA) {
            toCandidate = toCandidate.treeNext
            to = toCandidate
            seenComma = true

            if (to != null && to.elementType === TokenType.WHITE_SPACE) {
                to = to.treeNext
            }
        }

        if (!seenComma) {
            var treePrev = from.treePrev

            while (treePrev != null && treePrev.elementType === TokenType.WHITE_SPACE) {
                from = treePrev
                treePrev = treePrev.treePrev
            }
            if (treePrev != null && treePrev.elementType === JsonElementTypes.COMMA) {
                from = treePrev
            }
        }

        parent.removeRange(from, to)
    }
}
