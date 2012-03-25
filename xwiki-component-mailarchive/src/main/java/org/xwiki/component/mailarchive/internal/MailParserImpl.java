package org.xwiki.component.mailarchive.internal;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.xwiki.component.mailarchive.MailParser;
import org.xwiki.component.mailarchive.internal.data.MailItem;

public class MailParserImpl implements MailParser {
	/**
	 * parseMail Parse mail headers to create a MailItem. Decodes localization
	 * and date.
	 */
	public MailItem parseMail(Message mail) {

		MailItem m = new MailItem();

		String[] headers;

		try {
			String topicId = "";
			headers = mail.getHeader("Thread-Index");
			if (headers != null) {
				topicId = MimeUtility.decodeText(headers[0]);
				topicId = cropId(topicId);
			}
			if (topicId.length() >= 30) {
				topicId = topicId.substring(0, 29);
			}
			m.setTopicId(topicId);

			String messageId = "";
			headers = mail.getHeader("Message-ID");
			if (headers != null) {
				messageId = MimeUtility.decodeText(headers[0]);
				messageId = cropId(messageId);
			}
			m.setMessageId(messageId);

			String replyToId = "";
			headers = mail.getHeader("In-Reply-To");
			if (headers != null) {
				replyToId = headers[0];
				replyToId = cropId(replyToId);
			}
			m.setReplyToId(replyToId);

			String refs = "";
			headers = mail.getHeader("References");
			if (headers != null) {
				refs = headers[0];
			}
			m.setRefs(refs);

			String subject = "[no subject]";
			headers = mail.getHeader("Subject");
			if (headers != null && !headers[0].isEmpty()) {
				subject = MimeUtility.decodeText(headers[0])
						.replaceAll("\n", " ").replaceAll("\r", " ")
						.replaceAll(">", "&gt;").replaceAll("<", "&lt;");
			}
			m.setSubject(subject);

			String topic = "[no subject]";
			headers = mail.getHeader("Thread-Topic");
			if (headers != null && !headers[0].isEmpty()) {
				topic = MimeUtility.decodeText(headers[0])
						.replaceAll("\n", " ").replaceAll("\r", " ");
			}
			m.setTopic(topic);

			String from = "";
			headers = mail.getHeader("From");
			if (headers != null) {
				from = MimeUtility.decodeText(headers[0]);
			}
			from = from.replaceAll("\"", "");
			m.setFrom(from);

			String to = "";
			headers = mail.getHeader("To");
			if (headers != null) {
				to = MimeUtility.decodeText(headers[0]);
			}
			to = to.replaceAll("\"", "");
			m.setTo(to);

			String cc = "";
			headers = mail.getHeader("CC");
			if (headers != null) {
				cc = MimeUtility.decodeText(headers[0]);
			}
			cc = cc.replaceAll("\"", "");
			m.setCc(cc);

			// process the language
			String locLang = "en";
			String locCountry = "US";
			String language;
			headers = mail.getHeader("Content-Language");
			if (headers != null) {
				language = headers[0];
				if (language != null && !language.isEmpty()) {
					int index = language.indexOf('.');
					if (index != -1) {
						locLang = language.substring(0, index - 1);
						locCountry = language.substring(index);
					}
				}
			}
			Locale locale = new Locale(locLang, locCountry);
			m.setLocale(locale);

			SimpleDateFormat dateFormatter = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss ZZZZZ", locale);

			String date = "";
			Date decodedDate = null;
			headers = mail.getHeader("Date");
			if (headers != null) {
				date = headers[0];
			}
			// Decode the date
			try {
				decodedDate = dateFormatter.parse(date);
			} catch (ParseException pE) {
				decodedDate = new Date();
			}
			m.setDate(date);
			m.setDecodedDate(decodedDate);

			// @TODO : not generic part
			boolean isNewsletter = (subject.toUpperCase()
					.contains("COMMUNITY NEWSLETTER"));
			boolean isProductRelease = ((from.toUpperCase().contains(
					"DONOTREPLY@GEMALTO.COM") || from.toUpperCase().contains(
					"DOWNLOADZONE")) && (subject.toUpperCase()
					.startsWith("DELIVERY OF")));
			// end of not generic part
			String type = "Mail";
			// @TODO : not generic part
			if (isNewsletter) {
				type = "Newsletter";
			}
			if (isProductRelease) {
				type = "Product Release";
			}
			// end of not generic part
			m.setType(type);

			boolean firstInTopic = ("".equals(replyToId));
			m.setFirstInTopic(firstInTopic);

			// @TODO Try to retrieve wiki user
			// @TODO : here, or after ? (link with ldap and xwiki profiles
			// options to be checked ...)
			/*
			 * String userwiki = parseUser(from); if (userwiki == null ||
			 * userwiki == "") { userwiki = unknownUser; }
			 */
			m.setWikiuser(null);

			m.setBodypart(mail.getContent());
			m.setContentType(mail.getContentType().toLowerCase());

			String sensitivity = "normal";
			headers = mail.getHeader("Sensitivity");
			if (headers != null && !headers[0].isEmpty()) {
				sensitivity = "normal";
			}
			m.setSensitivity(sensitivity);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return m;
	}

	/**
	 * Extract id from mail address header string.
	 * 
	 * @param id
	 * @return
	 */
	public static String cropId(String id) {
		int start = id.indexOf('<');
		int end = id.indexOf('>');
		if (start != -1 && end != -1) {
			return id.substring(start + 1, end);
		} else {
			return id;
		}
	}
}
