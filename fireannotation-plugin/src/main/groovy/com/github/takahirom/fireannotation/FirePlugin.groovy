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

import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Method

class FirePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        Logger logger = LoggerFactory.getLogger('fire-plugin')
        logger.warn("FirePlugin apply")

        def isAndroidApp = project.plugins.withType(AppPlugin)
        def isAndroidLib = project.plugins.withType(LibraryPlugin)
        if (!isAndroidApp && !isAndroidLib) {
            throw new GradleException("'com.android.application' or 'com.android.library' plugin required.")
        }

        if (!isTransformAvailable()) {
            throw new GradleException('FirePlugin gradle plugin only supports android gradle plugin 2.0.0 or later')
        }


        def compileDeps = project.getConfigurations().getByName("compile").getDependencies()
        project.getGradle().addListener(new DependencyResolutionListener() {
            @Override
            void beforeResolve(ResolvableDependencies resolvableDependencies) {
                project.android.registerTransform(new AnnotationTransformer(project))
                compileDeps.add(project.getDependencies().create("com.github.takahirom.fireannotation.library:fireannotation-library:0.2.0"))
                project.getGradle().removeListener(this)
            }

            @Override
            void afterResolve(ResolvableDependencies resolvableDependencies) {}
        })
    }

    private static boolean isTransformAvailable() {
        try {
            Class transform = Class.forName('com.android.build.api.transform.Transform')
            Method transformMethod = transform.getMethod("transform", [TransformInvocation.class] as Class[])
            return transformMethod != null
        } catch (Exception ignored) {
            return false
        }
    }
}
