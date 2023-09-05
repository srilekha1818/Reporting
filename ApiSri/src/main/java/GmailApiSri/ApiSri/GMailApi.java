package GmailApiSri.ApiSri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonFactory;
import java.util.Base64;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.client.util.StringUtils;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import io.restassured.path.json.JsonPath;

public class GMailApi {
	private static final String APPLICATION_NAME = "SRILEKHA GMAIL";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	 private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
	private static final String user = "me";
	//static Gmail service = null;
	 private static final String CREDENTIALS_FILE_PATH =  
	    		System.getProperty("user.dir") +
	             File.separator + "src" +
	             File.separator + "main" +
	             File.separator + "resources" +
	             File.separator + "credentials" +
	             File.separator + "credentials_new.json";
	    
	    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.dir") +
	            File.separator + "src" +
	            File.separator + "main" +
	            File.separator + "resources" +
	            File.separator + "credentials";
	    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
	        // Load client secrets.
	        InputStream in = new FileInputStream(new File(CREDENTIALS_FILE_PATH));
	        if (in == null) {
	            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
	        }
	        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
	        // Build flow and trigger user authorization request.
	        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
	                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
	                .setAccessType("offline")
	                .build();
	        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(9999).build();
	        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	    }
	  

		  public static Gmail getGmailService() throws IOException, GeneralSecurityException {
		        // Build a new authorized API client service.
		        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
		                .setApplicationName(APPLICATION_NAME)
		                .build();
		        return service;
		    }

			public static void getMailBody(String searchString) throws IOException, GeneralSecurityException {

				// Access Gmail inbox
				Gmail service = getGmailService();

				Gmail.Users.Messages.List request = service.users().messages().list(user).setQ(searchString);

				ListMessagesResponse messagesResponse = request.execute();
				request.setPageToken(messagesResponse.getNextPageToken());

				// Get ID of the email you are looking for
				String messageId = messagesResponse.getMessages().get(0).getId();

				Message message = service.users().messages().get(user, messageId).execute();

				// Print email body
				  JsonPath jp = new JsonPath(message.toString());

				String emailBody = StringUtils
						.newStringUtf8(Base64.getDecoder().decode(jp.getString("payload.parts[0].body.data")));

				System.out.println("Email body : " + emailBody);

			}

		  public static void main(String[] args) throws IOException, GeneralSecurityException {

				getGmailService();
				
				getMailBody("test");

			}
}
