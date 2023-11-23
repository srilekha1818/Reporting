package restjava23;





import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
 
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//import org.eclipse.jetty.http.HttpStatus;

//import org.apache.http.HttpStatus;
 

 
public class SampleGET {
 
	private static String userName = "admin";
	private static String password = "admin123";
	private static String hostName = "localhost";
	private static String portNumber = "8206";
	private static String databaseName = "P6EPPM2";
	private static String objectId = "178";
	private static String loginUrl = "http://" + hostName + ":" + portNumber + "/p6ws/restapi/login"
			+ "?DatabaseName="+ databaseName;
	private static String loadActivitiesURL = "http://" + hostName + ":" + portNumber + "/p6ws/restapi/calendar?Fields=Name,ObjectId&Filter=ObjectId :eq: " + objectId;
 
	public static void main(String s[]) throws Exception {
		String responseJson = callRestURL(loginUrl, "POST");
		System.out.println("Response:- " + responseJson);
	}
	private static String callRestURL(String restUrl, String method) throws Exception {
		HttpURLConnection conn = null;
		try {
			trustAllCert();
			String cookie = callLoginAPI(restUrl, method, conn);
			URL loadUrl = new URL(loadActivitiesURL);
			conn = (HttpURLConnection) loadUrl.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Cookie", cookie);
			if (conn.getResponseCode() != HttpStatus.SC_OK) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + "Error: "
						+ readStreamData(conn.getErrorStream()));
			}
			return readStreamData(conn.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return "";
		
	}
	private static String callLoginAPI(String restUrl, String method, HttpURLConnection conn) throws IOException {
		URL url = new URL(restUrl);
		conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod(method);
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Content-Type", "application/json");
		String userCredentials = userName + ":" + password;
		String base64Credentials = javax.xml.bind.DatatypeConverter.printBase64Binary(userCredentials.getBytes());
		String basicAuth = base64Credentials;
		conn.setRequestProperty("authToken", basicAuth);
		if (conn.getResponseCode() != HttpStatus.SC_OK) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode() + "Error: "
					+ readStreamData(conn.getErrorStream()));
		}
		return conn.getHeaderField("Set-Cookie");
	}
 
	private static String readStreamData(InputStream is) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String output;
			StringBuilder buff = new StringBuilder();
			while ((output = br.readLine()) != null) {
				buff.append(output);
			}
			return buff.toString();
		}
	}
 
	private static void trustAllCert() throws NoSuchAlgorithmException, KeyManagementException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
 
			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}
 
			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };
		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
 
}
	

