/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.android.apps.common.testing.accessibility.framework.uielement;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DeviceStateProto;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Locale;

/**
 * Representation of the state of a device at the time an {@link AccessibilityHierarchy} is
 * captured.
 * <p>
 * Display properties, such as screen resolution and pixel density, are stored within
 * {@link DisplayInfo} and as fields in the associated {@link DeviceStateProto}.
 */
public class DeviceState {

    private static final Splitter HYPHEN_SPLITTER = Splitter.on('-');

    private final DisplayInfo defaultDisplayInfo;

    private final int sdkVersion;

    private final Locale locale;

    DeviceState(DeviceStateProto fromProto) {
        sdkVersion = fromProto.getSdkVersion();
        defaultDisplayInfo = new DisplayInfo(fromProto.getDefaultDisplayInfo());
        String languageTag = fromProto.getLocale();
        // Use the default Locale if no locale was recorded in the proto.
        // This is for backward compatibility.
        locale = languageTag.isEmpty() ? Locale.getDefault() : getLocaleFromLanguageTag(languageTag);
    }

    /**
     * WindowManager#getDefaultDisplay()
     */
    public DisplayInfo getDefaultDisplayInfo() {
        return defaultDisplayInfo;
    }

    /** Build.VERSION#SDK_INT */
    public int getSdkVersion() {
        return sdkVersion;
    }

    /** Gets the locale at the time the device state was captured. */
    public Locale getLocale() {
        return locale;
    }

    DeviceStateProto toProto() {
        DeviceStateProto.Builder builder = DeviceStateProto.newBuilder();
        builder.setSdkVersion(sdkVersion);
        builder.setDefaultDisplayInfo(defaultDisplayInfo.toProto());
        builder.setLocale(getLanguageTag());
        return builder.build();
    }

    private String getLanguageTag() {
        return locale.toLanguageTag();
    }

    private static Locale getLocaleFromLanguageTag(String languageTag) {
        return Locale.forLanguageTag(languageTag);
    }

    /**
     * Attempts to produce the same result as {@link Locale#toLanguageTag} for those locales
     * supported by ATF.
     * <p>For use with builds prior to LOLLIPOP, where toLanguageTag is not available.
     */
    @VisibleForTesting
    static String getStringFromLocale(Locale locale) {
        return locale.toString().replace('_', '-');
    }

    /**
     * Attempts to produce the same result as {@link Locale#forLanguageTag} for those locales
     * supported by ATF.
     * <p>For use with builds prior to LOLLIPOP, where forLanguageTag is not available.
     */
    @VisibleForTesting
    static Locale getLocaleFromString(String str) {
        List<String> parts = HYPHEN_SPLITTER.splitToList(str);
        switch (parts.size()) {
            case 1:
                return new Locale(parts.get(0));
            case 2:
                return new Locale(parts.get(0), parts.get(1));
            case 3:
                return new Locale(parts.get(0), parts.get(1), parts.get(2));
            default:
                throw new IllegalArgumentException("Unsupported locale string: " + str);
        }
    }
}
