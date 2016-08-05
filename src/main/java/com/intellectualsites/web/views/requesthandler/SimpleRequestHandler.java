package com.intellectualsites.web.views.requesthandler;

import com.intellectualsites.web.object.Generator;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.Assert;
import com.intellectualsites.web.views.RequestHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class SimpleRequestHandler extends RequestHandler {

    private static AtomicInteger identifier = new AtomicInteger(0);

    @NonNull
    private final String pattern;
    @NonNull
    private final Generator<Request, Response> generator;
    private final String internalName = "simpleRequestHandler::" + identifier.getAndIncrement();

    private Pattern compiledPattern;

    protected Pattern getPattern() {
        if (compiledPattern == null) {
            compiledPattern = Pattern.compile(pattern);
        }
        return compiledPattern;
    }

    @Override
    public boolean matches(@NonNull final Request request) {
        final Matcher matcher = getPattern().matcher(request.getQuery().getResource());
        return matcher.matches();
    }

    @Override
    public Response generate(@NonNull final Request r) {
        final Response response = generator.generate(r);
        Assert.notNull(response);
        if (!response.hasParent()) {
            response.setParent(this);
        }
        return response;
    }

    @Override
    public String getName() {
        return this.internalName;
    }

}