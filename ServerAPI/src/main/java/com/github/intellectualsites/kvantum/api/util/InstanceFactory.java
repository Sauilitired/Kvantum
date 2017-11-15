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

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utility class for dealing with singleton instances
 */
@SuppressWarnings( "ALL" )
@UtilityClass
public class InstanceFactory
{

    public static <T> void setupInstance(final T t, final String fieldName)
    {
        try
        {
            Field field = t.getClass().getDeclaredField( fieldName );
            if ( !field.isAccessible() )
            {
                field.setAccessible( true );
            }
            field.set( null, t );

            Assert.equals( field.get( null ).equals( t ), true );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    public static <T> void setupInstance(final T t)
    {
        setupInstance( t, "instance" );
    }

    public static <T> void setupInstanceAutomagic(final T t)
    {
        for ( final Field field : t.getClass().getDeclaredFields() )
        {
            if ( Modifier.isStatic( field.getModifiers() ) && field.getType().equals( t.getClass() ) )
            {
                try
                {
                    field.setAccessible( true );
                    if ( field.get( null ) != null )
                    {
                        continue;
                    }
                    field.set( null, t );
                } catch ( IllegalAccessException e )
                {
                    e.printStackTrace();
                }
            }
        }
    }

}