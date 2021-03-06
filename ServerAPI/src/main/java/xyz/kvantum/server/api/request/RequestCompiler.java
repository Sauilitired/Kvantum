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
package xyz.kvantum.server.api.request;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.exceptions.RequestException;
import xyz.kvantum.server.api.request.AbstractRequest.QueryParameters;
import xyz.kvantum.server.api.util.AsciiString;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass public final class RequestCompiler {

    private static final Timer TIMER_COMPILE_QUERY;
    private static final Pattern PATTERN_QUERY = Pattern.compile(
        "(?<method>[A-Za-z]+) (?<resource>[/\\-A-Za-z0-9.?=&:@!%]*) "
            + "(?<protocol>(?<prottype>[A-Za-z]+)/(?<protver>[A-Za-z0-9.]+))?");
    private static final Pattern PATTERN_HEADER =
        Pattern.compile("(?<key>[A-Za-z-_0-9]+)\\s*:\\s*(?<value>.*$)");
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String METHOD = "method";
    private static final String RESOURCE = "resource";

    static {
        if (ServerImplementation.getImplementation() != null) {
            TIMER_COMPILE_QUERY =
                ServerImplementation.getImplementation().getMetrics().getRegistry()
                    .timer(MetricRegistry.name(RequestCompiler.class, "compileQuery"));
        } else {
            TIMER_COMPILE_QUERY = null;
        }
    }

    public static Optional<HeaderPair> compileHeader(final String line) {
        final Matcher matcher = PATTERN_HEADER.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        final AsciiString key = AsciiString.of(matcher.group(KEY).toLowerCase(Locale.ENGLISH));
        final AsciiString value = AsciiString.of(matcher.group(VALUE), false);
        return Optional.of(new HeaderPair(key, value));
    }

    public static void compileQuery(final AbstractRequest request, final String line)
        throws IllegalArgumentException, RequestException {
        final Timer.Context timer = TIMER_COMPILE_QUERY.time();
        final Matcher matcher = PATTERN_QUERY.matcher(line);
        if (!matcher.matches()) {
            timer.close();
            throw new IllegalArgumentException(String.format("Not a query line: %s", line));
        }
        if (CoreConfig.verbose) {
            ServerImplementation.getImplementation().log("Query: " + matcher.group());
        }
        final Optional<HttpMethod> methodOptional = HttpMethod.getByName(matcher.group(METHOD));
        if (!methodOptional.isPresent()) {
            timer.close();
            throw new RequestException("Unknown request method: " + matcher.group(METHOD), request);
        }
        // request.setQuery( new AbstractRequest.Query( methodOptional.get(), request.getProtocolType(),
        // 		matcher.group( RESOURCE ) ) );
        request.setQuery(AbstractRequest.QueryCache.getInstance().getQuery(
            new QueryParameters(methodOptional.get(), request.getProtocolType(),
                matcher.group(RESOURCE))));
        timer.close();
    }

    @Getter @RequiredArgsConstructor public static final class HeaderPair {

        private final AsciiString key;
        private final AsciiString value;
    }

}
