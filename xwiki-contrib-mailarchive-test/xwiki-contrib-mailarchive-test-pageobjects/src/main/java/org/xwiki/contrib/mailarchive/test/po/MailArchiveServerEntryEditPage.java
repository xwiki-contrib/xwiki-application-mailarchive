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
public class MailArchiveServerEntryEditPage extends InlinePage
{
    private static final String CLASS_PREFIX = "MailArchiveCode.ServerSettingsClass_0_";

    @FindBy(id = CLASS_PREFIX + "id")
    private WebElement idElement;

    @FindBy(id = CLASS_PREFIX + "hostname")
    private WebElement hostnameElement;

    @FindBy(id = CLASS_PREFIX + "port")
    private WebElement portElement;

    @FindBy(id = CLASS_PREFIX + "protocol")
    private WebElement protocolElement;

    @FindBy(id = CLASS_PREFIX + "user")
    private WebElement userElement;

    @FindBy(id = CLASS_PREFIX + "password")
    private WebElement passwordElement;

    @FindBy(id = CLASS_PREFIX + "folder")
    private WebElement folderElement;

    @FindBy(id = CLASS_PREFIX + "additionalProperties")
    private WebElement additionalPropertiesElement;

    @FindBy(id = CLASS_PREFIX + "state")
    private WebElement stateElement;

    @FindBy(name = "save")
    private WebElement saveButton;

    public void setId(final String id)
    {
        this.idElement.sendKeys(id);
    }

    public void setHostname(final String hostname)
    {
        this.hostnameElement.sendKeys(hostname);
    }

    public void setPort(final String port)
    {
        this.portElement.sendKeys(port);
    }

    public void setProtocol(final String protocol)
    {
        this.protocolElement.sendKeys(protocol);
    }

    public void setUser(final String user)
    {
        this.userElement.sendKeys(user);
    }

    public void setPassword(final String password)
    {
        this.passwordElement.sendKeys(password);
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

    public void submitForm()
    {
        this.saveButton.click();
    }
}
