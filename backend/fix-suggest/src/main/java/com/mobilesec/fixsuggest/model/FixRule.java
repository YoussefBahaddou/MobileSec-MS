package com.mobilesec.fixsuggest.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixRule {
    private String id; // "ANDROID_DEBUGGABLE"
    private String description; // "Application is marked as debuggable"
    private String fixCode; // "android:debuggable=\"false\""
    private String explanation; // "Debuggable apps can be attached to by a debugger..."
}
