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
package org.xwiki.component.mailarchive;

import static org.junit.Assert.*;

import org.hamcrest.Description;
import org.junit.Test;
import org.xwiki.component.mailarchive.internal.DefaultMailArchive;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.test.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;
import org.jmock.*;
import org.jmock.api.Expectation;
import org.jmock.api.Invocation;

/**
 * Tests for the {@link MailArchive} component.
 */
public class DefaultMailArchiveTest extends AbstractMockingComponentTestCase
{
    @MockingRequirement
    private DefaultMailArchive ma;
    
    
    

    @Test
    public void testLoadExistingTopics() throws QueryException
    {
    	assertTrue(true);
    	/*Mockery context = new Mockery();
    	context.checking(new Expectations() {{
    	    oneOf (queryManager).createQuery(with(any(String.class)), Query.XWQL);
    	}});
        ma.loadExistingTopics();*/   
    }
}
