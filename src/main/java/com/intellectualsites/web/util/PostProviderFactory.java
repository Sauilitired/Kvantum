//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
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

package com.intellectualsites.web.util;

import com.intellectualsites.web.object.PostRequest;
import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.syntax.VariableProvider;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Created 2015-04-25 for IntellectualServer
 *
 * @author Citymonstret
 */
@NoArgsConstructor
public class PostProviderFactory implements ProviderFactory<PostProviderFactory>, VariableProvider {

    private PostRequest p;

    private PostProviderFactory(@NonNull final PostRequest p) {
        this.p = p;
    }

    @Override
    public PostProviderFactory get(@NonNull final Request r) {
        if (r.getPostRequest() == null) {
            return null;
        }
        return new PostProviderFactory(r.getPostRequest());
    }

    @Override
    public String providerName() {
        return "post";
    }

    @Override
    public boolean contains(@NonNull final String variable) {
        return p.contains(variable);
    }

    @Override
    public Object get(@NonNull final String variable) {
        return p.get(variable);
    }
}
