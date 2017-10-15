/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.views;

import com.github.intellectualsites.iserver.api.cache.CacheApplicable;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.util.FileExtension;

import java.util.Map;

/**
 * Created 2015-04-21 for IntellectualServer
 *
 * @author Citymonstret
 */
public class ImgView extends StaticFileView implements CacheApplicable
{

    public ImgView(String filter, Map<String, Object> options)
    {
        super( filter, options, "img", FileExtension.IMAGE );
        super.relatedFolderPath = "/assets/img";
    }

    @Override
    public boolean isApplicable(Request r)
    {
        return false;
    }
}