package com.gewara.vo;

import com.gewara.api.gmail.request.SendMailByOutboxRequest;

public class GmailRecord extends SendMailByOutboxRequest{

	private static final long serialVersionUID = 6026044566943687988L;
	public GmailRecord(){}
	
	public GmailRecord(String title, String content, String email) {
		this.setOutbox(SendMailByOutboxRequest.OUTBOX_OPERATION021);
		this.setFromName("GewaraReport");
		this.setTitle(title);
		this.setContent(content);
		this.setTo(email);
		this.setSendType("html");
	}
}
