package org.xwiki.component.mailarchive.internal.data;

public class MailShortItem {

	private String subject;
	private String topicId;
	private String fullName;

	public MailShortItem(String subject, String topicId, String fullName) {
		super();
		this.subject = subject;
		this.topicId = topicId;
		this.fullName = fullName;
	}

	public String getSubject() {
		return subject;
	}

	public String getTopicId() {
		return topicId;
	}

	public String getFullName() {
		return fullName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MailShortItem [subject=").append(subject)
				.append(", topicId=").append(topicId).append(", fullName=")
				.append(fullName).append("]");
		return builder.toString();
	}
	
	

}
