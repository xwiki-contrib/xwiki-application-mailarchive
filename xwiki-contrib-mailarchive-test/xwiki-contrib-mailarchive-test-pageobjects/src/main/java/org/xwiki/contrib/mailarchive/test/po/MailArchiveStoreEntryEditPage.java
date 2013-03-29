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
import org.xwiki.test.ui.po.InlinePage;

/**
 * @version $Id$
 */
public class MailArchiveStoreEntryEditPage extends InlinePage
{
    @FindBy(id = "MailArchiveCode.StoreClass_0_id")
    private WebElement idElement;

    @FindBy(id = "MailArchiveCode.StoreClass_0_format")
    private WebElement formatElement;

    @FindBy(id = "MailArchiveCode.StoreClass_0_location")
    private WebElement locationElement;

    @FindBy(id = "MailArchiveCode.StoreClass_0_folder")
    private WebElement folderElement;

    @FindBy(id = "MailArchiveCode.StoreClass_0_additionalProperties")
    private WebElement additionalPropertiesElement;

    @FindBy(id = "MailArchiveCode.StoreClass_0_state")
    private WebElement stateElement;

    @FindBy(name = "save")
    private WebElement saveButton;

    public void setId(final String id)
    {
        this.idElement.sendKeys(id);
    }

    public void setFormat(final String format)
    {
        this.formatElement.sendKeys(format);
    }

    public void setLocation(final String location)
    {
        this.locationElement.sendKeys(location);
    }

    public void setFolder(final String folder)
    {
        this.folderElement.sendKeys(folder);
    }

    public void setAdditionalProperties(final String additionalProperties)
    {
        this.additionalPropertiesElement.sendKeys(additionalProperties);
    }

    public void setState(final String state)
    {
        this.stateElement.sendKeys(state);
    }

    public void submitStoreForm()
    {
        this.saveButton.click();
    }
}
