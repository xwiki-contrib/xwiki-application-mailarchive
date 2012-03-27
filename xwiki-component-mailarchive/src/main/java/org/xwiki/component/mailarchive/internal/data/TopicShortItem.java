package org.xwiki.component.mailarchive.internal.data;

public class TopicShortItem {

	private String fullName;
	private String subject;

	public TopicShortItem(String fullName, String subject) {
		super();
		this.fullName = fullName;
		this.subject = subject;
	}

	public String getFullName() {
		return fullName;
	}

	public String getSubject() {
		return subject;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TopicShortItem [fullName=").append(fullName)
				.append(", subject=").append(subject).append("]");
		return builder.toString();
	}

	
	
}
