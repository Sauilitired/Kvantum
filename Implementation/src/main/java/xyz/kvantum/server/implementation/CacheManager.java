/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.implementation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import xyz.kvantum.files.CachedFile;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.cache.CachedResponse;
import xyz.kvantum.server.api.cache.ICacheManager;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.response.ResponseBody;
import xyz.kvantum.server.api.views.RequestHandler;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The utility file that handles all runtime caching
 */
@SuppressWarnings("ALL") public final class CacheManager implements ICacheManager {

    private final Cache<String, String> cachedIncludes;
    private final Cache<String, CachedFile> cachedFiles;
    private final Cache<String, CachedResponse> cachedBodies;

    public CacheManager() {
        cachedIncludes = Caffeine.newBuilder()
            .expireAfterWrite(CoreConfig.Cache.cachedIncludesExpiry, TimeUnit.SECONDS)
            .maximumSize(CoreConfig.Cache.cachedIncludesMaxItems).build();
        cachedFiles = Caffeine.newBuilder()
            .expireAfterWrite(CoreConfig.Cache.cachedFilesExpiry, TimeUnit.SECONDS)
            .maximumSize(CoreConfig.Cache.cachedFilesMaxItems).build();
        cachedBodies = Caffeine.newBuilder()
            .expireAfterWrite(CoreConfig.Cache.cachedBodiesExpiry, TimeUnit.SECONDS)
            .maximumSize(CoreConfig.Cache.cachedBodiesMaxItems).build();
    }

    @Override public String getCachedInclude(final String group) {
        return this.cachedIncludes.getIfPresent(group);
    }

    @Override public Optional<CachedFile> getCachedFile(final Path file) {
        return Optional.ofNullable(cachedFiles.getIfPresent(file.toString()));
    }

    @Override public void setCachedFile(final Path file, final CachedFile content) {
        cachedFiles.put(file.toString(), content);
    }

    @Override public void setCachedInclude(final String group, final String document) {
        this.cachedIncludes.put(group, document);
    }

    @Override public void removeFileCache(final Path path) {
        this.cachedFiles.invalidate(path.toString());
    }

    @Override public boolean hasCache(final RequestHandler view) {
        return this.cachedBodies.getIfPresent(view.toString()) != null;
    }

    @Override public void setCache(final RequestHandler view, final ResponseBody responseBody) {
        this.cachedBodies.put(view.toString(), new CachedResponse(responseBody));
    }

    @Override public CachedResponse getCache(final RequestHandler view) {
        return this.cachedBodies.getIfPresent(view.toString());
    }

}
