package com.hartwig.hmftools.patientreporter.util;

import org.jetbrains.annotations.Nullable;

public enum PatientReportFormat {
    ;

    public static String formatNullablePercent(final @Nullable Double percentage) {
        return percentage == null ? "Na" : formatPercent(percentage);
    }

    public static String formatPercent(final double percentage) {
        return Long.toString(Math.round(percentage * 100D)) + "%";
    }
}
