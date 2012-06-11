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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.contrib.mail.MailItem;
import org.xwiki.contrib.mailarchive.IMailArchive;
import org.xwiki.contrib.mailarchive.IType;
import org.xwiki.contrib.mailarchive.internal.data.MailTypeImpl;
import org.xwiki.environment.Environment;
import org.xwiki.environment.internal.ServletEnvironment;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

/**
 * Tests for the {@link IMailArchive} component.
 */
public class DefaultMailArchiveTest extends AbstractBridgedComponentTestCase
{
    private DefaultMailArchive ma;

    private XWiki mockXWiki;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        setupEnvironment();

        final Execution execution = getComponentManager().lookup(Execution.class);
        System.out.println("Execution tu " + execution.hashCode());

        this.mockXWiki = getMockery().mock(XWiki.class);

        getContext().setWiki(this.mockXWiki);

        this.ma = (DefaultMailArchive) getComponentManager().lookup(IMailArchive.class);
    }

    // FIXME: this is supposed to be done by AbstractBridgedComponentTestCase already but it seems to be buggy in 3.5.1.
    // Note: works well in 4.1 so it should be removed when upgrading.
    protected void setupEnvironment() throws Exception
    {
        // Since the oldcore module draws the Servlet Environment in its dependencies we need to ensure it's set up
        // correctly with a Servlet Context.
        ServletEnvironment environment = (ServletEnvironment) getComponentManager().lookup(Environment.class);
        final ServletContext mockServletContext = environment.getServletContext();
        getMockery().checking(new Expectations()
        {
            {
                allowing(mockServletContext).getResourceAsStream("/WEB-INF/cache/infinispan/config.xml");
                will(returnValue(null));
                allowing(mockServletContext).getAttribute("javax.servlet.context.tempdir");
                will(returnValue(new File(System.getProperty("java.io.tmpdir"))));
            }
        });
    }

    @Test
    public void extractTypesWithLimitValues()
    {
        try {
            ma.extractTypes(null, null);
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            List<IType> types = new ArrayList<IType>();
            ma.extractTypes(types, null);
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            MailItem mail = new MailItem();
            ma.extractTypes(null, mail);
        } catch (IllegalArgumentException e) {
            // ok
        }

    }

    public void extractTypesForNominalCases_MailType()
    {
        // Check with a unique "mail" type
        List<IType> types = new ArrayList<IType>();

        MailTypeImpl typeMail = new MailTypeImpl();
        typeMail.setName(IType.TYPE_MAIL);
        typeMail.setDisplayName("Mail");
        HashMap<List<String>, String> patterns = new HashMap<List<String>, String>();
        List<String> fields = new ArrayList<String>();
        fields.add("subject");
        patterns.put(fields, "^.*$");
        typeMail.setPatterns(patterns);
        typeMail.setIcon("email");
        types.add(typeMail);

        MailItem m = new MailItem();
        m.setSubject("lorem ipsum");

        List<IType> foundTypes;
        foundTypes = ma.extractTypes(types, m);
        assertNotNull(foundTypes);
        assertEquals(1, foundTypes.size());
        assertEquals(typeMail, foundTypes.get(0));

    }

    @Test
    public void extractTypesForNominalCases_OtherType()
    {
        // Check with a unique "mail" type
        List<IType> types = new ArrayList<IType>();

        MailTypeImpl typeProposal = new MailTypeImpl();
        typeProposal.setName("proposal");
        typeProposal.setDisplayName("Proposal");
        HashMap<List<String>, String> patterns = new HashMap<List<String>, String>();
        List<String> fields = new ArrayList<String>();
        fields.add("subject");
        patterns.put(fields, "(?mi)^.*\\[proposal\\].*$");
        typeProposal.setPatterns(patterns);
        typeProposal.setIcon("proposal");
        types.add(typeProposal);

        List<IType> foundTypes;

        // Non-matching test
        MailItem m = new MailItem();
        m.setSubject("lorem ipsum");

        foundTypes = ma.extractTypes(types, m);
        assertNotNull(foundTypes);
        assertEquals(0, foundTypes.size());

        // Matching test
        m.setSubject("[xwiki-user][Proposal] Add more unitary tests");
        foundTypes = ma.extractTypes(types, m);
        assertNotNull(foundTypes);
        assertEquals(1, foundTypes.size());
        assertEquals(typeProposal, foundTypes.get(0));

        // With pattern at line start - non-matching test
        patterns.put(fields, "(?mi)^\\[proposal\\].*$");
        foundTypes = ma.extractTypes(types, m);
        assertNotNull(foundTypes);
        assertEquals(0, foundTypes.size());

        // With pattern at line start - matching test
        m.setSubject("[prOposaL] Too low");

        foundTypes = ma.extractTypes(types, m);
        assertNotNull(foundTypes);
        assertEquals(1, foundTypes.size());
        assertEquals(typeProposal, foundTypes.get(0));

    }

    @Test
    public void extractTypesForMultiplePatternsAndTypes()
    {
        // Check with a unique "mail" type
        List<IType> types = new ArrayList<IType>();

        // First setup a type "proposal" that matches subject field
        MailTypeImpl typeProposal = new MailTypeImpl();
        typeProposal.setName("proposal");
        typeProposal.setDisplayName("Proposal");
        HashMap<List<String>, String> patterns = new HashMap<List<String>, String>();
        List<String> fields = new ArrayList<String>();
        fields.add("subject");
        patterns.put(fields, "(?mi)^.*\\[proposal\\].*$");
        typeProposal.setPatterns(patterns);
        typeProposal.setIcon("proposal");
        types.add(typeProposal);

        // Type release : matches subject for a token, and a specific originating from
        MailTypeImpl typeRelease = new MailTypeImpl();
        typeRelease.setName("release");
        typeRelease.setDisplayName("Release");
        HashMap<List<String>, String> patternsRelease = new HashMap<List<String>, String>();
        List<String> fieldsReleaseSubject = new ArrayList<String>();
        fieldsReleaseSubject.add("subject");
        patternsRelease.put(fieldsReleaseSubject, "(?mi)^\\[release\\].*$");
        List<String> fieldsReleaseFrom = new ArrayList<String>();
        fieldsReleaseFrom.add("from");
        patternsRelease.put(fieldsReleaseFrom, "(?mi)^.*vmassol.*$");
        typeRelease.setPatterns(patternsRelease);
        typeRelease.setIcon("release");
        types.add(typeRelease);

        List<IType> foundTypes;

        // Non-matching test
        MailItem m = new MailItem();
        m.setSubject("lorem ipsum");
        m.setFrom("toto");

        foundTypes = ma.extractTypes(types, m);
        assertNotNull(foundTypes);
        assertEquals(0, foundTypes.size());

        // Match only proposal type
        m.setSubject("[Proposal] This is a proposal");
        m.setFrom("vmassol");
        foundTypes = ma.extractTypes(types, m);
        assertNotNull(foundTypes);
        assertEquals(1, foundTypes.size());
        assertEquals(typeProposal, foundTypes.get(0));

        // Match only release type
        m.setSubject("[Release] This is a release");
        m.setFrom("Vincent Massol <vmassol@mailarchive.net");
        foundTypes = ma.extractTypes(types, m);
        assertNotNull(foundTypes);
        assertEquals(1, foundTypes.size());
        assertEquals(typeRelease, foundTypes.get(0));

        // Match both types
        m.setSubject("[Release] [PROPOSAL] A new released proposal, whatever it could mean");
        m.setFrom("vmassol@xwiki.xwiki");
        foundTypes = ma.extractTypes(types, m);
        assertEquals(2, foundTypes.size());
        if ((!typeProposal.equals(foundTypes.get(0)) && !typeRelease.equals(foundTypes.get(0)))
            || (!typeProposal.equals(foundTypes.get(1)) && !typeRelease.equals(foundTypes.get(1)))) {
            fail("Invalid types found");
        }
    }

}
