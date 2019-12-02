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
import static com.google.common.base.Preconditions.checkState;

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.AccessibilityHierarchyProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.WindowHierarchyElementProto;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

/**
 * Representation of a UI hierarchy for accessibility checking
 * <p>
 * Such a hierarchy may contain a forest of {@link WindowHierarchyElement}s, each of which contain a
 * tree of {@link ViewHierarchyElement}s.
 */
public class AccessibilityHierarchy {

    /* A representation of the device's state at the time a hierarchy is initially captured */
    private final DeviceState deviceState;

    /* The id of each window corresponds to its position in this list */
    private final List<WindowHierarchyElement> windowHierarchyElements;

    /* A reference to the 'active' window. Exactly one such window exists in any hierarchy. */
    private final WindowHierarchyElement activeWindow;

    private AccessibilityHierarchy(
            DeviceState deviceState,
            List<WindowHierarchyElement> windowHierarchyElements,
            WindowHierarchyElement activeWindow) {
        this.deviceState = deviceState;
        this.windowHierarchyElements = windowHierarchyElements;
        this.activeWindow = activeWindow;
    }

    /**
     * @return the {@link DeviceState} representing certain properties of the device at the time an
     *         {@link AccessibilityHierarchy} was originally captured.
     */
    public DeviceState getDeviceState() {
        return deviceState;
    }

    /**
     * Get all {@link WindowHierarchyElement}s in this hierarchy.
     *
     * @return An unmodifiable collection of all windows in hierarchy
     */
    public Collection<WindowHierarchyElement> getAllWindows() {
        return Collections.unmodifiableCollection(windowHierarchyElements);
    }

    /**
     * @return a {@link WindowHierarchyElement} representing the active window in this hierarchy. If
     *         this hierarchy was constructed from a @link AccessibilityNodeInfo or @link View,
     *         this returns the default {@link WindowHierarchyElement} that was implicitly created to
     *         hold the hierarchy.
     */
    public WindowHierarchyElement getActiveWindow() {
        return activeWindow;
    }

    /**
     * @param id The identifier for the desired {@link WindowHierarchyElement}, as returned by
     *        {@link WindowHierarchyElement#getId()}.
     * @return The {@link WindowHierarchyElement} identified by {@code id} in this hierarchy
     * @throws NoSuchElementException if no window within this hierarchy matches the provided
     *         {@code id}
     */
    public WindowHierarchyElement getWindowById(int id) {
        if ((id < 0) || (id >= windowHierarchyElements.size())) {
            throw new NoSuchElementException();
        }
        return windowHierarchyElements.get(id);
    }

    /**
     * @param condensedUniqueId The identifier for the desired {@link ViewHierarchyElement}, as
     *        returned by {@link ViewHierarchyElement#getCondensedUniqueId()}
     * @return The {@link ViewHierarchyElement} identified by {@code id} in this hierarchy
     * @throws NoSuchElementException if no view within this hierarchy matches the provided
     *         {@code condensedUniqueId}
     */
    public ViewHierarchyElement getViewById(long condensedUniqueId) {
        int windowId = (int) (condensedUniqueId >>> 32);
        int viewId = (int) condensedUniqueId;
        return getWindowById(windowId).getViewById(viewId);
    }

    /**
     * @return an {@link AccessibilityHierarchyProto} protocol buffer representation of this hierarchy
     */
    public AccessibilityHierarchyProto toProto() {
        AccessibilityHierarchyProto.Builder builder = AccessibilityHierarchyProto.newBuilder();
        builder.setDeviceState(deviceState.toProto());
        builder.setActiveWindowId(activeWindow.getId());
        for (WindowHierarchyElement window : windowHierarchyElements) {
            builder.addWindows(window.toProto());
        }

        return builder.build();
    }

    /** Set backpointers from the windows to the accessibility hierarchy. */
    private void setAccessibilityHierarchy() {
        for (WindowHierarchyElement window : this.windowHierarchyElements) {
            window.setAccessibilityHierarchy(this);
        }
    }

    /**
     * Returns a new builder that can build an AccessibilityHierarchy from a proto.
     *
     * @param proto A protocol buffer representation of a hierarchy
     */
    public static Builder newBuilder(AccessibilityHierarchyProto proto) {
        Builder builder = new Builder();
        builder.proto = checkNotNull(proto);
        return builder;
    }

    /**
     * A builder for {@link AccessibilityHierarchy}; obtained using @link
     * AccessibilityHierarchy#builder.
     */
    public static class Builder {
        private @Nullable
        AccessibilityHierarchyProto proto;
        private boolean disposeInstances = false;

        public AccessibilityHierarchy build() {
            AccessibilityHierarchy result;
            if (proto != null) {
                result = buildHierarchyFromProto(proto);
            } else {
                throw new IllegalStateException("Nothing from which to build");
            }
            return result;
        }

        private AccessibilityHierarchy buildHierarchyFromProto(AccessibilityHierarchyProto proto) {
            DeviceState deviceState = new DeviceState(proto.getDeviceState());
            int activeWindowId = proto.getActiveWindowId();

            List<WindowHierarchyElement> windowHierarchyElements =
                    new ArrayList<>(proto.getWindowsCount());
            for (WindowHierarchyElementProto windowProto : proto.getWindowsList()) {
                windowHierarchyElements.add(WindowHierarchyElement.newBuilder(windowProto).build());
            }
            checkState(
                    !windowHierarchyElements.isEmpty(),
                    "Hierarchies must contain at least one window.");
            WindowHierarchyElement activeWindow = windowHierarchyElements.get(activeWindowId);

            AccessibilityHierarchy hierarchy =
                    new AccessibilityHierarchy(deviceState, windowHierarchyElements, activeWindow);
            hierarchy.setAccessibilityHierarchy();
            return hierarchy;
        }
    }
}
