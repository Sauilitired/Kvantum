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
package com.github.intellectualsites.iserver.api.views;

import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.Header;
import com.github.intellectualsites.iserver.api.response.Response;
import com.github.intellectualsites.iserver.api.util.FileExtension;
import com.github.intellectualsites.iserver.api.util.IgnoreSyntax;
import com.github.intellectualsites.iserver.files.Path;

import java.util.Map;

/**
 * Created 2015-05-01 for IntellectualServer
 *
 * @author Citymonstret
 */
public class DownloadView extends StaticFileView implements IgnoreSyntax
{

    public DownloadView(String filter, Map<String, Object> options)
    {
        super( filter, options, "download", FileExtension.DOWNLOADABLE );
        super.relatedFolderPath = "/assets/downloads";
    }

    @Override
    public void handle(final Request r, final Response response)
    {
        final Path path = r.getMetaUnsafe( "path" );
        final String fileName = path.getEntityName();
        final FileExtension extension = r.getMetaUnsafe( "extension" );

        response.getHeader().set( Header.HEADER_CONTENT_DISPOSITION,
                String.format( "attachment; filename=\"%s.%s\"", fileName, extension.getOption() ) );
        response.getHeader().set( Header.HEADER_CONTENT_TRANSFER_ENCODING, "binary" );
        response.getHeader().set( Header.HEADER_CONTENT_LENGTH, "" + r.<Long>getMetaUnsafe( "file_length" ) );
    }

}
