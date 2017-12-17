/*
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
package xyz.kvantum.files;

import java.util.Optional;

@SuppressWarnings("unused")
public enum Extension
{
    CSS( new String[]{ "css" } ),
    HTML( new String[]{ "html", "xhtml", "htm", "vm" } ),
    PNG( new String[]{ "png" } ),
    ICO( new String[]{ "ico" } ),
    GIF( new String[]{ "gif" } ),
    JPEG( new String[]{ "jpg", "jpeg" } ),
    ZIP( new String[]{ "zip" } ),
    TXT( new String[]{ "txt" } ),
    PDF( new String[]{ "pdf" } ),
    JAVASCRIPT( new String[]{ "js", } );

    private final String[] extensions;

    Extension(final String[] extensions)
    {
        this.extensions = extensions;
    }

    public static Optional<Extension> getExtension(final String string)
    {
        String workingString = string;
        if ( string.startsWith( "." ) )
        {
            workingString = string.substring( 1 );
        }
        for ( final Extension extension : values() )
        {
            for ( final String e : extension.extensions )
            {
                if ( e.equalsIgnoreCase( workingString ) )
                {
                    return Optional.of( extension );
                }
            }
        }
        return Optional.empty();
    }

    public String[] getExtensions()
    {
        return this.extensions;
    }

    public boolean matches(final String string)
    {
        String workingString = string;
        if ( workingString.startsWith( "." ) )
        {
            workingString = workingString.substring( 1 );
        }
        for ( final String e : extensions )
        {
            if ( e.equalsIgnoreCase( workingString ) )
            {
                return true;
            }
        }
        return false;
    }

}
