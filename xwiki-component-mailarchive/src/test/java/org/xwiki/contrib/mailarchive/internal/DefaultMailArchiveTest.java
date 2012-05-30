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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.mailarchive.MailArchive;
import org.xwiki.contrib.mailarchive.internal.DefaultMailArchive;
import org.xwiki.contrib.mailarchive.internal.data.MailServerImpl;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;

/**
 * Tests for the {@link MailArchive} component.
 */
public class DefaultMailArchiveTest extends AbstractMockingComponentTestCase
{

    @MockingRequirement
    private DefaultMailArchive ma;

    Mockery context = new Mockery();

    /**
     * @see org.xwiki.test.AbstractMockingComponentTestCase#configure()
     */
    @Override
    public void configure() throws Exception
    {
        final Execution execution = getComponentManager().lookup(Execution.class);
        System.out.println("Execution tu " + execution.hashCode());

        context.checking(new Expectations()
        {
            {
                allowing(execution).getContext();
                will(returnValue(new ExecutionContext()));
            }
        });

    }

    @Test
    public void testLoadExistingTopics() throws ComponentLookupException, Exception
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
        assertEquals(0, ma.getLevenshteinDistance("toto", "toto"), 0);
        assertEquals(0.25, ma.getLevenshteinDistance("toto", "tito"), 0);
        assertEquals(1, ma.getLevenshteinDistance("toto", "uiui"), 0);

    }
}
