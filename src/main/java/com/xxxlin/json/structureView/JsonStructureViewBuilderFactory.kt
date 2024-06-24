//// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
//package com.xxxlin.json.structureView
//
//import com.intellij.ide.impl.StructureViewWrapperImpl
//import com.intellij.ide.structureView.StructureViewBuilder
//import com.intellij.ide.structureView.StructureViewModel
//import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
//import com.intellij.lang.PsiStructureViewFactory
//import com.intellij.openapi.application.ApplicationManager
//import com.intellij.openapi.diagnostic.Logger
//import com.intellij.openapi.editor.Editor
//import com.intellij.openapi.extensions.ExtensionPointUtil
//import com.intellij.psi.PsiFile
//import com.xxxlin.json.psi.JsonFile
//
///**
// * @author Mikhail Golubev
// */
//class JsonStructureViewBuilderFactory : PsiStructureViewFactory {
//    init {
//        JsonCustomStructureViewFactory.EP_NAME.addChangeListener(
//            {
//                ApplicationManager.getApplication().messageBus.syncPublisher(StructureViewWrapperImpl.STRUCTURE_CHANGED)
//                    .run()
//            },
//            ExtensionPointUtil.createKeyedExtensionDisposable(this, PsiStructureViewFactory.EP_NAME.point)
//        )
//    }
//
//    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
//        if (psiFile !is JsonFile) {
//            return null
//        }
//
//        val extensionList = JsonCustomStructureViewFactory.EP_NAME.extensionList
//        if (extensionList.size > 1) {
//            Logger.getInstance(JsonStructureViewBuilderFactory::class.java)
//                .warn(
//                    "Several extensions are registered for JsonCustomStructureViewFactory extension point. " +
//                            "Conflicts can arise if there are several builders corresponding to the same file."
//                )
//        }
//
//        for (extension in extensionList) {
//            val builder = extension.getStructureViewBuilder(psiFile)
//            if (builder != null) {
//                return builder
//            }
//        }
//
//        return object : TreeBasedStructureViewBuilder() {
//            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
//                return JsonStructureViewModel(psiFile, editor)
//            }
//        }
//    }
//}
