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
package org.xwiki.contrib.mail.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.Part;

import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.contrib.mail.MailContent;

/**
 * @version $Id$
 */
public class JavamailMessageParserTest
{
    final static private Logger logger = LoggerFactory.getLogger(JavamailMessageParserTest.class);

    private JavamailMessageParser parser;

    private Mockery context;

    private Part message;

    @Before
    public void setUp()
    {
        parser = new JavamailMessageParser(logger);
        context = new Mockery();
        message = context.mock(Part.class);
        parser = new JavamailMessageParser(logger);
    }

    /**
     * Tests parsing of mail content. Content-Type: text/plain
     * 
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     * @throws IOException
     */
    @Test
    public void extractPartsContentForSingleTextPart() throws UnsupportedEncodingException, MessagingException,
        IOException
    {
        context.checking(new Expectations()
        {
            {
                oneOf(message).getContent();
                will(returnValue("plain text content"));
                allowing(message).getContentType();
                will(returnValue("text/plain blabla"));
                allowing(message).getFileName();
                will(returnValue(""));
                allowing(message).isMimeType("text/plain");
                will(returnValue(true));
                allowing(message).isMimeType(with(any(String.class)));
                will(returnValue(false));
            }
        });
        MailContent content = parser.extractPartsContent(message);
        assertEquals("Content does not match", "plain text content", content.getText());
        assertTrue(StringUtils.isBlank(content.getHtml()));
        assertNotNull(content.getAttachedMails());
        assertEquals(0, content.getAttachedMails().size());
        assertNotNull(content.getAttachments());
        assertEquals(0, content.getAttachments().size());
        assertNotNull(content.getWikiAttachments());
        assertEquals(0, content.getWikiAttachments().size());
    }

    /**
     * Tests parsing of mail content. Content-Type: text/html
     * 
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     * @throws IOException
     */
    @Test
    public void extractPartsContentForSingleHtmlPart() throws UnsupportedEncodingException, MessagingException,
        IOException
    {
        context.checking(new Expectations()
        {
            {
                oneOf(message).getContent();
                will(returnValue("<span class=\"content\">html content</span>\nhello."));
                allowing(message).getContentType();
                will(returnValue("text/html blabla"));
                allowing(message).getFileName();
                will(returnValue(""));
                allowing(message).isMimeType("text/html");
                will(returnValue(true));
                allowing(message).isMimeType(with(any(String.class)));
                will(returnValue(false));
            }
        });
        MailContent content = parser.extractPartsContent(message);
        assertEquals("Content does not match", "<span class=\"content\">html content</span>\nhello.", content.getHtml());
        assertTrue(StringUtils.isBlank(content.getText()));
        assertNotNull(content.getAttachedMails());
        assertEquals(0, content.getAttachedMails().size());
        assertNotNull(content.getAttachments());
        assertEquals(0, content.getAttachments().size());
        assertNotNull(content.getWikiAttachments());
        assertEquals(0, content.getWikiAttachments().size());
    }
}
