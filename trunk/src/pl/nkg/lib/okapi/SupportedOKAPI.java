package pl.nkg.lib.okapi;

public class SupportedOKAPI {

	public final String host;
	public final int version;
	public final String consumerKey;

	public static final SupportedOKAPI[] SUPPORTED = new SupportedOKAPI[] { //
	new SupportedOKAPI("opencaching.pl", 912, "DajjA4r3QZNRHAef7XZD") //
	};

	private SupportedOKAPI(String host, int version, String consumerKey) {
		super();
		this.host = host;
		this.version = version;
		this.consumerKey = consumerKey;
	}
}
