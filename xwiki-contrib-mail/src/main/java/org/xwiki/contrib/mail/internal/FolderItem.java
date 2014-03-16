package org.xwiki.contrib.mail.internal;

import org.xwiki.text.XWikiToStringBuilder;

/**
 * 
 * 
 * @version $Id$
 */
public class FolderItem
{
    private String name;
    
    private String fullName;
    
    private int index;
    
    private int level;
    
    private int messageCount;
    
    private int unreadMessageCount;
    
    private int newMessageCount;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }
    
    

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public int getMessageCount()
    {
        return messageCount;
    }

    public void setMessageCount(int messageCount)
    {
        this.messageCount = messageCount;
    }

    public int getUnreadMessageCount()
    {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount)
    {
        this.unreadMessageCount = unreadMessageCount;
    }

    public int getNewMessageCount()
    {
        return newMessageCount;
    }

    public void setNewMessageCount(int newMessageCount)
    {
        this.newMessageCount = newMessageCount;
    }

    @Override
    public String toString()
    {
        XWikiToStringBuilder builder = new XWikiToStringBuilder(this);
        if (name != null)
            builder.append("name", name);
        if (fullName != null)
            builder.append("fullName", fullName);
        builder.append("index", index);
        builder.append("level", level);
        builder.append("messageCount", messageCount);
        builder.append("unreadMessageCount", unreadMessageCount);
        builder.append("newMessageCount", newMessageCount);
        return builder.toString();
    }
    
    

}
