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
package com.github.intellectualsites.kvantum.api.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * BufferedOutputStream that asynchronously flushes to disk, so callers don't
 * have to wait until the flush happens. Buffers are put into a queue that is
 * written asynchronously to disk once it is really available.
 * <p>
 * This class is thread-safe.
 * <p>
 * The error handling (as all stream ops are done asynchronously) is done during
 * write and close. Exceptions on the asynchronous thread will be thrown to the
 * caller either while writing or closing this stream.
 *
 * @author thomas.jungblut
 */
public final class AsyncBufferedOutputStream extends FilterOutputStream
{

    private final FlushThread flusher = new FlushThread();
    private final Thread flusherThread = new Thread( flusher, "FlushThread" );
    private final ConcurrentLinkedDeque<byte[]> buffers;

    private final byte[] buf;
    private int count = 0;

    /**
     * Creates an asynchronous buffered output stream with 8K buffer and 5 maximal
     * buffers.
     */
    public AsyncBufferedOutputStream(OutputStream out)
    {
        this( out, 8 * 1024, 5 );
    }

    /**
     * Creates an asynchronous buffered output stream with defined buffersize and
     * 5 maximal buffers.
     */
    public AsyncBufferedOutputStream(OutputStream out, int bufSize)
    {
        this( out, bufSize, 5 );
    }

    /**
     * Creates an asynchronous buffered output stream.
     *
     * @param out        the outputstream to layer on.
     * @param bufSize    the buffer size.
     * @param maxBuffers the number of buffers to keep in parallel.
     */
    public AsyncBufferedOutputStream(OutputStream out, int bufSize, int maxBuffers)
    {
        super( out );
        buffers = new ConcurrentLinkedDeque<>();
        buf = new byte[ bufSize ];
        flusherThread.start();
    }

    /**
     * Writes the specified byte to this buffered output stream.
     *
     * @param b the byte to be written.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public synchronized void write(int b) throws IOException
    {
        flushBufferIfSizeLimitReached();
        throwOnFlusherError();
        buf[ count++ ] = (byte) b;
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        write( b, 0, b.length );
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at
     * offset <code>off</code> to this buffered output stream.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException
    {
        if ( ( off | len | ( b.length - ( len + off ) ) | ( off + len ) ) < 0 )
        {
            throw new IndexOutOfBoundsException();
        }

        int bytesWritten = 0;
        while ( bytesWritten < len )
        {
            throwOnFlusherError();
            flushBufferIfSizeLimitReached();

            int bytesToWrite = Math.min( len - bytesWritten, buf.length - count );
            System.arraycopy( b, off + bytesWritten, buf, count, bytesToWrite );
            count += bytesToWrite;
            bytesWritten += bytesToWrite;
        }
    }

    /**
     * Flushes this buffered output stream. It will enforce that the current
     * buffer will be queue for asynchronous flushing no matter what size it has.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public synchronized void flush() throws IOException
    {
        forceFlush();
    }

    private void flushBufferIfSizeLimitReached() throws IOException
    {
        if ( count >= buf.length )
        {
            forceFlush();
        }
    }

    private void forceFlush() throws IOException
    {
        if ( count > 0 )
        {
            final byte[] copy = new byte[ count ];
            System.arraycopy( buf, 0, copy, 0, copy.length );
            buffers.add( copy );
            count = 0;
        }
    }

    @Override
    public synchronized void close() throws IOException
    {
        throwOnFlusherError();

        forceFlush();
        flusher.closed = true;

        try
        {
            flusherThread.interrupt();
            flusherThread.join();

            throwOnFlusherError();
        } catch ( InterruptedException e )
        {
            // this is expected to happen
        } finally
        {
            out.close();
        }
    }

    private void throwOnFlusherError() throws IOException
    {
        if ( flusher != null && flusher.errorHappened )
        {
            throw new IOException( "caught flusher to fail writing asynchronously!",
                    flusher.caughtException );
        }
    }

    class FlushThread implements Runnable
    {

        volatile boolean closed = false;
        volatile boolean errorHappened = false;
        volatile Exception caughtException;

        @Override
        public void run()
        {
            // run the real flushing action to the underlying stream
            try
            {
                while ( !closed )
                {
                    byte[] take = buffers.poll();
                    if ( take != null )
                    {
                        out.write( take );
                    }
                }
            } catch ( Exception e )
            {
                caughtException = e;
                errorHappened = true;
                // yield this thread, an error happened
                return;
            }
        }
    }

}