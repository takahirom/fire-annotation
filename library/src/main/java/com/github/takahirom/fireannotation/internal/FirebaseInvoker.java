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

package com.github.takahirom.fireannotation.internal;

import android.content.Context;
import android.os.Bundle;

import com.github.takahirom.fireannotation.CustomValueCreator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;
import java.util.Set;

public class FirebaseInvoker {
    @SuppressWarnings({"unchecked", "unused"})
    public static void sendEventLog(String event, String parameters, CustomValueCreator creator, Object thiz) {
        Bundle fireParameter = new Bundle();
        if (creator != null) {
            Map<String, String> customParameter = creator.getValue(thiz);
            if (customParameter != null) {
                final Set<String> customParameterKeySet = customParameter.keySet();
                for (String key : customParameterKeySet) {
                    fireParameter.putString(key, customParameter.get(key));
                }
            }
        }

        String[] splitParameters = parameters.split(",");
        for (String splitParameter : splitParameters) {
            String[] split = splitParameter.split(":");
            if (split.length < 2) {
                continue;
            }
            String key = split[0].trim();
            String value = split[1].trim();
            fireParameter.putString(key, value);
        }
        FirebaseAnalytics.getInstance(FirebaseApp.getInstance().getApplicationContext()).logEvent(event, fireParameter);
    }

    @SuppressWarnings({"unchecked", "unused"})
    public static void sendUserProperty(String properties, CustomValueCreator creator, Object thiz) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(FirebaseApp.getInstance().getApplicationContext());
        if (creator != null) {
            Map<String, String> customParameter = creator.getValue(thiz);
            if (customParameter != null) {
                final Set<String> customParameterKeySet = customParameter.keySet();
                for (String key : customParameterKeySet) {
                    analytics.setUserProperty(key, customParameter.get(key));
                }
            }
        }

        String[] splitParameters = properties.split(",");
        for (String splitParameter : splitParameters) {
            String[] split = splitParameter.split(":");
            if (split.length < 2) {
                continue;
            }
            String key = split[0].trim();
            String value = split[1].trim();
            analytics.setUserProperty(key, value);
        }
    }
}
