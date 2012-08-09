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
package org.xwiki.contrib.test;

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
import org.xwiki.contrib.mail.internal.JavamailMessageParser;

/**
 * @version $Id$
 */
public class LoadMailsIT
{
    private JavamailMessageParser parser;

    @Before
    public void setUp()
    {
        Logger logger = LoggerFactory.getLogger("org.xwiki.contrib.test.LoadMailsIT");
        parser = new JavamailMessageParser(logger);

    }

    @Test
    public void testExtractMailContent() throws Exception
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource("messages/dummy.txt");
        File f;
        try {
            f = new File(url.toURI());
        } catch (URISyntaxException e) {
            f = new File(url.getPath());
        }
        File dir = f.getParentFile();

        for (File file : dir.listFiles(new EmlFilter())) {
            Message message = ReadEmailFromFile.read("messages/" + file.getName());

            MailContent content = parser.extractMailContent(message);

            System.out.println("PARSING RESULT for " + file.getName() + " [" + content + "]");
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
