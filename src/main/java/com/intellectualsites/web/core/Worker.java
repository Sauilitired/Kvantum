//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2016 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualsites.web.core;

import com.intellectualsites.web.config.Message;
import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.object.syntax.IgnoreSyntax;
import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.syntax.Syntax;
import com.intellectualsites.web.thread.ThreadManager;
import com.intellectualsites.web.views.RequestHandler;
import com.intellectualsites.web.views.View;
import lombok.Getter;
import lombok.NonNull;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

class Worker {

    private static byte[] empty = "NULL".getBytes();
    private static int idPool = 0;

    @Getter
    private final int id;

    Worker() {
        this.id = idPool++;
    }

    synchronized void start() {
        Server.getInstance().log("Started thread: " + id);
        final Server server = Server.getInstance();
        ThreadManager.createThread(() -> {
            if (!server.queue.isEmpty()) {
                Socket current = server.queue.poll();
                run(current, server);
            }
        });
    }

    public void run(final Socket remote, @NonNull final Server server) {
        if (remote == null || remote.isClosed()) {
            return; // TODO: Why?
        }

        if (server.verbose) {         // Do we want to output a load of useless information?
            server.log(Message.CONNECTION_ACCEPTED, remote.getInetAddress());
        }

        final Request request;
        final BufferedOutputStream output;
        final BufferedReader input;

        { // Read the actual request
            final StringBuilder rRaw = new StringBuilder();
            try {
                input = new BufferedReader(new InputStreamReader(remote.getInputStream()), server.bufferIn);
                output = new BufferedOutputStream(remote.getOutputStream(), server.bufferOut);
                String str;
                while ((str = input.readLine()) != null && !str.equals("")) {
                    rRaw.append(str).append("|");
                }
                request = new Request(rRaw.toString(), remote);
                // Fetch the post request, if it exists
                if (request.getQuery().getMethod() == Method.POST) {
                    final StringBuilder pR = new StringBuilder();
                    final int cl = Integer.parseInt(request.getHeader("Content-Length").substring(1));
                    for (int i = 0; i < cl; i++) {
                        pR.append((char) input.read());
                    }
                    request.setPostRequest(new PostRequest(pR.toString()));
                }
            } catch (final Exception e) {
                e.printStackTrace();
                return;
            }
        }

        if (!server.silent) {
            server.log(request.buildLog());
        }

        final RequestHandler requestHandler = server.requestManager.match(request);

        String textContent = "";
        byte[] bytes = empty;

        final Session session = server.sessionManager.getSession(request, output);
        if (session != null) {
            request.setSession(session);
        } else {
            request.setSession(server.sessionManager.createSession(request, output));
        }

        boolean shouldCache = false;
        boolean cache = false;
        final ResponseBody body;

        if (server.enableCaching && requestHandler instanceof CacheApplicable && ((CacheApplicable) requestHandler).isApplicable(request)) {
            cache = true;
            if (!server.cacheManager.hasCache(requestHandler)) {
                shouldCache = true;
            }
        }

        if (!cache || shouldCache) { // Either it's a non-cached view, or there is no cache stored
            body = requestHandler.generate(request);
        } else { // Just read from memory
            body = server.cacheManager.getCache(requestHandler);
        }

        if (shouldCache) {
            server.cacheManager.setCache(requestHandler, body);
        }

        if (body.isText()) {
            textContent = body.getContent();
        } else {
            bytes = body.getBytes();
        }

        for (final Map.Entry<String, String> postponedCookie : request.postponedCookies.entrySet()) {
            body.getHeader().setCookie(postponedCookie.getKey(), postponedCookie.getValue());
        }

        if (body.isText()) {
            // Make sure to not use Crush when
            // told not to
            if (!(requestHandler instanceof IgnoreSyntax)) {
                // Provider factories are fun, and so is the
                // global map. But we also need the view
                // specific ones!
                Map<String, ProviderFactory> factories = new HashMap<>();
                for (final ProviderFactory factory : server.providers) {
                    factories.put(factory.providerName().toLowerCase(), factory);
                }
                // Now make use of the view specific ProviderFactory
                ProviderFactory z = requestHandler.getFactory(request);
                if (z != null) {
                    factories.put(z.providerName().toLowerCase(), z);
                }
                // This is how the crush engine works.
                // Quite simple, yet powerful!
                for (Syntax syntax : server.syntaxes) {
                    if (syntax.matches(textContent)) {
                        textContent = syntax.handle(textContent, request, factories);
                    }
                }
            }
            // Now, finally, let's get the bytes.
            bytes = textContent.getBytes();
        }

        body.getHeader().apply(output);

        try {
            output.write(bytes);
            output.flush();
            input.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        if (!server.silent) {
            server.log("Request was served by '%s', with the type '%s'. The total length of the content was '%s'",
                    requestHandler.getName(), body.isText() ? "text" : "bytes", bytes.length
            );
        }
    }
}