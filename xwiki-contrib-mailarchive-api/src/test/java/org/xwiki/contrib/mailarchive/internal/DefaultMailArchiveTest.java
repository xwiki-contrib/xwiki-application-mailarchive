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

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.contrib.mailarchive.IMailArchive;
import org.xwiki.logging.LoggerManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

/**
 * Tests for the {@link IMailArchive} component.
 */
public class DefaultMailArchiveTest extends AbstractBridgedComponentTestCase
{
    private DefaultMailArchive ma;

    private XWiki mockXWiki;

    private Execution execution;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        // setupEnvironment();

        execution = getComponentManager().getInstance(Execution.class);
        System.out.println("Execution tu " + execution.hashCode());

        this.mockXWiki = getMockery().mock(XWiki.class);

        final XWikiDocument mockPrefsPage = getMockery().mock(XWikiDocument.class);
        getMockery().checking(new Expectations()
        {
            {
                allowing(mockXWiki).exists("MailArchivePrefs.GlobalParameters", getContext());
                will(returnValue(true));
                allowing(mockXWiki).getDocument("MailArchivePrefs.GlobalParameters", getContext());
                will(returnValue(mockPrefsPage));
            }
        });

        getContext().setWiki(this.mockXWiki);

        // Needed because not mocked by MockingComponentManager
        getComponentManager().registerMockComponent(getMockery(), LoggerManager.class);

        this.ma = (DefaultMailArchive) getComponentManager().getInstance(IMailArchive.class);

    }

    @Test
    public void testEnvironment()
    {
        // nothing to do, only validates setup and so component architecture
    }

}
