package org.xwiki.contrib.mailarchive.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
import org.xwiki.contrib.mailarchive.internal.utils.TextUtils;

import com.xpn.xwiki.XWikiException;

public class MailArchiveStringUtilsTest {
	
	
	@Before
	public void setUp() {
		Logger logger = LoggerFactory.getLogger("org.xwiki.contrib.mailarchive.internal.MailArchiveStringUtilsTest");
		TextUtils.setLogger(logger);
	}
	
    // FIXME : this is a unit test of MailUtils class
    @Test
    public void testGetLevenshteinDistance() throws InitializationException, MailArchiveException, XWikiException
    {   	
        assertEquals(0, TextUtils.getAveragedLevenshteinDistance("toto", "toto"), 0);
        assertEquals(0.25, TextUtils.getAveragedLevenshteinDistance("toto", "tito"), 0);
        assertEquals(1, TextUtils.getAveragedLevenshteinDistance("toto", "uiui"), 0);
    }
}
