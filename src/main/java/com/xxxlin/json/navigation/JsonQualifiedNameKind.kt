// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.xxxlin.json.navigation

import com.xxxlin.json.JsonBundle

enum class JsonQualifiedNameKind {
    Qualified,
    JsonPointer;

    override fun toString(): String {
        return when (this) {
            Qualified -> JsonBundle.message("qualified.name.qualified")
            JsonPointer -> JsonBundle.message("qualified.name.pointer")
        }
    }
}
