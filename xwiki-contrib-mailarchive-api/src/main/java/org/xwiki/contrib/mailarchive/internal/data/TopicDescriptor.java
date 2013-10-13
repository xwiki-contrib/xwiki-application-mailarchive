package org.xwiki.contrib.mailarchive.internal.data;

import org.xwiki.text.XWikiToStringBuilder;

public class TopicDescriptor
{

    private String fullName;

    private String subject;

    public TopicDescriptor(String fullName, String subject)
    {
        super();
        this.fullName = fullName;
        this.subject = subject;
    }

    public String getFullName()
    {
        return fullName;
    }

    public String getSubject()
    {
        return subject;
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
        builder.append("fullName", fullName);
        builder.append("subject", subject);
        return builder.toString();
    }

}
