// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.xxxlin.json.psi.JsonObject
import com.xxxlin.json.psi.JsonProperty

/**
 * @author Mikhail Golubev
 */
abstract class JsonObjectMixin(node: ASTNode) : JsonContainerImpl(node), JsonObject {
    private val myPropertyCache = CachedValueProvider {
        val cache: MutableMap<String, JsonProperty> = HashMap()
        for (property in propertyList) {
            val propertyName = property.name
            // Preserve the old behavior - return the first value in findProperty()
            if (!cache.containsKey(propertyName)) {
                cache[propertyName] = property
            }
        }
        CachedValueProvider.Result.createSingleDependency<Map<String, JsonProperty>>(cache, this)
    }

    override fun findProperty(name: String): JsonProperty? {
        return CachedValuesManager.getCachedValue(
            this,
            myPropertyCache
        )[name]
    }
}
