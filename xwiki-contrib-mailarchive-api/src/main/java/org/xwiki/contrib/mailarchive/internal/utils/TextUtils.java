package org.xwiki.contrib.mailarchive.internal.utils;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 * @author jbousque
 */
public class TextUtils
{

    /**
     * Assumed maximum length for Large string properties
     */
    public static final int LONG_STRINGS_MAX_LENGTH = 60000;

    /**
     * Assumed maximum length for string properties
     */
    public static final int SHORT_STRINGS_MAX_LENGTH = 255;

    private static Logger logger;

    private TextUtils()
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
    public static double getAveragedLevenshteinDistance(String s, String t)
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
    public static boolean similarSubjects(String s1, String s2)
    {
        logger.debug("similarSubjects : comparing [" + s1 + "] and [" + s2 + "]");
        s1 = s1.replaceAll("^([Rr][Ee]:|[Ff][Ww]:)(.*)$", "$2");
        s2 = s2.replaceAll("^([Rr][Ee]:|[Ff][Ww]:)(.*)$", "$2");
        logger.debug("similarSubjects : comparing [" + s1 + "] and [" + s2 + "]");
        if (s1 == s2) {
            logger.debug("similarSubjects : subjects are equal");
            return true;
        }
        if (s1 != null && s1.equals(s2)) {
            logger.debug("similarSubjects : subjects are the equal");
            return true;
        }
        if (s1.length() == 0 || s2.length() == 0) {
            logger.debug("similarSubjects : one subject is empty, we consider them different");
            return false;
        }
        try {
            double d = TextUtils.getAveragedLevenshteinDistance(s1, s2);
            logger.debug("similarSubjects : Levenshtein distance d=" + d);
            if (d <= 0.25) {
                logger.debug("similarSubjects : subjects are considered similar because d <= 0.25");
                return true;
            }
        } catch (IllegalArgumentException iaE) {
            return false;
        }
        if ((s1.startsWith(s2) || s2.startsWith(s1))) {
            logger.debug("similarSubjects : subjects are considered similar because one start with the other");
            return true;
        }
        return false;
    }

    // Truncate a string "s" to obtain less than a certain number of bytes "maxBytes", starting with "maxChars"
    // characters.
    public static String truncateStringForBytes(String s, int maxChars, int maxBytes)
    {

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

    public static String truncateForString(String s)
    {
        if (s.length() > SHORT_STRINGS_MAX_LENGTH) {
            return s.substring(0, SHORT_STRINGS_MAX_LENGTH - 1);
        }
        return s;
    }

    public static String truncateForLargeString(String s)
    {
        if (s.length() > LONG_STRINGS_MAX_LENGTH) {
            return s.substring(0, LONG_STRINGS_MAX_LENGTH - 1);
        }
        return s;
    }

    // FIXME: find equivalent methods in xwiki utilities libraries
    public static byte charToByte(char c)
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
    public static byte[] hex2byte(String hexStr)
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
    public static String byte2hex(byte[] b)
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
}
