package pl.nkg.geokrety.exceptions;

public class MessagedException extends Exception {
	private static final long serialVersionUID = 6866743560825681444L;
	private final int messageID;
	private final Object[] args;
	
	public MessagedException(int messageID) {
		this.messageID = messageID;
		args = new Object[]{};
	}

	public MessagedException(int message, Object[] args) {
		this.messageID = message;
		this.args = args;
	}
	
	public int getMessageID() {
		return messageID;
	}
	
	public Object[] getArgs() {
		return args;
	}
}
