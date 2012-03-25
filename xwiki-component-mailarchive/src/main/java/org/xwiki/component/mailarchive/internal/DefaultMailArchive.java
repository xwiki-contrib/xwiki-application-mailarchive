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
package org.xwiki.component.mailarchive.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.mailarchive.MailArchive;
import org.xwiki.component.mailarchive.MailArchiveConfiguration;
import org.xwiki.component.mailarchive.internal.data.ConnectionErrors;
import org.xwiki.component.mailarchive.internal.data.MailArchiveConfigurationImpl;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

/**
 * Implementation of a <tt>MailArchive</tt> component.
 */
@Component
@Singleton
public class DefaultMailArchive implements MailArchive, Initializable {
	/** Provides access to documents. Injected by the Component Manager. */
	@Inject
	private DocumentAccessBridge dab;

	/**
	 * Secure query manager that performs checks on rights depending on the query being executed.
	*/
	@Requirement // TODO : @Requirement("secure") ??
	private QueryManager queryManager;
	
	@Inject
	private Logger logger;

	protected Marker marker;
	protected MailArchiveConfigurationImpl config;
	
	private HashMap<String, String[]> existingTopics;

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.xwiki.component.phase.Initializable#initialize()
	 */
	@Override
	public void initialize() throws InitializationException {
		this.marker = MarkerFactory.getMarker("MailArchiveComponent");
		this.config = new MailArchiveConfigurationImpl();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.xwiki.component.mailarchive.MailArchive#checkMails()
	 */
	@Override
	public int checkMails(String serverPrefsDoc) {
		int nbMessages = -1;

		String server = (String) dab.getProperty(serverPrefsDoc,
				"MailArchiveCode.ServerSettingsClass", 0, "hostname");
		int port = Integer.parseInt((String)dab.getProperty(serverPrefsDoc,
				"MailArchiveCode.ServerSettingsClass", 0, "port"));
		String protocol = (String) dab.getProperty(serverPrefsDoc,
				"MailArchiveCode.ServerSettingsClass", 0, "protocol");
		String user = (String) dab.getProperty(serverPrefsDoc,
				"MailArchiveCode.ServerSettingsClass", 0, "user");
		String password = (String) dab.getProperty(serverPrefsDoc,
				"MailArchiveCode.ServerSettingsClass", 0, "password");
		String folder = (String) dab.getProperty(serverPrefsDoc,
				"MailArchiveCode.ServerSettingsClass", 0, "folder");

		try {

			// Get a session. Use a blank Properties object.
			Properties props = new Properties();
			// necessary to work with Gmail
			props.put("mail.imap.partialfetch", "false");
			props.put("mail.imaps.partialfetch", "false");
			props.put("mail.store.protocol", protocol);

			Session session = Session.getDefaultInstance(props, null);
			// Get a Store object
			Store store = session.getStore(protocol);

			// Connect to the mail account
			store.connect(server, port, user, password);
			Folder fldr;
			// Specifically for GMAIL ...
			if (server.endsWith(".gmail.com")) {
				fldr = store.getDefaultFolder();
			}

			fldr = store.getFolder(folder);
			fldr.open(Folder.READ_ONLY);

			// Searches for mails not already read
			FlagTerm searchterms = new FlagTerm(new Flags(Flags.Flag.SEEN),
					false);
			Message[] messages = fldr.search(searchterms);
			nbMessages = messages.length;
			try {
				store.close();
			} catch (MessagingException e) {
				logger.debug(marker, "checkMails : Could not close connection", e);
			}
		} catch (AuthenticationFailedException e) {
			logger.warn(marker, "checkMails : ", e);
			return ConnectionErrors.AUTHENTICATION_FAILED.getCode();
		} catch (FolderNotFoundException e) {
			logger.warn(marker, "checkMails : ", e);
			return ConnectionErrors.FOLDER_NOT_FOUND.getCode();
		} catch (MessagingException e) {
			logger.warn(marker, "checkMails : ", e);
			if (e.getCause() instanceof java.net.UnknownHostException) {
				return ConnectionErrors.UNKNOWN_HOST.getCode();
			} else {
				return ConnectionErrors.CONNECTION_ERROR.getCode();
			}
		} catch (IllegalStateException e) {
			return ConnectionErrors.ILLEGAL_STATE.getCode();
		} catch (Throwable t) {
			logger.warn(marker, "checkMails : " ,t);
			return ConnectionErrors.UNEXPECTED_EXCEPTION.getCode();
		}
		logger.debug(marker, "checkMails : ${nbMessages} to be read from $server:$port:$protocol:$user:$folder");
		return nbMessages;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.xwiki.component.mailarchive.MailArchive#loadMails(int, boolean)
	 */
	@Override
	public boolean loadMails(int nb, boolean withDelete) {
		// TODO Auto-generated method stub
		return false;
	}

    /**
     * loadExistingTopics
     * Loads existing topics information from database.
     * @throws QueryException 
    */
   public int loadExistingTopics() throws QueryException 
   {
     String xwql = "select doc.fullName, topic.topicid, topic.subject from Document doc, doc.object(MailArchiveCode.MailTopicClass) as  topic where doc.fullName<>'MailArchiveCode.MailTopicClassTemplate'";
     List<String[]> topics;

     topics = this.queryManager.createQuery(xwql, Query.XWQL).execute();

     for(String[] topic:topics) {
    	 existingTopics.put(topic[1], new String[] {topic[0], topic[2]});
     }
     return topics.size();
   }

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.xwiki.component.mailarchive.MailArchive#getConfiguration()
	 */
	@Override
	public MailArchiveConfiguration getConfiguration() {
		return this.config;
	}

}
