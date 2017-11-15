/*
 * Kvantum is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.kvantum.api.util;

import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.matching.Router;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import com.github.intellectualsites.kvantum.api.views.errors.View404;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Builder
final public class RequestManager extends Router
{

    private static Generator<AbstractRequest, RequestHandler> DEFAULT_404_GENERATOR =
            (request) -> View404.construct( request.getQuery().getFullRequest() );

    @Builder.Default
    private List<RequestHandler> views = new ArrayList<>();

    @Setter
    @Getter
    @NonNull
    @Builder.Default
    private Generator<AbstractRequest, RequestHandler> error404Generator = DEFAULT_404_GENERATOR;

    /**
     * Register a view to the request manager
     *
     * @param view The view to register
     */
    @Override
    public RequestHandler add(final RequestHandler view)
    {
        Assert.notNull( view );

        final Optional<RequestHandler> illegalRequestHandler = LambdaUtil.getFirst( views, v -> v.toString()
                .equalsIgnoreCase( view.toString() ) );
        if ( illegalRequestHandler.isPresent() )
        {
            throw new IllegalArgumentException( "Duplicate view pattern!" );
        }
        views.add( view );
        return view;
    }

    /**
     * Try to find the request handler that matches the request
     *
     * @param request Incoming request
     * @return Matching request handler, or {@link #getError404Generator()} if none was found
     */
    @Override
    public RequestHandler match(final AbstractRequest request)
    {
        Assert.isValid( request );

        final Optional<RequestHandler> view = LambdaUtil.getFirst( views, request.matches );
        if ( view.isPresent() )
        {
            return view.get();
        }
        return error404Generator.generate( request );
    }

    @Override
    public void dump(final Kvantum server)
    {
        Assert.notNull( server );

        ( (IConsumer<RequestHandler>) view -> Message.REQUEST_HANDLER_DUMP.log( view.getClass().getSimpleName(), view
                .toString() ) ).foreach( views );
    }

    @Override
    public void remove(final RequestHandler view)
    {
        Assert.notNull( view );

        if ( views.contains( view ) )
        {
            views.remove( view );
        }
    }

    @Override
    public void clear()
    {
        Message.CLEARED_VIEWS.log( CollectionUtil.clear( this.views ) );
    }

}