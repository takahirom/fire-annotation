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

import java.lang.reflect.Modifier

public class EventLogInserter {
    public static final String FIRE_LOG_ANNOTION_FQDN                      \
                 = "com.github.takahirom.fireannotation.annotation.FireEventLog"

    public static final Logger logger = LoggerFactory.getLogger('fire-plugin-transform-firelog')

    public static List<CtMethod> insert(ArrayList<CtMethod> methods, CtClass clazz) {
        methods.findAll { method ->
            AnnotationUtil.hasAnnotation(method, FIRE_LOG_ANNOTION_FQDN)
        }.each { method ->
            logger.warn(" * Event Log Insert " + method.longName)

            String text = generateInsertText(method)

            println(text)

            method.insertAfter(text)

        }
    }

    public static String generateInsertText(CtMethod method) {
        CtClass clazz = method.getDeclaringClass()

        // get data form annotation
        def annotation = AnnotationUtil.getAnnotation(method, FIRE_LOG_ANNOTION_FQDN)
        def eventAnnotationParameter = annotation.getMemberValue("event")
        def paramAnnotationParameter = annotation.getMemberValue("parameter")
        def customParamAnnotationParameter = annotation.getMemberValue("customParameter")

        String thisStatement
        if(!Modifier.isStatic(method.getModifiers())){
            thisStatement = getOuterThis(clazz)
        }else{
            thisStatement = "null"
        }

        LinkedHashMap<String, Object> templateValueMap = buildTemplateValueMap(thisStatement, eventAnnotationParameter, paramAnnotationParameter, customParamAnnotationParameter)

        def template = '''\
${custom_value_creator}
com.github.takahirom.fireannotation.internal.FirebaseInvoker.sendEventLog(${event_annotation_parameter},"${param_annotation_parameter}", ${creator_variable}, ${this_statement}, \\$args);
'''
        def engine = new SimpleTemplateEngine()
        String text = engine.createTemplate(template).make(templateValueMap)
        text
    }


    private
    static LinkedHashMap<String, Object> buildTemplateValueMap(String thisStatement, MemberValue eventAnnotationParameter, MemberValue paramAnnotationParameter, MemberValue customParamAnnotationParameter) {
        def templateValueMap = [
                this_statement            : thisStatement,
                event_annotation_parameter: eventAnnotationParameter
        ]
        if (paramAnnotationParameter != null) {
            templateValueMap["param_annotation_parameter"] = paramAnnotationParameter.value
        } else {
            templateValueMap["param_annotation_parameter"] = ""
        }

        if (customParamAnnotationParameter == null) {
            templateValueMap["custom_value_creator"] = "";
            templateValueMap["creator_variable"] = "null";
        } else {
            def engine = new SimpleTemplateEngine()
            templateValueMap["custom_value_creator"] = engine.createTemplate(
                    'com.github.takahirom.fireannotation.CustomValueCreator creator = new $custom_param_annotation_parameter();')
                    .make([custom_param_annotation_parameter: customParamAnnotationParameter.value,])
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
