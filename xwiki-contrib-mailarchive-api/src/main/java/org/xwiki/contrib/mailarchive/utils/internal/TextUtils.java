package org.xwiki.contrib.mailarchive.utils.internal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.mailarchive.utils.ITextUtils;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * @author jbousque
 */
@Component
@Singleton
public class TextUtils implements ITextUtils
{

    /**
     * The component used to parse XHTML obtained after cleaning, when transformations are not executed.
     */
    @Inject
    @Named("xhtml/1.0")
    private StreamParser htmlStreamParser;

    @Inject
    @Named("plain/1.0")
    private PrintRendererFactory printRendererFactory;

    private static Logger logger;

    public TextUtils()
    {
    }

    public Logger getLogger()
    {
        return logger;
    }

    public static void setLogger(final Logger loggr)
    {
        logger = loggr;
    }

    /**
     * Returns the Levenshtein distance between two strings, averaged by the length of the largest string provided, in
     * order to return a value n so that 0 < n < 1.
     * 
     * @param s
     * @param t
     * @return
     */
    @Override
    public double getAveragedLevenshteinDistance(final String s, final String t)
    {
        return (double) (StringUtils.getLevenshteinDistance(s, t)) / ((double) Math.max(s.length(), t.length()));
    }

    /**
     * Compare 2 strings for similarity Returns true if strings can be considered similar enough<br/>
     * - s1 and s2 have a levenshtein distance < 25% <br/>
     * - s1 or s2 begins with s2 or s1 respectively
     * 
     * @param defaultMailArchive TODO
     * @param s1
     * @param s2
     * @return
     */
    @Override
    public boolean similarSubjects(final String s1, final String s2)
    {
        logger.debug("similarSubjects : comparing [" + s1 + "] and [" + s2 + "]");
        String s1Replaced = s1.replaceAll("^([Rr][Ee]:|[Ff][Ww]:)(.*)$", "$2");
        String s2Replaced = s2.replaceAll("^([Rr][Ee]:|[Ff][Ww]:)(.*)$", "$2");
        logger.debug("similarSubjects : comparing [" + s1 + "] and [" + s2 + "]");
        if (s1Replaced == s2Replaced) {
            logger.debug("similarSubjects : subjects are equal");
            return true;
        }
        if (s1Replaced != null && s1Replaced.equals(s2Replaced)) {
            logger.debug("similarSubjects : subjects are the equal");
            return true;
        }
        if (s1Replaced.length() == 0 || s2Replaced.length() == 0) {
            logger.debug("similarSubjects : one subject is empty, we consider them different");
            return false;
        }
        try {
            double d = getAveragedLevenshteinDistance(s1Replaced, s2Replaced);
            logger.debug("similarSubjects : Levenshtein distance d=" + d);
            if (d <= 0.25) {
                logger.debug("similarSubjects : subjects are considered similar because d <= 0.25");
                return true;
            }
        } catch (IllegalArgumentException iaE) {
            return false;
        }
        if ((s1Replaced.startsWith(s2Replaced) || s2Replaced.startsWith(s1Replaced))) {
            logger.debug("similarSubjects : subjects are considered similar because one start with the other");
            return true;
        }
        return false;
    }

    // Truncate a string "s" to obtain less than a certain number of bytes "maxBytes", starting with "maxChars"
    // characters.
    @Override
    public String truncateStringForBytes(final String s, final int maxChars, final int maxBytes)
    {
        if (StringUtils.isEmpty(s)) {
            return "";
        }

        String substring = s;
        if (s.length() > maxChars) {
            substring = s.substring(0, maxChars);
        }

        byte[] bytes = new byte[] {};
        try {
            bytes = substring.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (bytes.length > maxBytes) {

            logger.debug("Truncate string to " + substring.length() + " characters, result in " + bytes.length
                + " bytes array");
            return truncateStringForBytes(s, maxChars - (bytes.length - maxChars) / 4, maxBytes);

        } else {

            logger.debug("String truncated to " + substring.length() + " characters, resulting in " + bytes.length
                + " bytes array");
            return substring;
        }

    }

    @Override
    public String truncateForString(final String s)
    {
        if (s.length() > SHORT_STRINGS_MAX_LENGTH) {
            return s.substring(0, SHORT_STRINGS_MAX_LENGTH - 1);
        }
        return s;
    }

    @Override
    public String truncateForLargeString(final String s)
    {
        if (s.length() > LONG_STRINGS_MAX_LENGTH) {
            return s.substring(0, LONG_STRINGS_MAX_LENGTH - 1);
        }
        return s;
    }

    // FIXME: find equivalent methods in xwiki utilities libraries
    @Override
    public byte charToByte(final char c)
    {
        return (byte) "0123456789ABCDEF".indexOf("" + c);
    }

    // FIXME: find equivalent methods in xwiki utilities libraries
    /**
     * BD : Used to transfer hex string into byte array. two hex string combines one byte. So that means the length of
     * hex string should be even. Or the null will be returned.
     * 
     * @param hexStr
     * @return
     */
    @Override
    public byte[] hex2byte(final String hexStr)
    {
        if (hexStr == null || hexStr.isEmpty() || (hexStr.length() % 2 > 1)) {
            return null;
        }
        String hexStrUp = hexStr.toUpperCase();
        int length = hexStrUp.length() / 2;
        char[] hexChars = hexStrUp.toCharArray();
        byte[] resultByte = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            resultByte[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return resultByte;
    }

    // FIXME: find equivalent methods in xwiki utilities libraries
    /**
     * BD : Used to transfer byte array into hex string.
     * 
     * @param b
     * @return
     */
    @Override
    public String byte2hex(final byte[] b)
    {
        StringBuffer hexStr = new StringBuffer("");
        String stmp = "";
        for (byte aB : b) {
            stmp = (Integer.toHexString(aB & 0xFF));
            if (stmp.length() == 1) {
                hexStr.append("0" + stmp);
            } else {
                hexStr.append(stmp);
            }
        }
        return hexStr.toString().toUpperCase();
    }

    @Override
    public String htmlToPlainText(final String htmlcontent)
    {
        String converted = null;
        try {

            WikiPrinter printer = new DefaultWikiPrinter();
            htmlStreamParser.parse(new StringReader(htmlcontent), printRendererFactory.createRenderer(printer));

            converted = printer.toString();

        } catch (Throwable t) {
            logger.warn("Conversion from HTML to plain text thrown exception", t);
            converted = null;
        }

        return converted;
    }

    @Override
    public String unzipString(final String zippedString) throws IOException, UnsupportedEncodingException
    {
        String html;
        InputStream is = new ByteArrayInputStream(hex2byte(zippedString));
        GZIPInputStream zis = new GZIPInputStream(is);
        html = "";
        if (zis != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(zis, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                zis.close();
            }
            html = sb.toString();
        }
        return html;
    }

}
