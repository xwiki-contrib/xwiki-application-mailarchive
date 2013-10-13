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
package org.xwiki.contrib.mailarchive.internal.data;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.contrib.mailarchive.IMailMatcher;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * @version $Id$
 */
public class MailMatcher implements IMailMatcher
{
    private List<String> fields = new ArrayList<String>();

    private String expression;

    private boolean advancedMode = false;

    private boolean ignoreCase = true;

    private boolean multiLine = true;

    public MailMatcher()
    {
        super();
    }

    /**
     * @param fields
     * @param expression
     * @param advancedMode
     * @param ignoreCase
     * @param multiLine
     */
    public MailMatcher(final List<String> fields, final String expression, final boolean advancedMode,
        final boolean ignoreCase, final boolean multiLine)
    {
        super();
        this.fields = fields;
        this.expression = expression;
        this.advancedMode = advancedMode;
        this.ignoreCase = ignoreCase;
        this.multiLine = multiLine;
    }

    /**
     * @param fields
     * @param expression
     * @param advancedMode
     */
    public MailMatcher(final List<String> fields, final String expression, final boolean advancedMode)
    {
        super();
        this.fields = fields;
        this.expression = expression;
        this.advancedMode = advancedMode;
    }

    /**
     * @param fields
     * @param expression
     */
    public MailMatcher(final List<String> fields, final String expression)
    {
        super();
        this.fields = fields;
        this.expression = expression;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailMatcher#getFields()
     */
    @Override
    public List<String> getFields()
    {
        return this.fields;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailMatcher#getExpression()
     */
    @Override
    public String getExpression()
    {
        return this.expression;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailMatcher#isAdvancedMode()
     */
    @Override
    public boolean isAdvancedMode()
    {
        return this.advancedMode;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailMatcher#isIgnoreCase()
     */
    @Override
    public boolean isIgnoreCase()
    {
        return this.ignoreCase;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mailarchive.IMailMatcher#isMultiLine()
     */
    @Override
    public boolean isMultiLine()
    {
        return this.multiLine;
    }

    /**
     * @param fields the fields to set
     */
    public void setFields(final List<String> fields)
    {
        this.fields = fields;
    }

    /**
     * @param expression the pattern to set
     */
    public void setExpression(final String expression)
    {
        this.expression = expression;
    }

    /**
     * @param advancedMode the advancedMode to set
     */
    public void setAdvancedMode(final boolean advancedMode)
    {
        this.advancedMode = advancedMode;
    }

    /**
     * @param ignoreCase the ignoreCase to set
     */
    public void setIgnoreCase(final boolean ignoreCase)
    {
        this.ignoreCase = ignoreCase;
    }

    /**
     * @param multiLine the multiLine to set
     */
    public void setMultiLine(final boolean multiLine)
    {
        this.multiLine = multiLine;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        XWikiToStringBuilder builder = new XWikiToStringBuilder(this);
        builder.append("fields", fields);
        builder.append("expression", expression);
        builder.append("advancedMode", advancedMode);
        builder.append("ignoreCase", ignoreCase);
        builder.append("multiLine", multiLine);
        return builder.toString();
    }

}
