//// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
//package com.xxxlin.json.structureView
//
//import com.intellij.ide.structureView.StructureViewBuilder
//import com.intellij.openapi.extensions.ExtensionPointName
//import com.xxxlin.json.psi.JsonFile
//
///**
// * Used for customization of default structure view for JSON files.
// * Note that in case several extensions for current EP are registered,
// * the behaviour is undefined and can be changed by `order` attribute in EP registration.
// * Therefore, there is no guarantee that the expected builder will be returned every time.
// */
//interface JsonCustomStructureViewFactory {
//    /**
//     * The first not-null builder received from all registered extensions will be used for building structure view.
//     * If the extension list is empty, the default implementation is used.
//     *
//     * @return a structure view builder for the given JSON file or `null` if the file doesn't need customized structure view.
//     */
//    fun getStructureViewBuilder(jsonFile: JsonFile): StructureViewBuilder?
//
//    companion object {
//        val EP_NAME =
//            ExtensionPointName.create<JsonCustomStructureViewFactory>("com.xxxlin.json.customStructureViewFactory")
//    }
//}
