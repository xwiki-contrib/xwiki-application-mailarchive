package org.xwiki.contrib.mailarchive.internal.data;

import org.xwiki.text.XWikiToStringBuilder;

public class MailDescriptor
{

    private String subject;

    private String topicId;

    private String fullName;

    public MailDescriptor(String subject, String topicId, String fullName)
    {
        super();
        this.subject = subject;
        this.topicId = topicId;
        this.fullName = fullName;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getTopicId()
    {
        return topicId;
    }

    public String getFullName()
    {
        return fullName;
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
        builder.append("subject", subject);
        builder.append("topicId", topicId);
        builder.append("fullName", fullName);
        return builder.toString();
    }

}
