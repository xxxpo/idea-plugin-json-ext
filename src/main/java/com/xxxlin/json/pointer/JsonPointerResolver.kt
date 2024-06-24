// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.pointer

import com.xxxlin.json.pointer.JsonPointerPosition.Companion.parsePointer
import com.xxxlin.json.psi.JsonArray
import com.xxxlin.json.psi.JsonObject
import com.xxxlin.json.psi.JsonValue

class JsonPointerResolver(private val myRoot: JsonValue, private val myPointer: String) {
    fun resolve(): JsonValue? {
        var root: JsonValue? = myRoot
        val steps = parsePointer(myPointer).getSteps()
        for (step in steps) {
            val name = step.name
            if (name != null) {
                if (root !is JsonObject) return null
                val property = root.findProperty(name)
                root = property?.value
            } else {
                val idx = step.idx
                if (idx < 0) return null

                if (root !is JsonArray) {
                    if (root is JsonObject) {
                        val property =
                            root.findProperty(idx.toString()) ?: return null
                        root = property.value
                        continue
                    } else {
                        return null
                    }
                }
                val list = root.valueList
                if (idx >= list.size) return null
                root = list[idx]
            }
        }
        return root
    }
}
