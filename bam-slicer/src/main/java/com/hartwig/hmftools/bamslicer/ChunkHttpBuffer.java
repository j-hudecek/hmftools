package com.hartwig.hmftools.bamslicer;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import htsjdk.samtools.Chunk;
import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

class ChunkHttpBuffer {
    private static final Logger LOGGER = LogManager.getLogger(ChunkHttpBuffer.class);
    @NotNull
    private final LoadingCache<Long, ListenableFuture<byte[]>> chunkBuffer;
    private final int maxSize;
    @NotNull
    private final URL url;
    @NotNull
    private final ConcurrentSkipListMap<Long, Chunk> chunksPerOffset = new ConcurrentSkipListMap<>();
    @NotNull
    private final OkHttpClient httpClient;

    ChunkHttpBuffer(@NotNull final OkHttpClient httpClient, @NotNull final URL url, final int maxSize, @NotNull final List<Chunk> chunks) {
        this.httpClient = httpClient;
        this.url = url;
        this.maxSize = maxSize;
        for (final Chunk chunk : chunks) {
            final long chunkStart = BlockCompressedFilePointerUtil.getBlockAddress(chunk.getChunkStart());
            chunksPerOffset.put(chunkStart, chunk);
        }
        chunkBuffer = CacheBuilder.newBuilder().maximumSize(maxSize).build(new CacheLoader<Long, ListenableFuture<byte[]>>() {
            @Override
            @NotNull
            public ListenableFuture<byte[]> load(@NotNull final Long offset) {
                final Chunk chunkAtOffset = chunksPerOffset.get(offset);
                return getBytesForChunk(chunkAtOffset);
            }

            @Override
            public ListenableFuture<ListenableFuture<byte[]>> reload(@NotNull final Long offset,
                    @NotNull final ListenableFuture<byte[]> oldBytes) {
                return Futures.immediateFuture(oldBytes);
            }
        });
    }

    @NotNull
    Pair<Long, byte[]> getEntryAtPosition(final long position) throws IOException {
        final long chunkOffset = chunksPerOffset.floorEntry(position).getKey();
        final byte[] bytesAtOffset;
        try {
            if (chunkBuffer.getIfPresent(chunkOffset) == null) {
                refillBuffer(chunkOffset);
            }
            bytesAtOffset = chunkBuffer.getUnchecked(chunkOffset).get();
            chunkBuffer.invalidate(chunkOffset);
            if (chunkBuffer.size() < maxSize / 5 + 1) {
                refillBuffer(chunkOffset);
            }
            return Pair.of(chunkOffset, bytesAtOffset);
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Could not read entry at position " + position + ". Cause: " + e.getMessage());
        }
    }

    private void refillBuffer(final long chunkOffset) {
        final int fillSize = (int) (maxSize * .75);
        chunksPerOffset.tailMap(chunkOffset, false).keySet().stream().limit(fillSize).forEach(key -> {
            if (chunkBuffer.getIfPresent(key) == null) {
                chunkBuffer.refresh(key);
            }
        });
    }

    @NotNull
    private ListenableFuture<byte[]> getBytesForChunk(@NotNull final Chunk chunk) {
        final long start = BlockCompressedFilePointerUtil.getBlockAddress(chunk.getChunkStart());
        final long end = BlockCompressedFilePointerUtil.getBlockAddress(chunk.getChunkEnd());
        if (start <= end) {
            return readUrlBytes(start, end - start);
        } else {
            return Futures.immediateFailedFuture(new IllegalArgumentException("start offset is greater than end"));
        }
    }

    @NotNull
    private ListenableFuture<byte[]> readUrlBytes(final long offset, final long count) {
        final Headers httpHeaders = new Headers.Builder().add("Range", "bytes=" + offset + "-" + (offset + count - 1)).build();
        final Request request = new Request.Builder().url(url).headers(httpHeaders).build();
        final SettableFuture<byte[]> bytesFuture = SettableFuture.create();
        httpClient.newCall(request).enqueue(retryingCallback(10, bytesFuture));
        return bytesFuture;
    }

    @NotNull
    private Callback retryingCallback(final int retryCount, @NotNull final SettableFuture<byte[]> resultFuture) {
        return new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {
                retryCall(call, e, retryCount, resultFuture);
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) {
                final ResponseBody body = response.body();
                try {
                    if (response.isSuccessful() && body != null) {
                        resultFuture.set(body.bytes());
                    } else {
                        final String nullBody = body == null ? "body = null" : "";
                        final Exception e = new IOException("Response " + response.code() + ": " + response.message() + "; " + nullBody);
                        retryCall(call, e, retryCount, resultFuture);
                    }
                } catch (final Exception e) {
                    retryCall(call, e, retryCount, resultFuture);
                } finally {
                    if (body != null) {
                        body.close();
                    }
                }
            }
        };
    }

    private void retryCall(@NotNull final Call call, @NotNull final Exception exception, final int remainingRetries,
            @NotNull final SettableFuture<byte[]> resultFuture) {
        if (remainingRetries <= 0) {
            LOGGER.error("Call {} [{}] failed with exception: {}",
                    call.request().method(),
                    call.request().headers(),
                    exception.getMessage());
            resultFuture.setException(exception);
        } else {
            call.clone().enqueue(retryingCallback(remainingRetries - 1, resultFuture));
        }
    }

    @NotNull
    URL url() {
        return url;
    }

    void closeHttpClient() {
        httpClient.dispatcher().executorService().shutdown();
    }
}
