package com.hartwig.hmftools.healthchecker.runners;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.hmftools.common.context.RunContext;
import com.hartwig.hmftools.common.exception.HartwigException;
import com.hartwig.hmftools.common.variant.SomaticVariant;
import com.hartwig.hmftools.common.variant.SomaticVariantFactory;
import com.hartwig.hmftools.common.variant.VariantType;
import com.hartwig.hmftools.common.variant.predicate.VariantFilter;
import com.hartwig.hmftools.common.variant.predicate.VariantPredicates;
import com.hartwig.hmftools.healthchecker.result.BaseResult;
import com.hartwig.hmftools.healthchecker.result.MultiValueResult;
import com.hartwig.hmftools.healthchecker.result.NoResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class SomaticVariantsChecker extends ErrorHandlingChecker implements HealthChecker {

    private static final Logger LOGGER = LogManager.getLogger(SomaticVariantsChecker.class);

    private static final String SOMATICS_EXTENSION = "_post_processed.vcf";

    public SomaticVariantsChecker() {
    }

    @NotNull
    @Override
    public BaseResult tryRun(@NotNull final RunContext runContext) throws IOException, HartwigException {
        if (!runContext.isSomaticRun()) {
            return new NoResult(CheckType.SOMATIC_VARIANTS);
        }
        final List<SomaticVariant> variants =
                new SomaticVariantFactory().fromVCFFile(runContext.tumorSample(), runContext.runDirectory(), SOMATICS_EXTENSION);
        final List<SomaticVariant> snps = VariantFilter.filter(variants, VariantPredicates.withType(VariantType.SNP));
        final List<SomaticVariant> mnps = VariantFilter.filter(variants, VariantPredicates.withType(VariantType.MNP));
        final List<SomaticVariant> indels = VariantFilter.filter(variants, VariantPredicates.withType(VariantType.INDEL));

        final List<SomaticVariant> snpsWithDBSNPAndNotCOSMIC = VariantFilter.filter(snps, VariantPredicates.inDBSNPAndNotInCOSMIC());
        final List<SomaticVariant> mnpWithDBSNPAndNotCOSMIC = VariantFilter.filter(mnps, VariantPredicates.inDBSNPAndNotInCOSMIC());
        final List<SomaticVariant> indelsWithDBSNPAndNotCOSMIC = VariantFilter.filter(indels, VariantPredicates.inDBSNPAndNotInCOSMIC());

        final List<HealthCheck> checks = Lists.newArrayList();
        checks.add(new HealthCheck(runContext.tumorSample(),
                SomaticVariantCheck.SOMATIC_SNP_COUNT.toString(),
                String.valueOf(snps.size())));
        checks.add(new HealthCheck(runContext.tumorSample(),
                SomaticVariantCheck.SOMATIC_INDEL_COUNT.toString(),
                String.valueOf(indels.size())));
        checks.add(new HealthCheck(runContext.tumorSample(),
                SomaticVariantCheck.SOMATIC_SNP_DBSNP_COUNT.toString(),
                String.valueOf(snpsWithDBSNPAndNotCOSMIC.size())));
        checks.add(new HealthCheck(runContext.tumorSample(),
                SomaticVariantCheck.SOMATIC_INDEL_DBSNP_COUNT.toString(),
                String.valueOf(indelsWithDBSNPAndNotCOSMIC.size())));
        checks.add(new HealthCheck(runContext.tumorSample(),
                SomaticVariantCheck.SOMATIC_MNP_COUNT.toString(),
                String.valueOf(mnps.size())));
        checks.add(new HealthCheck(runContext.tumorSample(),
                SomaticVariantCheck.SOMATIC_MNP_DBSNP_COUNT.toString(),
                String.valueOf(mnpWithDBSNPAndNotCOSMIC.size())));

        return toMultiValueResult(checks);
    }

    @NotNull
    @Override
    public BaseResult errorRun(@NotNull final RunContext runContext) {
        if (runContext.isSomaticRun()) {
            final List<HealthCheck> checks = Lists.newArrayList();

            checks.add(new HealthCheck(runContext.tumorSample(),
                    SomaticVariantCheck.SOMATIC_SNP_COUNT.toString(),
                    HealthCheckConstants.ERROR_VALUE));
            checks.add(new HealthCheck(runContext.tumorSample(),
                    SomaticVariantCheck.SOMATIC_INDEL_COUNT.toString(),
                    HealthCheckConstants.ERROR_VALUE));
            checks.add(new HealthCheck(runContext.tumorSample(),
                    SomaticVariantCheck.SOMATIC_SNP_DBSNP_COUNT.toString(),
                    HealthCheckConstants.ERROR_VALUE));
            checks.add(new HealthCheck(runContext.tumorSample(),
                    SomaticVariantCheck.SOMATIC_INDEL_DBSNP_COUNT.toString(),
                    HealthCheckConstants.ERROR_VALUE));
            checks.add(new HealthCheck(runContext.tumorSample(),
                    SomaticVariantCheck.SOMATIC_MNP_COUNT.toString(),
                    HealthCheckConstants.ERROR_VALUE));
            checks.add(new HealthCheck(runContext.tumorSample(),
                    SomaticVariantCheck.SOMATIC_MNP_DBSNP_COUNT.toString(),
                    HealthCheckConstants.ERROR_VALUE));

            return toMultiValueResult(checks);
        } else {
            return new NoResult(CheckType.SOMATIC_VARIANTS);
        }
    }

    @NotNull
    private static BaseResult toMultiValueResult(@NotNull final List<HealthCheck> checks) {
        HealthCheck.log(LOGGER, checks);
        return new MultiValueResult(CheckType.SOMATIC_VARIANTS, checks);
    }
}
