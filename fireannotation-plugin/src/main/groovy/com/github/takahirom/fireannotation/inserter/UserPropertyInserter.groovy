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

package com.github.takahirom.fireannotation.inserter

import com.github.takahirom.fireannotation.AnnotationUtil
import groovy.text.SimpleTemplateEngine
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.annotation.MemberValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class UserPropertyInserter {
    public static final String FIRE_LOG_ANNOTION_FQDN                 \
            = "com.github.takahirom.fireannotation.annotation.FireUserProperty"

    public static final Logger logger = LoggerFactory.getLogger('fire-plugin-transform-firelog')

    public static List<CtMethod> insert(ArrayList<CtMethod> methods, CtClass clazz) {
        methods.findAll { method ->
            AnnotationUtil.hasAnnotation(method, FIRE_LOG_ANNOTION_FQDN)
        }.each { method ->
            logger.warn(" * User Property Insert " + method.longName)

            String text = generateInsertText(method)

            println(text)

            method.insertAfter(text)

        }
    }

    protected static String generateInsertText(CtMethod method) {
        CtClass clazz = method.getDeclaringClass()
// get data form annotation
        def annotation = AnnotationUtil.getAnnotation(method, FIRE_LOG_ANNOTION_FQDN)
        def paramAnnotationProperty = annotation.getMemberValue("property")
        def customParamAnnotationProperty = annotation.getMemberValue("customProperty")

        String thisStatement = getOuterThis(clazz)

        LinkedHashMap<String, Object> templateValueMap = buildTemplateValueMap(thisStatement, paramAnnotationProperty, customParamAnnotationProperty)

        def template = '''\
${custom_value_creator}
com.github.takahirom.fireannotation.internal.FirebaseInvoker.sendUserProperty("${param_annotation_property}", ${creator_variable}, ${this_statement}, \\$args);
'''
        def engine = new SimpleTemplateEngine()
        String text = engine.createTemplate(template).make(templateValueMap)
        text
    }

    private
    static LinkedHashMap<String, Object> buildTemplateValueMap(String thisStatement, MemberValue paramAnnotationProperty, MemberValue customParamAnnotationProperty) {
        def templateValueMap = [
                this_statement: thisStatement,
        ]
        if (paramAnnotationProperty != null) {
            templateValueMap["param_annotation_property"] = paramAnnotationProperty.value
        } else {
            templateValueMap["param_annotation_property"] = ""
        }

        if (customParamAnnotationProperty == null) {
            templateValueMap["custom_value_creator"] = "";
            templateValueMap["creator_variable"] = "null";
        } else {
            def engine = new SimpleTemplateEngine()
            templateValueMap["custom_value_creator"] = engine.createTemplate(
                    'com.github.takahirom.fireannotation.CustomValueCreator creator = new $custom_param_annotation_parameter();')
                    .make([custom_param_annotation_parameter: customParamAnnotationProperty.value,])
            templateValueMap["creator_variable"] = "creator";
        }
        templateValueMap
    }

    private static String getOuterThis(CtClass clazz) {
        def declaringClass = clazz.getDeclaringClass()
        if (declaringClass == null) {
            return "this"
        }
        return "this\$0"
    }
}
