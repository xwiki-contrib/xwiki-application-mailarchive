/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.mailarchive.internal.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.xwiki.component.annotation.Role;

/**
 * @version $Id$
 */
@Role
public interface ITextUtils
{

    /**
     * Assumed maximum length for Large string properties
     */
    public static final int LONG_STRINGS_MAX_LENGTH = 60000;

    /**
     * Assumed maximum length for string properties
     */
    public static final int SHORT_STRINGS_MAX_LENGTH = 255;

    public String htmlToPlainText(final String html);

    public String byte2hex(byte[] b);

    public byte[] hex2byte(String hexStr);

    public byte charToByte(char c);

    public String truncateForLargeString(String s);

    public String truncateForString(String s);

    public String truncateStringForBytes(String s, int maxChars, int maxBytes);

    public boolean similarSubjects(String s1, String s2);

    public double getAveragedLevenshteinDistance(String s, String t);

    public String unzipString(String zippedString) throws IOException, UnsupportedEncodingException;

}
