package com.vikram.kdtree;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.amazonaws.auth.BasicAWSCredentials;
import com.google.gson.Gson;

public class AWSDeploymentManager {
    private BasicAWSCredentials credentials;
    private static volatile AWSDeploymentManager awsDeplMgr = new AWSDeploymentManager();
	private static volatile KDTreeSearcher kdTreeSearcher = new KDTreeSearcher();
	
	// Server populates this queue and the main program waits/reads from it
	public static final List<String> requestQueue = Collections.synchronizedList(new LinkedList<String>());
	
	public static final String LayerBRequestType = "POST";
	public static final String S3PrefixDelimiter = "/";
	public static final String RequestDelimiter = "\t| |\r?\n";
	public static final String ResponseKey = "Process-Results";
	public static final String ContentKey = "Content-Type";
	public static final String ContentType = "application/json";
	public Integer RequestPort = null;
	public String ResponseEndpoint = null;
	
	private String frameMetadataBucketName = null;	//	"frames-test";
	private String databaseMetadataDirPrefix = null;
	private String queryMetadataDirPrefix = null;	//	"QueryVectors";
	
	private static final String InsertOperation = "0";
	private static final String SearchOperation = "1";
	
	public static void main(String[] args) {
		// Initialize the S3 client on the Metadata KDTree manager
		kdTreeSearcher.startS3Client(awsDeplMgr.credentials);
		
		// Initialize the server that listens to Layer B
		Thread requestReceiver = new Thread(new ElementalHttpServer());
		requestReceiver.start();
		
		// Run until infinity
		String sincreLayerCInput = null;
		try {
			while (true) {
				// Read messages from sincre-link-b-c
				try {
					synchronized (requestQueue) {
						sincreLayerCInput = requestQueue.remove(0);
					}
					
					System.out.println("\n\n--------------------------\n--- Received a message ---\n--------------------------");
					System.out.println("The received message was: " + sincreLayerCInput);
					/* Process this message :-
					 * Based on type of message, to sincre-c-status:
					 * Write SIFT metadata to KDTree and respond <OR> Query KDTree and send matches.
					 */
					String[] parts = sincreLayerCInput.split(RequestDelimiter);
					String s3Key = parts[0];
					String oper = null;
					try {
						oper = parts[1];
					} catch (ArrayIndexOutOfBoundsException e) {
						oper = SearchOperation;
					}
					
					System.out.println("(OPER, KEY): (" + oper + ", " + s3Key + ")");
					
					// Perform the requested operation
					if (oper.equals(InsertOperation)) {
						s3Key = awsDeplMgr.databaseMetadataDirPrefix + S3PrefixDelimiter + s3Key;
						kdTreeSearcher.insertFrames(s3Key, awsDeplMgr.frameMetadataBucketName);
						
						// To indicate that frames were added successfully:
						postData(awsDeplMgr.ResponseEndpoint, "OK");
					}
					else if (oper.equals(SearchOperation)) {
						// "FeatureVectors/lSBJGXh6UMfhdzXPPKLqNefBCAzkgijO";
						s3Key = awsDeplMgr.queryMetadataDirPrefix + S3PrefixDelimiter + s3Key;
						List<String> searchResultsList = kdTreeSearcher.searchFrames(s3Key, awsDeplMgr.frameMetadataBucketName);
						
						// First entry should be the query frame key which doubles as a transaction ID, since SQS does not guarantee FIFO order.
						searchResultsList.add(0, s3Key);
						
						// Return the best matched N frames
						System.out.println("JSON: " + new Gson().toJson(searchResultsList));
						postData(awsDeplMgr.ResponseEndpoint, new Gson().toJson(searchResultsList));
					}
					else {
						System.out.println("WARN: Unrecognized message! MSG: " + sincreLayerCInput + "\nBODY: " + sincreLayerCInput);
					}
				}
				catch(ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		}
		finally {
			try {
				requestReceiver.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	public static void postData(String url, String messageBody) {
	    // Create a new HttpClient and Post Header
	    CloseableHttpClient httpclient = HttpClientBuilder.create().build();
	    HttpPost httppost = new HttpPost(url);

	    try {
	        // Add your data
	    	httppost.addHeader(ContentKey, ContentType);
	    	httppost.setEntity(new StringEntity(messageBody));
	    	
	        // Execute HTTP Post Request
	        /* HttpResponse response = */
	    	httpclient.execute(httppost);
	    }
	    catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    }
	    catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	    
	    // Shutdown the HTTP client
	    try {
			httpclient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private AWSDeploymentManager() {
        try{
            Properties properties = new Properties();
            properties.load(new FileInputStream("Config.properties"));
            this.credentials = new   BasicAWSCredentials(properties.getProperty("accessKey"),
                                                         properties.getProperty("secretKey"));
            this.ResponseEndpoint = properties.getProperty("responseEndpoint");
            this.frameMetadataBucketName = properties.getProperty("frameMetadataBucketName");
            this.queryMetadataDirPrefix = properties.getProperty("queryMetadataDirPrefix");
            this.databaseMetadataDirPrefix = properties.getProperty("databaseMetadataDirPrefix");
            this.RequestPort = Integer.valueOf(properties.getProperty("requestPort"));
        }
        catch(Exception e){
            System.out.println("Exception while reading AWS credentials: " + e);
        }
	}
    
    public static AWSDeploymentManager getInstance(){
        return awsDeplMgr;
    }
}
