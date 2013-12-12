package pl.nkg.geokrety.data;

import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.nkg.geokrety.R;
import pl.nkg.geokrety.Utils;
import pl.nkg.geokrety.exceptions.MessagedException;

public class Account {
	private static final String URL_LOGIN = "http://geokrety.org/api-login2secid.php";
	private static final String URL_EXPORT2 = "http://geokrety.org/export2.php";
	private static final long EXPIRED = 600000;

	private String geoKretyLogin;
	private String geoKretyPassword;
	private String openCachingLogin;

	private String geoKretySecredID;
	private String openCachingUUID;
	private ArrayList<GeocacheLog> openCachingLogs;
	
	private ArrayList<Geokret> inventory;
	
	private Date lastDataLoaded;

	public Account(String geoKretyLogin, String geoKretyPassword,
			String openCachingLogin) {
		this.geoKretyLogin = geoKretyLogin;
		this.geoKretyPassword = geoKretyPassword;
		this.openCachingLogin = openCachingLogin;
	}

	public String getGeoKretyLogin() {
		return geoKretyLogin;
	}

	public void setGeoKretyLogin(String geoKretyLogin) {
		this.geoKretyLogin = geoKretyLogin;
	}

	public String getGeoKretyPassword() {
		return geoKretyPassword;
	}

	public void setGeoKretyPassword(String geoKretyPassword) {
		this.geoKretyPassword = geoKretyPassword;
	}

	public String getGeoKreySecredID() {
		return geoKretySecredID;
	}

	public String getOpenCachingLogin() {
		return openCachingLogin;
	}

	public void setOpenCachingLogin(String openCachingLogin) {
		this.openCachingLogin = openCachingLogin;
	}

	public String getOpenCachingUUID() {
		return openCachingUUID;
	}

	public ArrayList<GeocacheLog> getOpenCachingLogs() {
		return openCachingLogs;
	}
	
	public ArrayList<Geokret> getInventory() {
		return inventory;
	}
	
	public boolean expired() {
		if (lastDataLoaded == null) {
			return true;
		}
		return new Date().getTime() - lastDataLoaded.getTime() > EXPIRED;
	}

	public void loadData() throws MessagedException {
		loadSecureID();
		loadInventory();
		loadOpenCachingUUID();
		loadOpenCachingLogs();
		lastDataLoaded = new Date();
	}

	private void loadSecureID() throws MessagedException {
		String[][] postData = new String[][] {
				new String[] { "login", geoKretyLogin },
				new String[] { "password", geoKretyPassword } };

		String value;
		try {
			value = Utils.httpPost(URL_LOGIN, postData);
		} catch (Exception e) {
			throw new MessagedException(R.string.login_error_message);
		}

		if (value != null && !value.startsWith("error")) {
			geoKretySecredID = value.trim();
		} else {
			throw new MessagedException(R.string.login_error_password_message,
					new Object[] { geoKretyLogin, String.valueOf(value) });
		}
	}
	
	private void loadInventory() throws MessagedException {
		inventory = new ArrayList<Geokret>();
		
		String[][] postData = new String[][] {
				new String[] { "secid", geoKretySecredID },
				new String[] { "inventory", "1" } };
		try {
			String xml = Utils.httpGet(URL_EXPORT2, postData);
			Document doc = Utils.getDomElement(xml);
			 
			NodeList nl = doc.getElementsByTagName("geokret");
			 
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				inventory.add(new Geokret(node));
			}
		} catch (Exception e) {
			throw new MessagedException(R.string.inventory_error_message);
		}
	}
	
	private void loadOpenCachingUUID() {
		// TODO:
	}
	
	private void loadOpenCachingLogs() {
		openCachingLogs = new ArrayList<GeocacheLog>();
		// TODO:
	}
	
	@Override
	public String toString() {
		return geoKretyLogin;
	}

	public void loadIfExpired() throws MessagedException {
		if (expired()) {
			loadData();
		}
	}
}
