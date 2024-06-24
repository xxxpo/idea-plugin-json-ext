package com.xxxlin.json.utils;

import com.intellij.openapi.extensions.ExtensionPointName;

public class ExtensionPointNameUtils {

    public static <T> ExtensionPointName<T> create(String name) {
        return ExtensionPointName.create(name);
    }

}
