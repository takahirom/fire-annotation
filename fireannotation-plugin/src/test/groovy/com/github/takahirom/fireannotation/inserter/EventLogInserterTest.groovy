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

import javassist.ClassPool
import javassist.CtMethod
import javassist.CtNewMethod
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.ConstPool
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.ClassMemberValue
import javassist.bytecode.annotation.StringMemberValue

class EventLogInserterTest extends GroovyTestCase {


    public static
    final String EXPECTED_ALL_CONTAINS = "com.github.takahirom.fireannotation.CustomValueCreator creator = new java.lang.Object.class();\n" +
            "com.github.takahirom.fireannotation.internal.FirebaseInvoker.sendEventLog(\"eventName\",\"testParameter\",creator, this, \$args);\n"

    void testNoParameterMethod() {
        assertInsertText(EXPECTED_ALL_CONTAINS, createAnnotation("eventName", "testParameter", "java.lang.Object.class"), "public void test(){}")
    }

    void testOneParameterMethod() {
        assertInsertText(EXPECTED_ALL_CONTAINS, createAnnotation("eventName", "testParameter", "java.lang.Object.class"), "public void test(java.lang.String parameter){}")
    }

    void testStaticMethod() {
          String exptected = "com.github.takahirom.fireannotation.CustomValueCreator creator = new java.lang.Object.class();\n" +
            "com.github.takahirom.fireannotation.internal.FirebaseInvoker.sendEventLog(\"eventName\",\"testParameter\",creator, null, \$args);\n"
        assertInsertText(exptected, createAnnotation("eventName", "testParameter", "java.lang.Object.class"), "public static void test(java.lang.String parameter){}")
    }

    void testNoCreatorMethod() {
        final String noCreatorExpected = "com.github.takahirom.fireannotation.internal.FirebaseInvoker.sendEventLog(\"eventName\",\"testParameter\" ,null, this, \$args);\n"
        assertInsertText(noCreatorExpected, createAnnotation("eventName", "testParameter", null), "public void test(java.lang.String parameter){}")
    }

    void testNoEventNoCreatorParameterMethod() {
        final String expected = "com.github.takahirom.fireannotation.internal.FirebaseInvoker.sendEventLog(\"eventName\",\"\", null, this, \$args);\n"
        assertInsertText(expected, createAnnotation("eventName", null, null), "public void test(java.lang.String parameter){}")
    }


    private
    static void assertInsertText(String expected, AnnotationsAttribute attr, String methodText) {

        def objectClass = ClassPool.default.getCtClass("java.lang.Object")

        CtMethod method = CtNewMethod.make(methodText, objectClass)
        method.getMethodInfo().addAttribute(attr)

        // Exec !
        def actual = EventLogInserter.generateInsertText(method)

        String formattedExpected = expected.replaceAll(" ", "").replaceAll("\n", "")
        String formattedActual = actual.replaceAll(" ", "").replaceAll("\n", "")
        assertEquals("\n----\nexpected:\n" + expected + "\nactual:\n" + actual + "\n----\n", formattedExpected, formattedActual)
    }

    private
    static AnnotationsAttribute createAnnotation(String eventValue, String parameterValue, String customPropertyValue) {
        def constPool = new ConstPool("java.lang.Object")
        AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation annot = new Annotation("com.github.takahirom.fireannotation.annotation.FireEventLog", constPool);
        if (eventValue?.trim()) {
            // ?.trim() equal !isEmpty()
            annot.addMemberValue("event", new StringMemberValue(eventValue, constPool));
        }

        if (parameterValue?.trim()) {
            annot.addMemberValue("parameter", new StringMemberValue(parameterValue, constPool));
        }

        if (customPropertyValue?.trim()) {
            annot.addMemberValue("customParameter", new ClassMemberValue(customPropertyValue, constPool));
        }
        attr.addAnnotation(annot);
        attr
    }
}
