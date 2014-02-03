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
package org.xwiki.contrib.mailarchive.utils;

import java.lang.reflect.Type;
import java.util.Set;

import org.xwiki.component.annotation.Role;
import org.xwiki.logging.LogLevel;

/**
 * @version $Id$
 */
@Role
public interface IAggregatedLoggerManager
{
    /**
     * Sets new log level for all managed loggers, and keeps aside previous log level of each.
     * 
     * @param logLevel
     */
    void pushLogLevel(LogLevel logLevel);

    /**
     * Restores loggers log level to their state, previous to last call to pushLogLevel.
     */
    void popLogLevel();

    /**
     * Adds a logger to manage levels in an aggregated way.
     * 
     * @param loggerName
     */
    void addLogger(String loggerName);

    /**
     * Removes a logger.
     * 
     * @param loggerName
     */
    void removeLogger(String loggerName);

    /**
     * Sets the list of loggers to manage.
     * 
     * @param loggerNames
     */
    void setLoggers(Set<String> loggers);

    /**
     * Gets the list of managed loggers.
     * 
     * @return
     */
    Set<String> getLoggers();

    /**
     * Empties the list of managed loggers.
     */
    void cleanLoggers();

    void addComponentLogger(Type roleType);
}
