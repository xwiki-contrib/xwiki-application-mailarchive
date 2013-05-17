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
package org.xwiki.contrib.mailarchive.test.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.contrib.mailarchive.test.po.MailArchiveAdminMailSourcesPane;
import org.xwiki.contrib.mailarchive.test.po.MailArchiveAdminPage;
import org.xwiki.contrib.mailarchive.test.po.MailArchiveStoreEntryEditPage;
import org.xwiki.test.ui.AbstractAdminAuthenticatedTest;
import org.xwiki.test.ui.po.LiveTableElement;

/**
 * @version $Id$
 */
public class AdminMailSourcesTest extends AbstractAdminAuthenticatedTest
{
    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        getUtil().deleteSpace("MailArchivePrefs");
    }

    public void tearDown() throws Exception
    {
        // remove any prefs pages that could have been created by tests
        getUtil().deleteSpace("MailArchivePrefs");
    }

    @Test
    public void testAddStore()
    {
        MailArchiveAdminPage maAdminPage = MailArchiveAdminPage.gotoPage();
        assertTrue(maAdminPage.isOnPage());
        MailArchiveAdminMailSourcesPane maAdminSourcesPane = maAdminPage.openMailSourcesPane();
        MailArchiveStoreEntryEditPage storeEntryEditPage = maAdminSourcesPane.addStore();
        storeEntryEditPage.setId("test");
        storeEntryEditPage.setFolder("inbox");
        storeEntryEditPage.setFormat("mbox");
        storeEntryEditPage.setLocation("/a/folder/to/a/store");
        storeEntryEditPage.setState("on");
        storeEntryEditPage.submitStoreForm();
        LiveTableElement storeLt = maAdminSourcesPane.getStoreLiveTable();
        assertEquals("1 store should have been added to live table", 1, storeLt.getRowCount());
        assertTrue("correct store item not retrieved from live table", storeLt.hasRow("ID", "test"));
    }
}
