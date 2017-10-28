/*
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.intellectualsites.iserver.api.account.Account;
import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.response.ResponseBody;
import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.api.views.RequestHandler;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The utility file that
 * handles all runtime caching
 */
@SuppressWarnings("ALL")
public final class CacheManager
{

    private final Cache<String, String> cachedIncludes;
    private final Cache<String, String> cachedFiles;
    private final Cache<String, CachedResponse> cachedBodies;
    private final Cache<Integer, Account> cachedAccounts;
    private final Cache<String, Integer> cachedAccountIds;

    public CacheManager()
    {
        cachedIncludes = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedIncludesExpiry, TimeUnit
                .SECONDS ).maximumSize( CoreConfig.Cache.cachedIncludesMaxItems ).build();
        cachedFiles = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedFilesExpiry, TimeUnit
                .SECONDS ).maximumSize( CoreConfig.Cache.cachedFilesMaxItems ).build();
        cachedBodies = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedBodiesExpiry, TimeUnit
                .SECONDS ).maximumSize( CoreConfig.Cache.cachedBodiesMaxItems ).build();
        cachedAccounts = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedAccountsExpiry, TimeUnit
                .SECONDS ).maximumSize( CoreConfig.Cache.cachedAccountsMaxItems ).build();
        cachedAccountIds = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedAccountIdsExpiry, TimeUnit
                .SECONDS ).maximumSize( CoreConfig.Cache.cachedAccountIdsMaxItems ).build();
    }

    /**
     * Get a cached include block
     *
     * @param group Include block (matcher.group())
     * @return string|null
     */
    public String getCachedInclude(final String group)
    {
        Assert.notNull( group );

        return this.cachedIncludes.getIfPresent( group );
    }

    public Optional<Account> getCachedAccount(final int id)
    {
        return Optional.ofNullable( cachedAccounts.getIfPresent( id ) );
    }

    public Optional<Integer> getCachedId(final String username)
    {
        return Optional.ofNullable( cachedAccountIds.getIfPresent( username ) );
    }

    public void setCachedAccount(final Account account)
    {
        this.cachedAccounts.put( account.getId(), account );
        this.cachedAccountIds.put( account.getUsername(), account.getId() );
    }

    public Optional<String> getCachedFile(final String file)
    {
        Assert.notEmpty( file );

        if ( CoreConfig.debug )
        {
            ServerImplementation.getImplementation().log( "Accessing cached file: " + file );
        }

        if ( !CoreConfig.Cache.enabled )
        {
            return Optional.empty();
        }

        return Optional.ofNullable( cachedFiles.getIfPresent( file ) );
    }

    public void setCachedFile(final String file, final String content)
    {
        Assert.notEmpty( file );
        Assert.notNull( content );

        cachedFiles.put( file, content );
    }

    /**
     * Set a cached include block
     *
     * @param group    matcher.group()
     * @param document Generated document
     */
    public void setCachedInclude(final String group, final String document)
    {
        Assert.notNull( group, document );

        this.cachedIncludes.put( group, document );
    }

    /**
     * Check if there is a ResponseBody cached for the view
     *
     * @param view RequestHandler
     * @return true if there is a ResponseBody cached, else false
     */
    public boolean hasCache(final RequestHandler view)
    {
        Assert.notNull( view );

        return this.cachedBodies.getIfPresent( view.toString() ) != null;
    }

    /**
     * Add a cached ResponseBody
     *
     * @param view         RequestHandler for which the caching will apply
     * @param responseBody ResponseBody (will generate a CachedResponseBody)
     * @see CachedResponse
     */
    public void setCache(final RequestHandler view, final ResponseBody responseBody)
    {
        Assert.notNull( view, responseBody );

        this.cachedBodies.put( view.toString(), new CachedResponse( responseBody ) );
    }

    /**
     * Get the cached reponse for a view
     *
     * @param view RequestHandler
     * @return the cached ResponseBody
     * @see #hasCache(RequestHandler) To check if the view has a cache
     */
    public CachedResponse getCache(final RequestHandler view)
    {
        Assert.notNull( view );

        if ( CoreConfig.debug )
        {
            ServerImplementation.getImplementation().log( "Accessing cached body: " + view );
        }

        return this.cachedBodies.getIfPresent( view.toString() );
    }
}
