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

package com.github.takahirom.fireannotation;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

public class AnnotationUtil {


    public static boolean hasAnnotation(CtClass ctClass, String annotationClass) {
        AnnotationsAttribute annotationInfo = (AnnotationsAttribute) ctClass.getClassFile2().
                getAttribute(AnnotationsAttribute.visibleTag);
        if (annotationInfo == null) {
            return false;

        }
        for (Annotation annotation : annotationInfo.getAnnotations()) {
            if (annotation.getTypeName().equals(annotationClass))
                return true;
        }
        return false;
    }

    public static boolean hasAnnotation(CtMethod method, String annotationClass) {
        return getAnnotation(method, annotationClass) != null;
    }


    public static Annotation getAnnotation(CtMethod method, String annotationClass) {
        AnnotationsAttribute ainfo = (AnnotationsAttribute) method.getMethodInfo2().
                getAttribute(AnnotationsAttribute.visibleTag);
        if (ainfo == null) {
            return null;

        }
        for (Annotation annot : ainfo.getAnnotations()) {
            if (annot.getTypeName().equals(annotationClass))
                return annot;
        }
        return null;
    }


}