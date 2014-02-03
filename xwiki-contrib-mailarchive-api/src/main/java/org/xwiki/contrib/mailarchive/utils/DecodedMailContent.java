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
package org.xwiki.contrib.mailarchive.utils;

/**
 * @version $Id$
 */
public class DecodedMailContent
{
    private boolean html;

    private String text;

    /**
     * @param html
     * @param text
     */
    public DecodedMailContent(final boolean isHtml, final String text)
    {
        super();
        this.html = isHtml;
        this.text = text;
    }

    /**
     * @return the html
     */
    public boolean isHtml()
    {
        return html;
    }

    /**
     * @param html the html to set
     */
    public void setHtml(final boolean html)
    {
        this.html = html;
    }

    /**
     * @return the text
     */
    public String getText()
    {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(final String text)
    {
        this.text = text;
    }

}
