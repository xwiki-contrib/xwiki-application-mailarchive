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
package org.xwiki.contrib.mailarchive;

import java.util.List;

/**
 * @version $Id$
 */
public interface IMailMatcher
{
    /**
     * The list of fields to match pattern against.
     * 
     * @return
     */
    List<String> getFields();

    /**
     * The pattern to match.
     * 
     * @return
     */
    String getExpression();

    /**
     * Mode for matching pattern against fields. If "true", then pattern is considered to be a regular expression. If
     * "false", then pattern is matched if field contains the pattern.
     * 
     * @return
     */
    boolean isAdvancedMode();

    /**
     * If the pattern should be matched against the fields values ignoring case or not. Default is true.
     * 
     * @return
     */
    boolean isIgnoreCase();

    /**
     * If the pattern should be matched against multiple lines, or is defined for each single line. Default is true.
     * 
     * @return
     */
    boolean isMultiLine();

}
