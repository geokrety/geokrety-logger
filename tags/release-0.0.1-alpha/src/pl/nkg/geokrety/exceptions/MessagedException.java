package pl.nkg.geokrety.exceptions;

import android.content.ContextWrapper;

public class MessagedException extends Exception {
	private static final long serialVersionUID = 6866743560825681444L;
	private final int messageID;
	private final String arg;
	
	public MessagedException(int messageID) {
		this.messageID = messageID;
		arg = "";
	}

	public MessagedException(int message, String arg) {
		this.messageID = message;
		this.arg = arg;
	}
	
	public int getMessageID() {
		return messageID;
	}
	
	public String getFormatedMessage(ContextWrapper context) {
		String message = context.getResources().getString(messageID) + arg;
		return message;
	}
}
