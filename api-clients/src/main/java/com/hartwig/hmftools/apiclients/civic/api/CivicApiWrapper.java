package com.hartwig.hmftools.apiclients.civic.api;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import com.google.gson.Gson;
import com.hartwig.hmftools.apiclients.civic.data.CivicApiDataGson;
import com.hartwig.hmftools.apiclients.civic.data.CivicApiMetadata;
import com.hartwig.hmftools.apiclients.civic.data.CivicEvidenceItem;
import com.hartwig.hmftools.apiclients.civic.data.CivicGene;
import com.hartwig.hmftools.apiclients.civic.data.CivicIndexResult;
import com.hartwig.hmftools.apiclients.civic.data.CivicVariantWithEvidence;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CivicApiWrapper {
    private static final String CIVIC_API_ENDPOINT = "https://civic.genome.wustl.edu/api/";
    private static final Long CIVIC_BATCH_COUNT = 10000L;
    private final CivicApi api;
    private final OkHttpClient httpClient;

    public CivicApiWrapper() {
        final Dispatcher requestDispatcher = new Dispatcher();
        requestDispatcher.setMaxRequests(100);
        requestDispatcher.setMaxRequestsPerHost(100);
        httpClient = new OkHttpClient.Builder().connectionPool(new ConnectionPool(20, 5, TimeUnit.SECONDS))
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .dispatcher(requestDispatcher)
                .build();
        final Gson gson = CivicApiDataGson.buildGson();
        final Retrofit retrofit = new Retrofit.Builder().baseUrl(CIVIC_API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .client(httpClient)
                .build();
        api = retrofit.create(CivicApi.class);
    }

    @NotNull
    public Observable<CivicVariantWithEvidence> getVariantsForGene(final int entrezId) {
        return api.getGene(entrezId)
                .flatMapIterable(CivicGene::variantMetadatas)
                .flatMap(variantMetadata -> api.getVariant(variantMetadata.id()));
    }

    @NotNull
    public Observable<CivicVariantWithEvidence> getAllWildTypeVariants() {
        return getAllFromPaginatedEndpoint(api::getVariants).filter(variantMetadata -> {
            final String type = variantMetadata.type();
            final String name = variantMetadata.name();
            return (type != null && type.toLowerCase().equals("wild_type")) || (name != null && (name.toLowerCase().equals("wild type")
                    || name.toLowerCase().equals("wildtype")));
        }).flatMap(variantMetadata -> api.getVariant(variantMetadata.id()));
    }

    @NotNull
    public Map<Integer, String> getDrugInteractionMap() {
        return getAllFromPaginatedEndpoint(api::getEvidenceItems).toMap(CivicEvidenceItem::id,
                evidenceItem -> evidenceItem.drugInteractionType() == null ? Strings.EMPTY : evidenceItem.drugInteractionType())
                .blockingGet();
    }

    public void releaseResources() {
        httpClient.dispatcher().executorService().shutdown();
    }

    @NotNull
    private <T> Observable<T> getAllFromPaginatedEndpoint(@NotNull final BiFunction<Long, Long, Observable<CivicIndexResult<T>>> endpoint) {
        return endpoint.apply(1L, CIVIC_BATCH_COUNT).flatMap(indexResult -> {
            final CivicApiMetadata metadata = indexResult.meta();
            final Observable<T> firstPageResults = Observable.fromIterable(indexResult.records());
            final Observable<T> nextPagesResults = Observable.range(2, metadata.totalPages() - 1)
                    .flatMap(page -> endpoint.apply((long) page, CIVIC_BATCH_COUNT).flatMapIterable(CivicIndexResult::records));
            return firstPageResults.mergeWith(nextPagesResults);
        });
    }
}
