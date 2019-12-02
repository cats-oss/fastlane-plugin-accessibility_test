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

package com.google.android.apps.common.testing.accessibility.framework;

import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * Base class to check the accessibility of {@code ViewHierarchyElement}s.
 */
public abstract class AccessibilityHierarchyCheck extends AccessibilityCheck {

    // Template for the URL of a Google Accessibility Help article, with a placeholder for a topic ID.
    private static final String ANDROID_A11Y_HELP_URL =
            "https://support.google.com/accessibility/android/answer/%s";

    /**
     * Returns the identifier of a Google Accessibility Help article related to the issues identified
     * by this check, or {@code null} if there is not yet an article on these issues.
     */
    protected abstract @Nullable
    String getHelpTopic();

    /**
     * Returns the string representation of a URL for a Google Accessibility Help article related to
     * the issues identified by this check, or {@code null} if there is not yet an article on these
     * issues.
     */
    public @Nullable String getHelpUrl() {
        String topic = getHelpTopic();
        return (topic == null) ? null : String.format(ANDROID_A11Y_HELP_URL, getHelpTopic());
    }

    /** Indicates the category to which the check belongs. */
    public abstract Category getCategory();

    /**
     * Generates a static human-readable title message for the check, supporting standard HTML
     * formatting markup.
     *
     * @return a localized {@link CharSequence} of the check title message
     */
    public abstract String getTitleMessage(Locale locale);

    /**
     * Generates a human-readable message describing a given result, supporting standard HTML
     * formatting markup.
     *
     * <p>NOTE: This method should be implemented such that a message can be generated for any result
     * that may be emitted as a {@link AccessibilityHierarchyCheckResult} from the check's {@link
     * #runCheckOnHierarchy} implementation.
     *
     * @param locale desired locale for the message
     * @param result the {@link AccessibilityHierarchyCheckResult} for which to get the message
     * @return a localized message for the result
     */
    public String getMessageForResult(Locale locale, AccessibilityHierarchyCheckResult result) {
        return getMessageForResultData(locale, result.getResultId(), result.getMetadata());
    }

    /**
     * Generates a human-readable message describing a given result id and any associated metadata,
     * supporting standard HTML formatting markup.
     *
     * <p>NOTE: This method should be implemented such that a message can be generated for any result
     * data that may be emitted as a {@link AccessibilityHierarchyCheckResult} from the check's {@link
     * #runCheckOnHierarchy} implementation.
     *
     * @param locale desired locale for the message
     * @param resultId the result ID with which an {@link AccessibilityHierarchyCheckResult} was
     *     created
     * @param metadata the {@link Metadata} with which an {@link AccessibilityHierarchyCheckResult}
     *     was created
     * @return a localized message for the result data
     */
    public abstract String getMessageForResultData(
            Locale locale, int resultId, @Nullable ResultMetadata metadata);

    /**
     * Generates a concise human-readable message describing a given result, supporting standard HTML
     * formatting markup.
     *
     * <p>NOTE: This method should be implemented such that a message can be generated for any result
     * that may be emitted as a {@link AccessibilityHierarchyCheckResult} from the check's {@link
     * #runCheckOnHierarchy} implementation.
     *
     * @param locale desired locale for the message
     * @param result the {@link AccessibilityHierarchyCheckResult} for which to get the abbreviated
     *     message
     * @return a localized {@link CharSequence} of the abbreviated message for the result
     */
    public String getShortMessageForResult(Locale locale, AccessibilityHierarchyCheckResult result) {
        return getShortMessageForResultData(locale, result.getResultId(), result.getMetadata());
    }

    /**
     * Generates a concise human-readable message describing a given result id and any associated
     * metadata, supporting standard HTML formatting markup.
     *
     * <p>NOTE: This method should be implemented such that a message can be generated for any result
     * data that may be emitted as a {@link AccessibilityHierarchyCheckResult} from the check's {@link
     * #runCheckOnHierarchy} implementation.
     *
     * @param locale desired locale for the message
     * @param resultId the result ID with which an {@link AccessibilityHierarchyCheckResult} was
     *     created
     * @param metadata the {@link Metadata} with which an {@link AccessibilityHierarchyCheckResult}
     *     was created
     * @return a localized {@link CharSequence} of the abbreviated message for the result datq
     */
    public abstract String getShortMessageForResultData(
            Locale locale, int resultId, @Nullable ResultMetadata metadata);

    /**
     * Run the check on a hierarchy of ViewHierarchyElements. Because these elements support partial
     * initialization, they any of their getters may return null. Checks extending this class must
     * therefore be robust to that uncertainty.
     * <p>
     * Any extending class must define constant result IDs which correspond to a distinct
     * result emitted from that class. These IDs are permanent and cannot be removed or changed to
     * correspond to a semantically different result.
     * <p>
     * Similarly, each class may define constant metadata keys which correspond to a distinct
     * piece of result metadata stored by this class. These IDs are permanent and cannot be removed
     * or changed to correspond to semantically different metadata or a different type of metadata.
     *
     * @param hierarchy The hierarchy to be checked.
     * @param fromRoot An optional {@link ViewHierarchyElement} root from which the {@code hierarchy}
     *        should be evaluated.  All elements within {@code hierarchy} will be available to the
     *        check, but only elements within {@code fromRoot}'s subtree will be explicitly evaluated.
     *        If {@code null}, checks will evaluate all elements within {@code hierarchy}'s active
     *        window.
     * @param metadata An optional {@link Metadata} that may contain check metadata defined by
     *        @link AccessibilityCheckMetadata.
     * @return A list of interesting results encountered while running the check. The list will be
     *         empty if the check passes without incident.
     */
    public abstract List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
            AccessibilityHierarchy hierarchy,
            @Nullable ViewHierarchyElement fromRoot,
            @Nullable Metadata metadata);

    /**
     * @see AccessibilityHierarchyCheck#runCheckOnHierarchy(AccessibilityHierarchy,
     *      ViewHierarchyElement, Metadata)
     */
    public List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
            AccessibilityHierarchy hierarchy) {
        return runCheckOnHierarchy(hierarchy, null);
    }

    /**
     * @see AccessibilityHierarchyCheck#runCheckOnHierarchy(AccessibilityHierarchy,
     *      ViewHierarchyElement, Metadata)
     */
    public List<AccessibilityHierarchyCheckResult> runCheckOnHierarchy(
            AccessibilityHierarchy hierarchy, @Nullable ViewHierarchyElement fromRoot) {
        return runCheckOnHierarchy(hierarchy, fromRoot, null);
    }

    /**
     * Determines the {@link List} of {@link ViewHierarchyElement}s that should be evaluated based on
     * arguments provided to
     * {@link AccessibilityHierarchyCheck#runCheckOnHierarchy(AccessibilityHierarchy,
     * ViewHierarchyElement, Metadata)}  If {@code fromRoot} is {@code null}, all element's within
     * {@code hierarchy}'s active window are returned.
     *
     * @param fromRoot the element from which evaluation should occur, or {@ocde null} if no such
     *        element was provided
     * @param hierarchy the non-{@ocde null} {@link AccessibilityHierarchy} under evaluation
     * @return a {@Link List} of {@link ViewHierarchyElement}s that should be evaluated, in
     *         depth-first ordering
     */
    protected static List<ViewHierarchyElement> getElementsToEvaluate(
            @Nullable ViewHierarchyElement fromRoot, AccessibilityHierarchy hierarchy) {
        return (fromRoot != null) ? fromRoot.getSelfAndAllDescendants()
                : hierarchy.getActiveWindow().getAllViews();
    }
}
