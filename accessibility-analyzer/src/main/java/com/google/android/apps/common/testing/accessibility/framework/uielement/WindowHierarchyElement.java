/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.common.testing.accessibility.framework.uielement;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewHierarchyElementProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.WindowHierarchyElementProto;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Representation of a @link Window hierarchy for accessibility checking
 * <p>
 * These windows hold references to surrounding {@link WindowHierarchyElement}s in its local window
 * hierarchy, the window's root {@link ViewHierarchyElement}, and the containing
 * {@link AccessibilityHierarchy}. An individual window may be uniquely identified in the context of
 * an {@link AccessibilityHierarchy} by the {@code id} value returned by {@link #getId()}.
 */
public class WindowHierarchyElement {
    public static final int WINDOW_TYPE_APPLICATION = 1;
    public static final int WINDOW_TYPE_INPUT_METHOD = 2;
    public static final int WINDOW_TYPE_SYSTEM = 3;
    public static final int WINDOW_TYPE_ACCESSIBILITY = 4;

    /* The id of each view corresponds to its position in this list */
    private final List<ViewHierarchyElement> viewHierarchyElements;

    private final int id;
    private final @Nullable Integer parentId;
    private final List<Integer> childIds = new ArrayList<>();

    // This field is set to a non-null value after construction.
    private @MonotonicNonNull AccessibilityHierarchy accessibilityHierarchy;

    private final @Nullable Integer windowId;
    private final @Nullable Integer layer;
    private final @Nullable Integer type;
    private final @Nullable Boolean focused;
    private final @Nullable Boolean accessibilityFocused;
    private final @Nullable Boolean active;
    private final @Nullable Rect boundsInScreen;

    private WindowHierarchyElement(WindowHierarchyElementProto proto) {
        // Bookkeeping
        this.id = proto.getId();
        this.parentId = (proto.getParentId() != -1) ? proto.getParentId() : null;
        this.childIds.addAll(proto.getChildIdsList());

        // Window properties
        this.windowId = proto.hasWindowId() ? proto.getWindowId() : null;
        this.layer = proto.hasLayer() ? proto.getLayer() : null;
        this.type = proto.hasType() ? proto.getType() : null;
        this.focused = proto.hasFocused() ? proto.getFocused() : null;
        this.accessibilityFocused =
                proto.hasAccessibilityFocused() ? proto.getAccessibilityFocused() : null;
        this.active = proto.hasActive() ? proto.getActive() : null;
        this.boundsInScreen = proto.hasBoundsInScreen() ? new Rect(proto.getBoundsInScreen()) : null;

        // Window contents
        int totalNodes = proto.getViewsCount();
        this.viewHierarchyElements = new ArrayList<>(totalNodes);
        for (ViewHierarchyElementProto view : proto.getViewsList()) {
            viewHierarchyElements.add(new ViewHierarchyElement(view));
        }
    }

    /**
     * @return The id uniquely identifying this window within the context of its containing
     *         {@link AccessibilityHierarchy}
     */
    public int getId() {
        return id;
    }

    /**
     * @return The root {@link ViewHierarchyElement} of this window, or {@code null} if the window
     *     does not have a root view.
     */
    public @Nullable ViewHierarchyElement getRootView() {
        if (viewHierarchyElements.isEmpty()) {
            return null;
        }
        return viewHierarchyElements.get(0);
    }

    /**
     * Get all {@code ViewHierarchyElement}s in the window
     *
     * @return an unmodifiable {@link List} containing all {@link ViewHierarchyElement}s in this
     *         window, in depth-first ordering.
     */
    public List<ViewHierarchyElement> getAllViews() {
        return Collections.unmodifiableList(viewHierarchyElements);
    }

    /**
     * @return The parent {@link WindowHierarchyElement} of this window, or {@code null} if this
     *     window is a root window.
     */
    public @Nullable WindowHierarchyElement getParentWindow() {
        return (parentId != null) ? getAccessibilityHierarchy().getWindowById(parentId) : null;
    }

    /**
     * @return The number of child {@link WindowHierarchyElement}s rooted at this window
     */
    public int getChildWindowCount() {
        return childIds.size();
    }

    /**
     * @param atIndex The index of the child {@link WindowHierarchyElement} to obtain.
     * @return The requested child window
     * @throws NoSuchElementException if {@code atIndex} is less than 0 or greater than
     *         {@code getChildWindowCount() - 1}
     */
    public WindowHierarchyElement getChildWindow(int atIndex) {
        if (atIndex < 0 || atIndex >= childIds.size()) {
            throw new NoSuchElementException();
        }
        return getAccessibilityHierarchy().getWindowById(childIds.get(atIndex));
    }

    /**
     * @param id The identifier for the desired {@link ViewHierarchyElement}, as returned by
     *        {@link ViewHierarchyElement#getId()}.
     * @return The {@link ViewHierarchyElement} identified by {@code id} in this window
     * @throws NoSuchElementException if no view within this window matches the provided {@code id}
     */
    public ViewHierarchyElement getViewById(int id) {
        if ((id < 0) || (id >= viewHierarchyElements.size())) {
            throw new NoSuchElementException();
        }
        return viewHierarchyElements.get(id);
    }

    /**
     * @return The containing {@link AccessibilityHierarchy} of this window.
     */
    public AccessibilityHierarchy getAccessibilityHierarchy() {

        // The type is explicit because the @MonotonicNonNull field is not read as @Nullable.
        return Preconditions.<@Nullable AccessibilityHierarchy>checkNotNull(accessibilityHierarchy);
    }

    /**
     * @return The system-defined window identifier for this window, or {@code null} if this cannot be
     *     determined
     */
    public @Nullable Integer getWindowId() {
        return windowId;
    }

    /**
     * @return The layer value indicating z-ordering of this window, or {@code null} if this cannot be
     *     determined
     */
    public @Nullable Integer getLayer() {
        return layer;
    }

    /**
     * @return One of {@link #WINDOW_TYPE_APPLICATION}, {@link #WINDOW_TYPE_INPUT_METHOD}, {@link
     *     #WINDOW_TYPE_SYSTEM}, {@link #WINDOW_TYPE_ACCESSIBILITY}, or {@code null} if this cannot be
     *     determined
     */
    public @Nullable Integer getType() {
        return type;
    }

    /**
     * @return {@link Boolean#TRUE} if this window contains system input focus, {@link Boolean#FALSE}
     *     if not, or {@code null} if this cannot be determined
     */
    public @Nullable Boolean isFocused() {
        return focused;
    }

    /**
     * @return {@link Boolean#TRUE} if this window contains system accessibility focus, {@link
     *     Boolean#FALSE} if not, or {@code null} if this cannot be determined
     */
    public @Nullable Boolean isAccessibilityFocused() {
        return accessibilityFocused;
    }

    /**
     * @return {@link Boolean#TRUE} if this is considered the active window, {@link Boolean#FALSE} if
     *     not, or {@code null} if this cannot be determined
     */
    public @Nullable Boolean isActive() {
        return active;
    }

    /**
     * Retrieves the bounds of this window in absolute screen coordinates.
     *
     * @return the window's bounds, or {@link Rect#EMPTY} if the window's bounds are unavailable, such
     * as when it is positioned off-screen.
     */
    public Rect getBoundsInScreen() {
        return (boundsInScreen != null) ? boundsInScreen : Rect.EMPTY;
    }

    /** Set the containing {@link AccessibilityHierarchy} of this window. */
    void setAccessibilityHierarchy(AccessibilityHierarchy accessibilityHierarchy) {
        this.accessibilityHierarchy = accessibilityHierarchy;
    }

    WindowHierarchyElementProto toProto() {
        WindowHierarchyElementProto.Builder builder = WindowHierarchyElementProto.newBuilder();
        // Bookkeeping
        builder.setId(id);
        if (parentId != null) {
            builder.setParentId(parentId);
        }
        if (!childIds.isEmpty()) {
            builder.addAllChildIds(childIds);
        }

        // Window properties
        if (windowId != null) {
            builder.setWindowId(windowId);
        }
        if (layer != null) {
            builder.setLayer(layer);
        }
        if (type != null) {
            builder.setType(type);
        }
        if (focused != null) {
            builder.setFocused(focused);
        }
        if (accessibilityFocused != null) {
            builder.setFocused(accessibilityFocused);
        }
        if (active != null) {
            builder.setActive(active);
        }
        if (boundsInScreen != null) {
            builder.setBoundsInScreen(boundsInScreen.toProto());
        }

        // Window contents
        for (ViewHierarchyElement view : viewHierarchyElements) {
            builder.addViews(view.toProto());
        }
        return builder.build();
    }

    /**
     * @param child The child {@link WindowHierarchyElement} to add as a child of this window
     */
    void addChild(WindowHierarchyElement child) {
        childIds.add(child.id);
    }

    /** Returns a new builder that can build a WindowHierarchyElement from a proto. */
    static Builder newBuilder(WindowHierarchyElementProto proto) {
        int id = proto.getId();
        Builder builder = new Builder(id);
        builder.proto = checkNotNull(proto);
        return builder;
    }

    /**
     * A builder for {@link WindowHierarchyElement}; obtained using @link
     * WindowHierarchyElement#builder.
     */
    public static class Builder {
        private final int id;
        private @Nullable WindowHierarchyElementProto proto;
        private @Nullable WindowHierarchyElement parent;

        Builder(int id) {
            this.id = id;
        }

        public Builder setParent(@Nullable WindowHierarchyElement parent) {
            this.parent = parent;
            return this;
        }

        public WindowHierarchyElement build() {
            WindowHierarchyElement result;

            if (proto != null) {
                result = new WindowHierarchyElement(proto);
            } else {
                throw new IllegalStateException("Nothing from which to build");
            }

            // Add entries to the origin maps after pointers to the window have been set.
            // The condensed unique IDs cannot be obtained without the window.
            setWindow(result);
            return result;
        }

        /** Set backpointers from the window's views to the window. */
        private void setWindow(WindowHierarchyElement window) {
            if (window.viewHierarchyElements != null) {
                for (ViewHierarchyElement view : window.viewHierarchyElements) {
                    view.setWindow(window);
                }
            }
        }
    }
}
