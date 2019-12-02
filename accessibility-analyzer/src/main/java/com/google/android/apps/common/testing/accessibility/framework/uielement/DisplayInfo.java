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

import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DisplayInfoMetricsProto;
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.DisplayInfoProto;

import javax.annotation.Nullable;

/**
 * Representation of a Display
 * <p>
 * NOTE: Currently, this class holds only {@link Metrics}, but will likely have additional fields in
 * the future.
 */
public class DisplayInfo {

    private final Metrics metricsWithoutDecoration;
    private final @Nullable
    Metrics realMetrics;

    DisplayInfo(DisplayInfoProto fromProto) {
        this.metricsWithoutDecoration = new Metrics(fromProto.getMetricsWithoutDecoration());
        this.realMetrics =
                (fromProto.hasRealMetrics()) ? new Metrics(fromProto.getRealMetrics()) : null;
    }

    /**
     * @return a {@link Metrics} representing the display's metrics excluding certain system
     *         decorations.
     * see Display#getMetrics(DisplayMetrics)
     */
    public Metrics getMetricsWithoutDecoration() {
        return metricsWithoutDecoration;
    }

    /**
     * @return a {@link Metrics} representing the display's real metrics, which include system
     *         decorations. This value can be {@code null} for instances created on platform versions
     *         that don't support resolution of real metrics.
     * see Display#getRealMetrics(DisplayMetrics)
     */
    public @Nullable Metrics getRealMetrics() {
        return realMetrics;
    }

    DisplayInfoProto toProto() {
        DisplayInfoProto.Builder builder = DisplayInfoProto.newBuilder();
        builder.setMetricsWithoutDecoration(metricsWithoutDecoration.toProto());
        if (realMetrics != null) {
            builder.setRealMetrics(realMetrics.toProto());
        }
        return builder.build();
    }

    /**
     * Representation of a @link DisplayMetrics
     */
    public static class Metrics {

        private final float density;
        private final float scaledDensity;
        private final float xDpi;
        private final float yDpi;
        private final int densityDpi;
        private final int heightPixels;
        private final int widthPixels;

        Metrics(DisplayInfoMetricsProto fromProto) {
            this.density = fromProto.getDensity();
            this.scaledDensity = fromProto.getScaledDensity();
            this.xDpi = fromProto.getXDpi();
            this.yDpi = fromProto.getYDpi();
            this.densityDpi = fromProto.getDensityDpi();
            this.heightPixels = fromProto.getHeightPixels();
            this.widthPixels = fromProto.getWidthPixels();
        }

        /**
         * see DisplayMetrics#density
         */
        public float getDensity() {
            return density;
        }

        /**
         * see DisplayMetrics#scaledDensity
         */
        public float getScaledDensity() {
            return scaledDensity;
        }

        /**
         * see DisplayMetrics#xdpi
         */
        public float getxDpi() {
            return xDpi;
        }

        /**
         * see DisplayMetrics#ydpi
         */
        public float getyDpi() {
            return yDpi;
        }

        /**
         * see DisplayMetrics#densityDpi
         */
        public int getDensityDpi() {
            return densityDpi;
        }

        /**
         * see DisplayMetrics#heightPixels
         */
        public int getHeightPixels() {
            return heightPixels;
        }

        /**
         * see DisplayMetrics#widthPixels
         */
        public int getWidthPixels() {
            return widthPixels;
        }

        DisplayInfoMetricsProto toProto() {
            DisplayInfoMetricsProto.Builder builder = DisplayInfoMetricsProto.newBuilder();
            builder.setDensity(density);
            builder.setScaledDensity(scaledDensity);
            builder.setXDpi(xDpi);
            builder.setYDpi(yDpi);
            builder.setDensityDpi(densityDpi);
            builder.setHeightPixels(heightPixels);
            builder.setWidthPixels(widthPixels);
            return builder.build();
        }
    }
}
