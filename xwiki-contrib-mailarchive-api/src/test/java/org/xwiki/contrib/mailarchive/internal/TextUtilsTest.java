package org.xwiki.contrib.mailarchive.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.utils.ITextUtils;
import org.xwiki.contrib.mailarchive.internal.utils.TextUtils;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
@ComponentList({TextUtils.class})
public class TextUtilsTest
{

    @Rule
    public final MockitoComponentMockingRule<ITextUtils> mocker = new MockitoComponentMockingRule<ITextUtils>(
        TextUtils.class);

    private ITextUtils textUtils;

    @Before
    public void setUp() throws ComponentLookupException
    {
        this.textUtils = mocker.getComponentUnderTest();
    }

    @Test
    public void testGetLevenshteinDistance() throws InitializationException, MailArchiveException, XWikiException
    {
        assertEquals(0, textUtils.getAveragedLevenshteinDistance("toto", "toto"), 0);
        assertEquals(0.25, textUtils.getAveragedLevenshteinDistance("toto", "tito"), 0);
        assertEquals(1, textUtils.getAveragedLevenshteinDistance("toto", "uiui"), 0);
    }
}
