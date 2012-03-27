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
	public void setFullName(final String fullName) {
		this.fullName = fullName;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(final String subject) {
		this.subject = subject;
	}
	
	

}
