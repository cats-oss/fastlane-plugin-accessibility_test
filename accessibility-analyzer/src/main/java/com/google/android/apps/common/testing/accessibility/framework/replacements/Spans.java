package com.google.android.apps.common.testing.accessibility.framework.replacements;

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.SpanProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AndroidFrameworkProtos.SpanProto.SpanType;

import javax.annotation.Nullable;

/**
 * Types of spans that are known and usable within {@link
 * com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck}
 * implementations.
 */
public final class Spans {

    /**
     * Used as a local replacement for Android's @link android.text.style.ClickableSpan
     *
     * <p>NOTE: Android's ClickableSpan is abstract, but this class is only used to track the presence
     * of this type of span within a CharSequence.
     */
    public static class ClickableSpan extends Span {

        public static final String ANDROID_CLASS_NAME = "android.text.style.ClickableSpan";

        public ClickableSpan(String spanClass, int start, int end, int flags) {
            // No state -- marker class only
            super(spanClass, start, end, flags);
        }

        public ClickableSpan(SpanProto proto) {
            super(proto);
        }

        @Override
        public SpanProto toProto() {
            // Explicitly not invoking super to ensure the SpanType is properly set
            SpanProto.Builder builder = SpanProto.newBuilder();
            builder.setSpanClassName(getSpanClassName());
            builder.setStart(getStart());
            builder.setEnd(getEnd());
            builder.setFlags(getFlags());
            builder.setType(SpanType.CLICKABLE);

            return builder.build();
        }

        @Override
        protected Span copyWithAdjustedPosition(int newStart, int newEnd) {
            return new ClickableSpan(getSpanClassName(), newStart, newEnd, getFlags());
        }
    }

    /**
     * Used as a local replacement for Android's @link android.text.style.URLSpan
     */
    public static class URLSpan extends ClickableSpan implements Replaceable {

        public static final String ANDROID_CLASS_NAME = "android.text.style.URLSpan";

        private final @Nullable
        String url;

        public URLSpan(String spanClass, int start, int end, int flags, @Nullable String url) {
            super(spanClass, start, end, flags);
            this.url = url;
        }

        public URLSpan(SpanProto proto) {
            super(proto);
            this.url = proto.getUrl();
        }

        /** see android.text.style.URLSpan#getURL() */
        public @Nullable String getUrl() {
            return url;
        }

        @Override
        public SpanProto toProto() {
            // Explicitly not invoking super to ensure the SpanType is properly set
            SpanProto.Builder builder = SpanProto.newBuilder();
            builder.setSpanClassName(getSpanClassName());
            builder.setStart(getStart());
            builder.setEnd(getEnd());
            builder.setFlags(getFlags());
            builder.setType(SpanType.URL);
            String url = getUrl();
            if (url != null) {
                builder.setUrl(url);
            }
            return builder.build();
        }

        @Override
        protected Span copyWithAdjustedPosition(int newStart, int newEnd) {
            return new URLSpan(getSpanClassName(), newStart, newEnd, getFlags(), url);
        }
    }
}
