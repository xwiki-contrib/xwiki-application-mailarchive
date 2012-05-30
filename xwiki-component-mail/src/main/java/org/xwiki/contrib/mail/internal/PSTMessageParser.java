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
package org.xwiki.contrib.mail.internal;

import java.util.HashMap;

import org.xwiki.contrib.mail.MailItem;

import com.pff.PSTMessage;

/**
 * @version $Id$
 */
public class PSTMessageParser implements IMessageParser<PSTMessage>
{
    public static final HashMap<Integer, String> IMPORTANCES = new HashMap<Integer, String>();

    public static final HashMap<Integer, String> SENSITIVITIES = new HashMap<Integer, String>();

    public PSTMessageParser()
    {
        IMPORTANCES.put(PSTMessage.IMPORTANCE_NORMAL, "normal");
        IMPORTANCES.put(PSTMessage.IMPORTANCE_HIGH, "high");
        IMPORTANCES.put(PSTMessage.IMPORTANCE_LOW, "low");
        SENSITIVITIES.put(0, "");
        SENSITIVITIES.put(1, "Personal");
        SENSITIVITIES.put(2, "Private");
        SENSITIVITIES.put(3, "Company-Confidential");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.mail.internal.IMessageParser#parseHeaders(java.lang.Object)
     */
    @Override
    public MailItem parseHeaders(PSTMessage message)
    {
        MailItem m = new MailItem();

        m.setDate(message.getClientSubmitTime());
        m.setFrom(message.getSenderName() + " <" + message.getSenderEmailAddress() + '>');
        m.setTo(message.getOriginalDisplayTo());
        m.setCc(message.getOriginalDisplayCc());
        m.setContentType("multipart/mixed");
        m.setMessageId(message.getInternetMessageId());
        m.setReplyToId(message.getInReplyToId());
        m.setFirstInTopic(message.getInReplyToId() != null & !"".equals(message.getInReplyToId().trim()));
        m.setImportance(IMPORTANCES.get(message.getImportance()));
        m.setSensitivity(SENSITIVITIES.get(message.getSensitivity()));
        // No References stored in pst ... ?
        m.setRefs("");
        // No "on behalf" stored in pst ... ?
        m.setSender("");
        m.setSubject(message.getSubject());
        m.setTopic(message.getConversationTopic());
        // No topic id stored in pst ... ?
        m.setTopicId("");
        // TODO Finish and test
        return m;
    }

}
