package org.xwiki.contrib.mailarchive;

import javax.mail.Message;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.contrib.mail.MailItem;

@ComponentRole
public interface MailParser
{

    public MailItem parseMail(Message mail);

}
