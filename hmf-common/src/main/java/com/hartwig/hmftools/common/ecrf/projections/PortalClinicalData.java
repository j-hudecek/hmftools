package com.hartwig.hmftools.common.ecrf.projections;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class PortalClinicalData {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private enum Header {
        cpctId,
        sampleId,
        gender,
        birthYear,
        registrationDate,
        cancerType,
        biopsySite,
        hmfId,
        sampleHmfId
    }

    @NotNull
    public abstract String cpctId();

    @NotNull
    public abstract String sampleId();

    @NotNull
    public abstract String gender();

    @NotNull
    public abstract String birthYear();

    @NotNull
    public abstract String registrationDate();

    @NotNull
    public abstract String cancerType();

    @NotNull
    public abstract String biopsySite();

    @NotNull
    public abstract String hmfId();

    @NotNull
    public abstract String sampleHmfId();

    @NotNull
    private List<String> csvRecord() {
        return Lists.newArrayList(cpctId(),
                sampleId(),
                gender(),
                birthYear(),
                registrationDate(),
                cancerType(),
                biopsySite(),
                hmfId(),
                sampleHmfId());
    }

    @NotNull
    public static PortalClinicalData of(@NotNull final String cpctId, @Nullable final String gender, @Nullable final Integer birthYear,
            @Nullable final LocalDate registrationDate, @Nullable final String cancerType) {
        return ImmutablePortalClinicalData.of(cpctId,
                "",
                Strings.nullToEmpty(gender),
                birthYear == null ? "" : birthYear.toString(),
                registrationDate == null ? "" : registrationDate.format(FORMATTER),
                Strings.nullToEmpty(cancerType),
                "",
                "",
                "");
    }

    @NotNull
    public static PortalClinicalData of(@NotNull final String cpctId, @Nullable final String sampleId, @Nullable final String gender,
            @Nullable final Integer birthYear, @Nullable final LocalDate registrationDate, @Nullable final String cancerType,
            @Nullable final String biopsySite) {
        return ImmutablePortalClinicalData.of(cpctId,
                Strings.nullToEmpty(sampleId),
                Strings.nullToEmpty(gender),
                birthYear == null ? "" : birthYear.toString(),
                registrationDate == null ? "" : registrationDate.format(FORMATTER),
                Strings.nullToEmpty(cancerType),
                Strings.nullToEmpty(biopsySite),
                "",
                "");
    }

    public static void writeRecords(@NotNull final String outputPath, @NotNull final List<PortalClinicalData> patientCancerTypes)
            throws IOException {
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(Header.class);
        final CSVPrinter printer = new CSVPrinter(new FileWriter(outputPath), format);
        printer.printRecords(patientCancerTypes.stream().map(PortalClinicalData::csvRecord).collect(Collectors.toList()));
        printer.close();
    }

    public static List<PortalClinicalData> readRecords(@NotNull final String filePath) throws IOException {
        final CSVParser parser = CSVParser.parse(new File(filePath),
                Charset.defaultCharset(),
                CSVFormat.DEFAULT.withHeader(Header.class).withSkipHeaderRecord());
        return StreamSupport.stream(parser.spliterator(), false)
                .map(record -> ImmutablePortalClinicalData.of(record.get(Header.cpctId),
                        record.get(Header.sampleId),
                        record.get(Header.gender),
                        record.get(Header.birthYear),
                        record.get(Header.registrationDate),
                        record.get(Header.cancerType),
                        record.get(Header.biopsySite),
                        record.get(Header.hmfId),
                        record.get(Header.sampleHmfId)))
                .collect(Collectors.toList());
    }
}