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
package org.xwiki.contrib.mailarchive.test.po;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

/**
 * @version $Id$
 */
public class MailArchiveAdminPage extends ViewPage
{
    @FindBy(id = "tabIdMailServersListItem")
    private WebElement tabMailSourcesSwitch;

    private final MailArchiveAdminMailSourcesPane mailSourcesPane = new MailArchiveAdminMailSourcesPane();

    public static MailArchiveAdminPage gotoPage()
    {
        getUtil().gotoPage("MailArchive", "Admin");
        return new MailArchiveAdminPage();
    }

    public String getURL()
    {
        return getUtil().getURL("MailArchive", "Admin");
    }

    public boolean isOnPage()
    {
        return getDriver().getCurrentUrl().equals(getURL());
    }

    public MailArchiveAdminMailSourcesPane openMailSourcesPane()
    {
        this.tabMailSourcesSwitch.click();
        return this.mailSourcesPane;
    }

}
