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

package com.github.takahirom.fireannotation.sample;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.github.takahirom.fireannotation.annotation.FireEventLog;
import com.github.takahirom.fireannotation.annotation.FireUserProperty;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {
    public static final String BUTTON_COLOR_KEY = "button_color";
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private ImageButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (ImageButton) findViewById(R.id.color_button);
        initRemoteConfig();
        // do not wait remoteconfig fetch end
        applyButtonColor();

        button.setOnClickListener(new View.OnClickListener() {
            @FireEventLog(event = FirebaseAnalytics.Event.SELECT_CONTENT, parameter = "open:web,url:google")
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com")));
            }
        });

        if (BuildConfig.DEBUG) {
            FirebaseAnalytics.getInstance(this).setUserProperty("debug_build", "true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRemoteConfig.activateFetched();
    }

    @FireEventLog(event = FirebaseAnalytics.Event.VIEW_ITEM, parameter = "open:web,url:google")
    @FireUserProperty(customProperty = ButtonColorCustomValueCreator.class)
    private void applyButtonColor() {
        final String color = getButtonColor();
        button.setBackgroundColor(Color.parseColor(color));
    }

    public String getButtonColor() {
        return firebaseRemoteConfig.getString(BUTTON_COLOR_KEY);
    }

    private void initRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        long cacheExpiration = 3600; // 1 hour in seconds.

        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        firebaseRemoteConfig.fetch(cacheExpiration);
    }
}
