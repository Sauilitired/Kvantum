/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2017 IntellectualSites
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
package xyz.kvantum.server.api.request;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.exceptions.RequestException;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({ "unused", "WeakerAccess" })
@UtilityClass
public class RequestCompiler
{

    private static final Pattern PATTERN_QUERY = Pattern.compile(
            "(?<method>[A-Za-z]+) (?<resource>[/\\-A-Za-z0-9.?=&:@!%]*) " +
                    "(?<protocol>(?<prottype>[A-Za-z]+)/(?<protver>[A-Za-z0-9.]+))?"
    );
    private static final Pattern PATTERN_HEADER = Pattern.compile( "(?<key>[A-Za-z-_0-9]+)\\s*:\\s*(?<value>.*$)" );

    public static Optional<HeaderPair> compileHeader(@NonNull final String line)
    {
        final Matcher matcher = PATTERN_HEADER.matcher( line );
        if ( !matcher.matches() )
        {
            return Optional.empty();
        }
        return Optional.of( new HeaderPair( matcher.group( "key" ).toLowerCase( Locale.ENGLISH ),
                matcher.group( "value" ) ) );
    }

    public static void compileQuery(@NonNull final AbstractRequest request, @NonNull final String line)
            throws IllegalArgumentException, RequestException
    {
        final Matcher matcher = PATTERN_QUERY.matcher( line );
        if ( !matcher.matches() )
        {
            throw new IllegalArgumentException( "Not a query line" );
        }
        if ( CoreConfig.verbose )
        {
            ServerImplementation.getImplementation().log( "Query: " + matcher.group() );
        }
        final Optional<HttpMethod> methodOptional = HttpMethod.getByName( matcher.group( "method" ) );
        if ( !methodOptional.isPresent() )
        {
            throw new RequestException( "Unknown request method: " + matcher.group( "method" ),
                    request );
        }
        request.setQuery( new AbstractRequest.Query( methodOptional.get(), matcher.group( "resource" ) ) );
    }

    @Getter
    @RequiredArgsConstructor
    public static final class HeaderPair
    {

        @NonNull
        private final String key;
        @NonNull
        private final String value;
    }

}