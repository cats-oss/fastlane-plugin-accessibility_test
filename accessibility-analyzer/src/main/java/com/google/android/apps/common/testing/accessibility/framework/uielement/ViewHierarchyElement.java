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

import com.google.android.apps.common.testing.accessibility.framework.replacements.Rect;
import com.google.android.apps.common.testing.accessibility.framework.replacements.SpannableString;
import com.google.android.apps.common.testing.accessibility.framework.replacements.TextUtils;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.ViewHierarchyElementProto;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Representation of a link View hierarchy for accessibility checking
 * <p>
 * These views hold references to surrounding {@link ViewHierarchyElement}s in its local view
 * hierarchy and the containing {@link WindowHierarchyElement}. An individual view may be uniquely
 * identified in the context of its containing {@link WindowHierarchyElement} by the {@code id}
 * value returned by {@link #getId()}, or it may be uniquely identified in the context of its
 * containing {@link AccessibilityHierarchy} by the {@code long} returned by
 * {@link #getCondensedUniqueId()}.
 */
public class ViewHierarchyElement {
    private final int id;
    private final @Nullable Integer parentId;

    // Created lazily, because many views are leafs.
    private @MonotonicNonNull List<Integer> childIds;

    // This field is set to a non-null value after construction.
    private @MonotonicNonNull WindowHierarchyElement windowElement;

    private final @Nullable CharSequence packageName;
    private final @Nullable CharSequence className;
    private final @Nullable CharSequence accessibilityClassName;
    private final @Nullable String resourceName;
    private final @Nullable SpannableString contentDescription;
    private final @Nullable SpannableString text;
    private final boolean importantForAccessibility;
    private final @Nullable Boolean visibleToUser;
    private final boolean clickable;
    private final boolean longClickable;
    private final boolean focusable;
    private final @Nullable Boolean editable;
    private final @Nullable Boolean scrollable;
    private final @Nullable Boolean canScrollForward;
    private final @Nullable Boolean canScrollBackward;
    private final @Nullable Boolean checkable;
    private final @Nullable Boolean checked;
    private final @Nullable Boolean hasTouchDelegate;
    private final @Nullable Rect boundsInScreen;
    private final @Nullable Integer nonclippedHeight;
    private final @Nullable Integer nonclippedWidth;
    private final @Nullable Float textSize;
    private final @Nullable Integer textColor;
    private final @Nullable Integer backgroundDrawableColor;
    private final @Nullable Integer typefaceStyle;
    private final boolean enabled;

    // Populated only after a hierarchy is constructed
    private @Nullable Long labeledById;
    private @Nullable Long accessibilityTraversalBeforeId;
    private @Nullable Long accessibilityTraversalAfterId;

    ViewHierarchyElement(ViewHierarchyElementProto proto) {
        checkNotNull(proto);

        // Bookkeeping
        this.id = proto.getId();
        this.parentId = (proto.getParentId() != -1) ? proto.getParentId() : null;
        if (proto.getChildIdsCount() > 0) {
            this.childIds = new ArrayList<>(proto.getChildIdsCount());
            this.childIds.addAll(proto.getChildIdsList());
        }

        packageName = proto.hasPackageName() ? proto.getPackageName() : null;
        className = proto.hasClassName() ? proto.getClassName() : null;
        accessibilityClassName =
                proto.hasAccessibilityClassName() ? proto.getAccessibilityClassName() : null;
        resourceName = proto.hasResourceName() ? proto.getResourceName() : null;
        contentDescription =
                proto.hasContentDescription() ? new SpannableString(proto.getContentDescription()) : null;
        text = proto.hasText() ? new SpannableString(proto.getText()) : null;
        importantForAccessibility = proto.getImportantForAccessibility();
        visibleToUser = proto.hasVisibleToUser() ? proto.getVisibleToUser() : null;
        clickable = proto.getClickable();
        longClickable = proto.getLongClickable();
        focusable = proto.getFocusable();
        editable = proto.hasEditable() ? proto.getEditable() : null;
        scrollable = proto.hasScrollable() ? proto.getScrollable() : null;
        canScrollForward = proto.hasCanScrollForward() ? proto.getCanScrollForward() : null;
        canScrollBackward = proto.hasCanScrollBackward() ? proto.getCanScrollBackward() : null;
        checkable = proto.hasCheckable() ? proto.getCheckable() : null;
        checked = proto.hasChecked() ?  proto.getChecked() : null;
        hasTouchDelegate = proto.hasHasTouchDelegate() ? proto.getHasTouchDelegate() : null;
        this.boundsInScreen = proto.hasBoundsInScreen() ? new Rect(proto.getBoundsInScreen()) : null;
        nonclippedHeight = proto.hasNonclippedHeight() ? proto.getNonclippedHeight() : null;
        nonclippedWidth = proto.hasNonclippedWidth() ? proto.getNonclippedWidth() : null;
        textSize = proto.hasTextSize() ? proto.getTextSize() : null;
        textColor = proto.hasTextColor() ? proto.getTextColor() : null;
        backgroundDrawableColor =
                proto.hasBackgroundDrawableColor() ? proto.getBackgroundDrawableColor() : null;
        typefaceStyle = proto.hasTypefaceStyle() ? proto.getTypefaceStyle() : null;
        enabled = proto.getEnabled();
        labeledById = proto.hasLabeledById() ? proto.getLabeledById() : null;
        accessibilityTraversalBeforeId =
                proto.hasAccessibilityTraversalBeforeId()
                        ? proto.getAccessibilityTraversalBeforeId()
                        : null;
        accessibilityTraversalAfterId =
                proto.hasAccessibilityTraversalAfterId() ? proto.getAccessibilityTraversalAfterId() : null;
    }

    /**
     * @return The value uniquely identifying this window within the context of its containing
     *         {@link WindowHierarchyElement}
     */
    public int getId() {
        return id;
    }

    /**
     * @return a value uniquely representing this {@link ViewHierarchyElement} and its containing
     *         {@link WindowHierarchyElement} in the context of it's containing
     *         {@link AccessibilityHierarchy}.
     */
    public long getCondensedUniqueId() {
        return (((long) getWindow().getId() << 32) | getId());
    }

    /**
     * @return The parent {@link ViewHierarchyElement} of this view, or {@code null} if this is a root
     *     view.
     */
    @Pure
    public @Nullable ViewHierarchyElement getParentView() {
        return (parentId != null) ? getWindow().getViewById(parentId) : null;
    }

    /**
     * @return The number of child {@link ViewHierarchyElement}s rooted at this view
     */
    public int getChildViewCount() {
        return (childIds == null) ? 0 : childIds.size();
    }

    /**
     * @param atIndex The index of the child {@link ViewHierarchyElement} to obtain. Must be &ge 0 and
     *        &lt {@link #getChildViewCount()}.
     * @return The requested child, or {@code null} if no such child exists at the given
     *         {@code atIndex}
     * @throws NoSuchElementException if {@code atIndex} is less than 0 or greater than
     *         {@code getChildViewCount() - 1}
     */
    public ViewHierarchyElement getChildView(int atIndex) {
        if ((atIndex < 0) || (childIds == null) || (atIndex >= childIds.size())) {
            throw new NoSuchElementException();
        }
        return getWindow().getViewById(childIds.get(atIndex));
    }

    /**
     * @return an unmodifiable {@link List} containing this {@link ViewHierarchyElement} and any
     *         descendants, direct or indirect, in depth-first ordering.
     */
    public List<ViewHierarchyElement> getSelfAndAllDescendants() {
        List<ViewHierarchyElement> listToPopulate = new ArrayList<>();
        listToPopulate.add(this);
        for (int i = 0; i < getChildViewCount(); ++i) {
            listToPopulate.addAll(getChildView(i).getSelfAndAllDescendants());
        }

        return Collections.unmodifiableList(listToPopulate);
    }

    /**
     * @return The containing {@link WindowHierarchyElement} of this view.
     */
    public WindowHierarchyElement getWindow() {

        // The type is explicit because the @MonotonicNonNull field is not read as @Nullable.
        return Preconditions.<@Nullable WindowHierarchyElement>checkNotNull(windowElement);
    }

    /**
     * @return The package name to which this view belongs, or {@code null} if one cannot be
     *     determined
     */
    public @Nullable CharSequence getPackageName() {
        return packageName;
    }

    /**
     * @return The class name to which this view belongs, or {@code null} if one cannot be determined
     */
    public @Nullable CharSequence getClassName() {
        return className;
    }

    /**
     * @return The view id's associated resource name, or {@code null} if one cannot be determined or
     *     is not available
     */
    @Pure
    public @Nullable String getResourceName() {
        return resourceName;
    }

    /**
     * Check if the link View this element represents matches a particular class.
     *
     * @param referenceClass the class to check against the class of this element
     * @return {@link Boolean#TRUE} if the {@code View} this element represents is an instance of the
     *     class whose name is {@code referenceClass}. {@link Boolean#FALSE} if it does not. {@code
     *     null} if a determination cannot be made.
     */
    public @Nullable Boolean checkInstanceOf(Class<?> referenceClass) {
        if ((className == null) || (referenceClass == null)) {
            return null;
        }

        Class<?> targetClass = null;
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            if (classLoader != null) {
                targetClass = classLoader.loadClass(className.toString());
            }
        } catch (ClassNotFoundException e) {
            // Do nothing
        }

        if (targetClass == null) {
            return null;
        }

        return referenceClass.isAssignableFrom(targetClass);
    }

    public Boolean isListView() {
        return className == "android.widget.ListView" ||
                className == "android.widget.GridView" ||
                className == "android.support.v7.widget.RecyclerView" ||
                className == "androidx.recyclerview.widget.RecyclerView";
    }

    public Boolean isSpinner() {
        return className == "android.widget.Spinner";
    }

    public Boolean isAdapterView() {
        return isSpinner() || isListView();
    }

    public Boolean isScrollView() {
        return className == "android.widget.ScrollView";
    }

    public Boolean isHorizontalScrollView() {
        return className == "android.widget.HorizontalScrollView";
    }

    public Boolean isWebView() {
        return className != null && className.toString().contains("WebView");
    }

    public Boolean isTextView() {
        return (className != null && className.toString().contains("TextView")) ||
                className == "android.widget.Button" ||
                className == "android.widget.TextClock" ||
                className == "android.widget.Chronometer" ||
                className == "android.widget.DigitalClock" ||
                isEditText();
    }

    public Boolean isEditText() {
        return className != null && className.toString().contains("EditText");
    }

    /**
     */
    public @Nullable SpannableString getContentDescription() {
        return contentDescription;
    }

    /**
     * Indicates whether the element is important for accessibility and would be reported to
     * accessibility services.
     */
    public boolean isImportantForAccessibility() {
        return importantForAccessibility;
    }

    /**
     * @return This view's text content, or {@code null} if none is present
     */
    public @Nullable SpannableString getText() {
        return text;
    }

    /**
     * @return {@link Boolean#TRUE} if the element is visible to the user, {@link Boolean#FALSE} if
     *     not, or {@code null} if this cannot be determined.
     */
    public @Nullable Boolean isVisibleToUser() {
        return visibleToUser;
    }

    /**
     * Indicates whether this view reports that it reacts to click events or not.
     */
    public boolean isClickable() {
        return clickable;
    }

    /**
     * Indicates whether this view reports that it reacts to long click events or not.
     */
    public boolean isLongClickable() {
        return longClickable;
    }

    /**
     * Indicates whether this view reports that it is currently able to take focus.
     */
    public boolean isFocusable() {
        return focusable;
    }

    /**
     * @return {@link Boolean#TRUE} if the element is editable, {@link Boolean#FALSE} if not, or
     *     {@code null} if this cannot be determined.
     */
    public @Nullable Boolean isEditable() {
        return editable;
    }

    /**
     * @return {@link Boolean#TRUE} if the element is potentially scrollable or indicated as a
     *     scrollable container, {@link Boolean#FALSE} if not, or {@code null} if this cannot be
     *     determined. Scrollable in this context refers only to a element's potential for being
     *     scrolled, and doesn't indicate if the container holds enough wrapped content to scroll. To
     *     determine if an element is actually scrollable based on contents use {@link
     *     #canScrollForward} or {@link #canScrollBackward}.
     */
    public @Nullable Boolean isScrollable() {
        return scrollable;
    }

    /**
     * @return {@link Boolean#TRUE} if the element is scrollable in the "forward" direction, typically
     *         meaning either vertically downward or horizontally to the right (in left-to-right
     *         locales), {@link Boolean#FALSE} if not, or {@code null if this cannot be determined.
     */
    public @Nullable Boolean canScrollForward() {
        return canScrollForward;
    }

    /**
     * @return {@link Boolean#TRUE} if the element is scrollable in the "backward" direction,
     *         typically meaning either vertically downward or horizontally to the right (in
     *         left-to-right locales), {@link Boolean#FALSE} if not, or {@code null if this cannot be
     *         determined.
     */
    public @Nullable Boolean canScrollBackward() {
        return canScrollBackward;
    }

    /**
     * @return {@link Boolean#TRUE} if the element is checkable, {@link Boolean#FALSE} if not, or
     *     {@code null} if this cannot be determined.
     */
    public @Nullable Boolean isCheckable() {
        return checkable;
    }

    /**
     * @return {@link Boolean#TRUE} if the element is checked, {@link Boolean#FALSE} if not, or {@code
     *     null} if this cannot be determined.
     */
    public @Nullable Boolean isChecked() {
        return checked;
    }

    /**
     * @return {@link Boolean#TRUE} if the element has a link TouchDelegate, {@link Boolean#FALSE}
     *     if not, or {@code null} if this cannot be determined.
     */
    public @Nullable Boolean hasTouchDelegate() {
        return hasTouchDelegate;
    }

    /**
     * Retrieves the visible bounds of this element in absolute screen coordinates.
     * <p>
     * NOTE: This method provides dimensions that may be reduced in size due to clipping effects from
     * parent elements. To determine nonclipped dimensions, consider using
     * {@link #getNonclippedHeight()} and {@link #getNonclippedWidth}.
     *
     * @return the view's bounds, or {@link Rect#EMPTY} if the view's bounds are unavailable, such as
     * when it is positioned off-screen.
     */
    public Rect getBoundsInScreen() {
        return (boundsInScreen != null) ? boundsInScreen : Rect.EMPTY;
    }

    /**
     * @return the height of this element (in raw pixels) not taking into account clipping effects
     *     applied by parent elements.
     */
    public @Nullable Integer getNonclippedHeight() {
        return nonclippedHeight;
    }

    /**
     * @return the width of this element (in raw pixels) not taking into account clipping effects
     *     applied by parent elements.
     */
    public @Nullable Integer getNonclippedWidth() {
        return nonclippedWidth;
    }

    /**
     * @return The size (in raw pixels) of the default text appearing in this view, or {@code null} if
     *     this cannot be determined
     */
    public @Nullable Float getTextSize() {
        return textSize;
    }

    /**
     * @return The color of the default text appearing in this view, or {@code null} if this cannot be
     *     determined
     */
    public @Nullable Integer getTextColor() {
        return textColor;
    }

    /**
     * @return The color of this View's background drawable, or {@code null} if the view does not have
     *     a @link ColorDrawable background
     */
    public @Nullable Integer getBackgroundDrawableColor() {
        return backgroundDrawableColor;
    }

    /**
     * @return The style attributes of the @link Typeface of the default text appearing in this
     *    view, or @code null} if this cannot be determined.
     */
    @Pure
    public @Nullable Integer getTypefaceStyle() {
        return typefaceStyle;
    }

    /**
     * Returns the enabled status for this view.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return The class name as reported to accessibility services, or {@code null} if this cannot be
     *     determined
     *     <p>NOTE: Unavailable for instances originally created from a @link View
     */
    public @Nullable CharSequence getAccessibilityClassName() {
        return accessibilityClassName;
    }

    /**
     * @return the {@link ViewHierarchyElement} which acts as a label for this element, or
     *    {@code null} if this element is not labeled by another
     */
    @Pure
    public @Nullable ViewHierarchyElement getLabeledBy() {
        return getViewHierarchyElementById(labeledById);
    }

    /**
     * @return a view before which this one is visited in accessibility traversal
     */
    public @Nullable ViewHierarchyElement getAccessibilityTraversalBefore() {
        return getViewHierarchyElementById(accessibilityTraversalBeforeId);
    }

    /**
     * @return a view after which this one is visited in accessibility traversal
     */
    public @Nullable ViewHierarchyElement getAccessibilityTraversalAfter() {
        return getViewHierarchyElementById(accessibilityTraversalAfterId);
    }

    ViewHierarchyElementProto toProto() {
        ViewHierarchyElementProto.Builder builder = ViewHierarchyElementProto.newBuilder();
        // Bookkeeping
        builder.setId(id);
        if (parentId != null) {
            builder.setParentId(parentId);
        }
        if ((childIds != null) && !childIds.isEmpty()) {
            builder.addAllChildIds(childIds);
        }

        // View properties
        if (!TextUtils.isEmpty(packageName)) {
            builder.setPackageName(packageName.toString());
        }
        if (!TextUtils.isEmpty(className)) {
            builder.setClassName(className.toString());
        }
        if (!TextUtils.isEmpty(resourceName)) {
            builder.setResourceName(resourceName);
        }
        if (!TextUtils.isEmpty(contentDescription)) {
            builder.setContentDescription(contentDescription.toProto());
        }
        if (!TextUtils.isEmpty(text)) {
            builder.setText(text.toProto());
        }
        builder.setImportantForAccessibility(importantForAccessibility);
        if (visibleToUser != null) {
            builder.setVisibleToUser(visibleToUser);
        }
        builder.setClickable(clickable);
        builder.setLongClickable(longClickable);
        builder.setFocusable(focusable);
        if (editable != null) {
            builder.setEditable(editable);
        }
        if (scrollable != null) {
            builder.setScrollable(scrollable);
        }
        if (canScrollForward != null) {
            builder.setCanScrollForward(canScrollForward);
        }
        if (canScrollBackward != null) {
            builder.setCanScrollBackward(canScrollBackward);
        }
        if (checkable != null) {
            builder.setCheckable(checkable);
        }
        if (checked != null) {
            builder.setChecked(checked);
        }
        if (hasTouchDelegate != null) {
            builder.setHasTouchDelegate(hasTouchDelegate);
        }
        if (boundsInScreen != null) {
            builder.setBoundsInScreen(boundsInScreen.toProto());
        }
        if (nonclippedHeight != null) {
            builder.setNonclippedHeight(nonclippedHeight);
        }
        if (nonclippedWidth != null) {
            builder.setNonclippedWidth(nonclippedWidth);
        }
        if (textSize != null) {
            builder.setTextSize(textSize);
        }
        if (textColor != null) {
            builder.setTextColor(textColor);
        }
        if (backgroundDrawableColor != null) {
            builder.setBackgroundDrawableColor(backgroundDrawableColor);
        }
        if (typefaceStyle != null) {
            builder.setTypefaceStyle(typefaceStyle);
        }
        builder.setEnabled(enabled);
        if (labeledById != null) {
            builder.setLabeledById(labeledById);
        }
        if (accessibilityClassName != null) {
            builder.setAccessibilityClassName(accessibilityClassName.toString());
        }
        if (accessibilityTraversalBeforeId != null) {
            builder.setAccessibilityTraversalBeforeId(accessibilityTraversalBeforeId);
        }
        if (accessibilityTraversalAfterId != null) {
            builder.setAccessibilityTraversalAfterId(accessibilityTraversalAfterId);
        }
        return builder.build();
    }

    /** Set the containing {@link WindowHierarchyElement} of this view. */
    void setWindow(WindowHierarchyElement window) {
        this.windowElement = window;
    }

    /**
     * @param child The child {@link ViewHierarchyElement} to add as a child of this view
     */
    void addChild(ViewHierarchyElement child) {
        if (childIds == null) {
            childIds = new ArrayList<>();
        }
        childIds.add(child.id);
    }

    /**
     * Denotes that {@code labelingElement} acts as a label for this element
     *
     * @param labelingElement The element that labels this element, or {@code null} if this element is
     *        not labeled by another
     */
    void setLabeledBy(ViewHierarchyElement labelingElement) {
        labeledById = (labelingElement != null) ? labelingElement.getCondensedUniqueId() : null;
    }

    /**
     * Sets a view before which this one is visited in accessibility traversal. A screen-reader must
     * visit the content of this view before the content of the one it precedes.
     */
    void setAccessibilityTraversalBefore(ViewHierarchyElement element) {
        accessibilityTraversalBeforeId = element.getCondensedUniqueId();
    }

    /**
     * Sets a view after which this one is visited in accessibility traversal. A screen-reader must
     * visit the content of the other view before the content of this one.
     */
    void setAccessibilityTraversalAfter(ViewHierarchyElement element) {
        accessibilityTraversalAfterId = element.getCondensedUniqueId();
    }

    private @Nullable ViewHierarchyElement getViewHierarchyElementById(@Nullable Long id) {
        return (id != null) ? getWindow().getAccessibilityHierarchy().getViewById(id) : null;
    }
}
