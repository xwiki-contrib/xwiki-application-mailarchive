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
package org.xwiki.contrib.mailarchive.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.servlet.ServletContext;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.context.Execution;
import org.xwiki.contrib.mailarchive.IMailArchive;
import org.xwiki.contrib.mailarchive.internal.data.MailServerImpl;
import org.xwiki.environment.Environment;
import org.xwiki.environment.internal.ServletEnvironment;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

/**
 * Tests for the {@link IMailArchive} component.
 */
public class DefaultMailArchiveTest extends AbstractBridgedComponentTestCase
{
    private DefaultMailArchive ma;

    private XWiki mockXWiki;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        setupEnvironment();
        
        final Execution execution = getComponentManager().lookup(Execution.class);
        System.out.println("Execution tu " + execution.hashCode());

        this.mockXWiki = getMockery().mock(XWiki.class);

        getContext().setWiki(this.mockXWiki);

        this.ma = (DefaultMailArchive) getComponentManager().lookup(IMailArchive.class);
    }

    // FIXME: this is supposed to be done by AbstractBridgedComponentTestCase already but it seems to be buggy in 3.5.1.
    // Note: works well in 4.1 so it should be removed when upgrading.
    protected void setupEnvironment() throws Exception
    {
        // Since the oldcore module draws the Servlet Environment in its dependencies we need to ensure it's set up
        // correctly with a Servlet Context.
        ServletEnvironment environment = (ServletEnvironment) getComponentManager().lookup(Environment.class);
        final ServletContext mockServletContext = environment.getServletContext();
        getMockery().checking(new Expectations()
        {
            {
                allowing(mockServletContext).getResourceAsStream("/WEB-INF/cache/infinispan/config.xml");
                will(returnValue(null));
                allowing(mockServletContext).getAttribute("javax.servlet.context.tempdir");
                will(returnValue(new File(System.getProperty("java.io.tmpdir"))));
            }
        });
    }

    @Test
    public void testLoadExistingTopics() throws Exception
    {
        MailServerImpl server = new MailServerImpl();
        server.setFolder("WIKI");
        server.setHost("imap.gmail.com");
        server.setPort(993);
        server.setProtocol("imaps");
        server.setUser("jbousque");
        server.setPassword("gwada15");
        int result = ma.queryServerInfo(server);
        System.out.println(result);
        assertTrue(result > 0);
    }

    @Test
    public void testGetLevenshteinDistance()
    {
        assertEquals(0, ma.mailutils.getAveragedLevenshteinDistance("toto", "toto"), 0);
        assertEquals(0.25, ma.mailutils.getAveragedLevenshteinDistance("toto", "tito"), 0);
        assertEquals(1, ma.mailutils.getAveragedLevenshteinDistance("toto", "uiui"), 0);
    }
}
