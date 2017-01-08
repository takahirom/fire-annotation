/*
 * Copyright (C) 2017 takahirom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.takahirom.fireannotation

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.DefaultContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.github.takahirom.fireannotation.inserter.EventLogInserter
import com.github.takahirom.fireannotation.inserter.UserPropertyInserter
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import groovy.io.FileType
import javassist.ClassPool
import javassist.LoaderClassPath
import javassist.Modifier
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory


public class AnnotationTransformer extends Transform {

    public static final Logger logger = LoggerFactory.getLogger('fire-plugin-transform')
    private Project project

    public AnnotationTransformer(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return 'AnnotationTransformer'
    }

    @Override
    Set<ContentType> getInputTypes() {
        return ImmutableSet.<ContentType> of(DefaultContentType.CLASSES)
    }

    @Override
    Set<Scope> getScopes() {
        return Sets.immutableEnumSet(Scope.PROJECT)
    }

    @Override
    Set<Scope> getReferencedScopes() {
        return Sets.immutableEnumSet(Scope.EXTERNAL_LIBRARIES, Scope.PROJECT_LOCAL_DEPS,
                Scope.SUB_PROJECTS, Scope.SUB_PROJECTS_LOCAL_DEPS, Scope.TESTED_CODE)
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        logger.warn("---FirePlugin transform start---")

        super.transform(transformInvocation)

        def outputProvider = transformInvocation.getOutputProvider()

        def outputDir = outputProvider.getContentLocation('fireplugin',
                getInputTypes(), getScopes(), Format.DIRECTORY)

        def inputs = transformInvocation.getInputs()
        def classNames = getClassNames(inputs)

        def mergedInputs = inputs + transformInvocation.getReferencedInputs()
        ClassPool classPool = getClassPool(mergedInputs)

        modify(classNames, classPool)

        classNames.each {
            def ctClass = classPool.getCtClass(it)
            ctClass.writeFile(outputDir.canonicalPath)
        }
        logger.warn("---FirePlugin transform end---")
    }

    private static void modify(Set<String> classNames, ClassPool classPool) {
        classNames.collect { classPool.getCtClass(it) }
                .each { clazz ->
            def methods = clazz.declaredMethods.findAll {
                !Modifier.isNative(it.getModifiers()) \
                    && !Modifier.isInterface(it.getModifiers()) \
                    && !Modifier.isAbstract(it.getModifiers())
            }
            // for @FireEventLog
            EventLogInserter.insert(methods, clazz)
            // for @FireUserProperty
            UserPropertyInserter.insert(methods, clazz)
        }
    }

    private ClassPool getClassPool(Collection<TransformInput> inputs) {
        ClassPool classPool = new ClassPool(null)
        classPool.appendSystemPath()
        classPool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()))

        inputs.each {
            it.directoryInputs.each {
                classPool.appendClassPath(it.file.absolutePath)
            }

            it.jarInputs.each {
                classPool.appendClassPath(it.file.absolutePath)
            }
        }

        project.android.bootClasspath.each {
            String path = it.absolutePath
            classPool.appendClassPath(path)
        }

        return classPool
    }

    static Set<String> getClassNames(Collection<TransformInput> inputs) {
        Set<String> classNames = new HashSet<String>()

        inputs.each {
            it.directoryInputs.each {
                def dirPath = it.file.absolutePath
                it.file.eachFileRecurse(FileType.FILES) {
                    if (it.absolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                        def className =
                                it.absolutePath.substring(dirPath.length() + 1, it.absolutePath.length() - 6)
                                        .replace(File.separatorChar, '.' as char)
                        classNames.add(className)
                    }
                }
            }
        }
        return classNames
    }
}
