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
package org.xwiki.contrib.mailarchive.it.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;

import javax.mail.Message;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.contrib.mail.MailContent;
import org.xwiki.contrib.mail.MailItem;
import org.xwiki.contrib.mail.internal.JavamailMessageParser;
import org.xwiki.contrib.mailarchive.it.ITUtils;

/**
 * These tests load and create Message objects by importing .eml files from test resources. Parsing methods are fed with
 * all created Message, not allowing for strict content verification, but only ensuring overall success of parsing
 * methods.
 * 
 * @version $Id$
 */
public class EmailParsingIT
{
    private JavamailMessageParser parser;

    private File dir;

    @Before
    public void setUp()
    {
        Logger logger = LoggerFactory.getLogger("org.xwiki.contrib.test.LoadMailsIT");
        parser = new JavamailMessageParser();

        // Init .eml input files folder
        URL url = Thread.currentThread().getContextClassLoader().getResource("messages/dummy.txt");
        File f;
        try {
            f = new File(url.toURI());
        } catch (URISyntaxException e) {
            f = new File(url.getPath());
        }
        this.dir = f.getParentFile();

    }

    /**
     * Tests parsing of email headers, using messages created from .eml files.
     * 
     * @throws Exception
     */
    @Test
    public void testParseHeaders() throws Exception
    {
        for (File file : this.dir.listFiles(new EmlFilter())) {
            Message message = ITUtils.read("messages/" + file.getName());

            MailItem m = parser.parseHeaders(message);

            System.out.println("PARSING RESULT for " + file.getName() + " [" + m + "]");
            // TODO: these are very minimal checks ...
            assertNotNull("Failed to parse headers for " + file.getName(), m);
        }

    }

    /**
     * Tests parsing of emails content (body), using emails created from .eml files.
     * 
     * @throws Exception
     */
    @Test
    public void testExtractMailContent() throws Exception
    {
        for (File file : this.dir.listFiles(new EmlFilter())) {
            Message message = ITUtils.read("messages/" + file.getName());

            MailContent content = parser.extractMailContent(message);

            System.out.println("PARSING RESULT for " + file.getName() + " [" + content + "]");
            // TODO: these are very minimal checks ...
            assertNotNull("Failed to parse content for " + file.getName(), content);
            assertFalse("No text nor html content for " + file.getName(), StringUtils.isBlank(content.getText())
                && StringUtils.isBlank(content.getHtml()));

        }

    }

    class EmlFilter implements FilenameFilter
    {

        /**
         * {@inheritDoc}
         * 
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        @Override
        public boolean accept(File dir, String name)
        {
            if (name.endsWith(".eml")) {
                return true;
            }
            return false;
        }

    }

}
