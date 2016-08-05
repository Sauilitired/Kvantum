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

package com.intellectualsites.web.object;

import com.intellectualsites.web.util.TimeUtil;
import com.intellectualsites.web.views.RequestHandler;
import lombok.NonNull;

/**
 * The HTTP response,
 * this includes all headers
 * and the actual bytecode.
 *
 * @author Citymonstret
 */
final public class Response implements ResponseBody {

    private Header header;
    private String content;
    public RequestHandler parent;
    private boolean isText;
    private byte[] bytes;

    /**
     * Constructor
     *
     * @param parent The view that generated this response
     */
    public Response(final RequestHandler parent) {
        this.parent = parent;
        this.header = new Header(Header.STATUS_OK)
                .set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML)
                .set(Header.HEADER_SERVER, Header.POWERED_BY)
                .set(Header.HEADER_DATE, TimeUtil.getHTTPTimeStamp())
                .set(Header.HEADER_STATUS, Header.STATUS_OK)
                .set(Header.HEADER_X_POWERED_BY, Header.X_POWERED_BY);
        this.content = "";
        this.bytes = new byte[0];
    }

    public Response() {
        this(null);
    }

    /**
     * Use raw bytes, rather than text
     *
     * @param bytes Bytes to send to the client
     */
    public void setBytes(@NonNull final byte[] bytes) {
        this.bytes = bytes;
        this.isText = false;
    }

    /**
     * Get the bytes
     *
     * @return bytes, if exists
     * @see #isText() Should be false for this to work
     */
    @Override
    public byte[] getBytes() {
        return this.bytes;
    }

    /**
     * Set the text content
     *
     * @param content The string content
     * @see #setBytes(byte[]) to send raw bytes
     */
    public Response setContent(@NonNull final String content) {
        this.content = content;
        this.isText = true;
        return this;
    }

    /**
     * Set the header file
     *
     * @param header Header file
     */
    public void setHeader(@NonNull final Header header) {
        this.header = header;
    }

    /**
     * Get the response header
     *
     * @return the set response header
     * @see #setHeader(Header) - To set the header
     */
    @Override
    public Header getHeader() {
        return this.header;
    }

    /**
     * Get the content as a string
     *
     * @return The string content
     * @see #isText() Should be true for this to work
     */
    @Override
    public String getContent() {
        return this.content;
    }

    /**
     * Check if using raw bytes, or a string
     *
     * @return True if using String, false if using bytes
     * @see #setContent(String) To set the string content
     * @see #setBytes(byte[]) To set the byte content
     */
    @Override
    public boolean isText() {
        return isText;
    }

    public void setParent(RequestHandler parent) {
        this.parent = parent;
    }

    public boolean hasParent() {
        return this.parent != null;
    }
}
