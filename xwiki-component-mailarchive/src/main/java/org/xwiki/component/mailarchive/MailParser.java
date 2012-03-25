package org.xwiki.component.mailarchive;

import javax.mail.Message;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.mailarchive.internal.data.MailItem;

@ComponentRole
public interface MailParser {

	public MailItem parseMail(Message mail);
	
}
