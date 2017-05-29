# Fire Annotation
Simple tool which help you to implement Firebase Analytics logging

# How to use

## Sending event log

You can use `@FireEventLog` Annotation.

```java
button.setOnClickListener(new View.OnClickListener() {
    @FireEventLog(event = FirebaseAnalytics.Event.SELECT_CONTENT, parameter = "open:web,url:google")
    @Override
    public void onClick(View v) {
        // Your code
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com")));
    }
});
```

This is the same as below.

```java
button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // Your code
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com")));

        // **genereted code**
        final FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(MainActivity.this);
        final Bundle bundle = new Bundle();
        bundle.putString("open", "web");
        bundle.putString("url", "google");
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        // **genereted code**
    }
});
```

## Sending user property

You can use `@FireUserProperty` Annotation.

```java
@FireUserProperty(property = "vending_user:yes")
private void buy() {
     // Your code
}
```

# Advanced usage

## Using method parameter value.

You can use format by method parameter for sending property or parameter.   
You can use this feature not only @FireUserProperty, but also @FireEventLog.

```java
@FireUserProperty(property = "is_activated:%s")
private void applyButtonColor(boolean isActivated) {
    final String color = getButtonColor();
    button.setBackgroundColor(Color.parseColor(color));
}
```

## Custom value

You can use `customProperty` for `@FireUserProperty`. And `customParameter` for `FireEventLog`

```java
@FireUserProperty(customProperty = ButtonColorCustomValueCreator.class)
private void applyButtonColor() {
    final String color = getButtonColor();
    button.setBackgroundColor(Color.parseColor(color));
}

public String getButtonColor() {
    String color;
    // generete color like "#FF0000"
    return color;
}
```

Please create ValueCreator class.
You can separate logging implementation.

```java
import com.github.takahirom.fireannotation.CustomValueCreator;

import java.util.HashMap;
import java.util.Map;

class ButtonColorCustomValueCreator extends CustomValueCreator<MainActivity> {
    @Override
    public Map<String, String> getValue(final MainActivity activity) {
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("button_color", activity.getButtonColor().replace("#", ""));
        return hashMap;
    }
}
```




# Download

Please add classpath to your project level build gradle.(`project/build.gradle`)



```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.2.0'
        classpath 'com.google.gms:google-services:3.0.0'

        classpath 'com.github.takahirom.fireannotation.plugin:fireannotation-plugin:0.2.1'
    }
}
```

Please add plugin to your module level build gradle.(`project/app/build.gradle`)

```
apply plugin: 'com.android.application'
apply plugin: 'com.github.takahirom.fireannotation'
```


# License

```
Copyright (C) 2017 takahirom

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
