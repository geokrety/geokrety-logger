package pl.nkg.geokrety;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {

	private final String URL_LOGIN = "http://geokrety.org/api-login2secid.php";
	private final String URL_EXPORT2 = "http://geokrety.org/export2.php";
	
	//LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
	private final ArrayList<String> listItems = new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
	private ArrayAdapter<String> adapter;
	
	private PreferencesDecorator preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = new PreferencesDecorator(this);
		setContentView(R.layout.activity_main);
		
		adapter = new ArrayAdapter<String>(this,
	            android.R.layout.simple_list_item_1,
	            listItems);
		
		ListView myListView = (ListView)findViewById(R.id.inventoryListView);
		myListView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void showAccountsActivity(View view) {
		startActivity(new Intent(this, AccountsActivity.class));
	}

	public void retriveSecureID(View view) {
		String[][] postData = new String[][] {
				new String[] { "login", getLogin() },
				new String[] { "password", getPassword() } };

		try {
			String value = httpPost(URL_LOGIN, postData);// ret.size() > 0 ?
															// ret.get(0) :
															// null;
			if (value != null && !value.startsWith("error")) {
				setSecureID(value.trim());
			} else {
				showMessageBox(
						getResources().getString(R.string.login_error_title),
						getResources().getString(
								R.string.login_error_password_message)
								+ String.valueOf(value));
			}
		} catch (Exception e) {
			showMessageBox(
					getResources().getString(R.string.login_error_title),
					getResources().getString(R.string.login_error_message)
							+ e.toString());
			setSecureID(e.toString());
		}

		retriveInventory(view);
	}

	public void retriveInventory(View view) {
		String[][] postData = new String[][] {
				new String[] { "secid", getSecureID() },
				new String[] { "inventory", "1" } };
		try {
			String xml = httpGet(URL_EXPORT2, postData);
			Document doc = getDomElement(xml); // getting DOM element
			 
			NodeList nl = doc.getElementsByTagName("geokret");
			 
			// looping through all item nodes <item>     
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				String nr = node.getAttributes().getNamedItem("nr").getTextContent();
			    String description = node.getTextContent();
			    listItems.add(description + " (" + nr + ")");
			}
		    adapter.notifyDataSetChanged();
		} catch (Exception e) {
			showMessageBox(
					getResources().getString(R.string.login_error_title),
					getResources().getString(R.string.login_error_message)
							+ e.toString());
			setSecureID(e.toString());
		}
	}

	private Document getDomElement(String xml) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);

		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}
		// return DOM
		return doc;
	}

	private final String getElementValue(Node elem) {
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child
						.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE) {
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

	private String httpPost(String url, String[][] data)
			throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		// Add your data
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
				data.length);
		for (String[] varible : data) {
			nameValuePairs.add(new BasicNameValuePair(varible[0], varible[1]));
		}
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		// Execute HTTP Post Request
		return EntityUtils.toString(httpclient.execute(httppost).getEntity(), HTTP.UTF_8);
	}

	private String httpGet(String url, String[][] data)
			throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();

		StringBuilder sb = new StringBuilder();
		sb.append(url);
		boolean first = true;

		for (String[] varible : data) {
			sb.append(first ? "?" : "&");
			first = false;
			sb.append(varible[0]);
			sb.append("=");
			sb.append(varible[1]);
		}

		String url2 = sb.toString();
		HttpGet httppost = new HttpGet(url2);

		// Execute HTTP Post Request
		return EntityUtils.toString(httpclient.execute(httppost).getEntity(), HTTP.UTF_8);
	}

	private String getLogin() {
		preferences = new PreferencesDecorator(this);
		return preferences.getAccountLogin();
	}

	private String getPassword() {
		preferences = new PreferencesDecorator(this);
		return preferences.getAccountPassword();
	}

	private String getSecureID() {
		return ((EditText) findViewById(R.id.secureIDEditText)).getText()
				.toString();
	}

	private void setSecureID(String value) {
		((EditText) findViewById(R.id.secureIDEditText)).setText(value);
	}

	private void showMessageBox(String title, String message) {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

		dlgAlert.setMessage("wrong password or username");
		dlgAlert.setTitle("Error Message...");
		dlgAlert.setPositiveButton(getResources()
				.getString(android.R.string.ok), null);
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();

		dlgAlert.setPositiveButton(getResources()
				.getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				});
	}
}
