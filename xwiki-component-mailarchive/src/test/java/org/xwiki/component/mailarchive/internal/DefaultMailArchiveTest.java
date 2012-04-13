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
package org.xwiki.component.mailarchive.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.xwiki.component.mailarchive.MailArchive;
import org.xwiki.component.mailarchive.internal.data.MailServer;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.context.Execution;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;

/**
 * Tests for the {@link MailArchive} component.
 */
public class DefaultMailArchiveTest extends AbstractMockingComponentTestCase
{

    @MockingRequirement
    private DefaultMailArchive ma;

    /**
     * @see org.xwiki.test.AbstractMockingComponentTestCase#configure()
     */
    @Override
    public void configure() throws Exception
    {
        try {
            final Execution execution = getComponentManager().lookup(Execution.class);
            Mockery context = new Mockery();
            context.checking(new Expectations()
            {
                {
                    oneOf(execution).getContext();
                }
            });

        } catch (ComponentLookupException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadExistingTopics() throws ComponentLookupException, Exception
    {

        MailServer server = new MailServer();
        server.setFolder("WIKI");
        server.setHost("imap.gmail.com");
        server.setPort(993);
        server.setProtocol("imaps");
        server.setUser("jbousque");
        server.setPassword("gwada15");
        int result = ma.checkMails(server);
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
