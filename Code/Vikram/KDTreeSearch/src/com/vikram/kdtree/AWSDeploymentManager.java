package com.vikram.kdtree;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.gson.Gson;

public class AWSDeploymentManager {
	private static final Integer RATELIMIT_WAIT_TIME = 500;
	
	private static String SqsEndpoint = "https://sqs.us-east-1.amazonaws.com";
	private static String ResponseEndpoint = "http://sincre-c-status:8701";
	
	private static String userPrefix = "/139012132121/";
	private static String inQueue = "syncre-link-b-c";
	private static String frameMetadataBucketName = "syncre-datasets";
	
	private static String InsertOperation = "0";
	private static String SearchOperation = "1";
	private static String ResponseKey = "Results";
	
    private BasicAWSCredentials credentials;
    private AmazonSQS sqsClient;
    private static volatile AWSDeploymentManager awsDeplMgr = new AWSDeploymentManager();
	private static volatile KDTreeSearcher kdTreeSearcher = new KDTreeSearcher();
	
	public static void main(String[] args) {
		try {
			String inQueueUrl = SqsEndpoint + userPrefix + inQueue;
			
			// Initialize the S3 client on the Metadata KDTree manager
			kdTreeSearcher.startS3Client(awsDeplMgr.credentials);
			
			// Run until infinity
			while (true) {
				// Read messages from sincre-link-b-c
				List<Message> sincreLayerCInput = awsDeplMgr.getMessagesFromQueue(inQueueUrl);
				
				System.out.println("\n\n--- Received " + sincreLayerCInput.size() + " messages ---");
				
				for (Message message : sincreLayerCInput) {
					/* Process this message :-
					 * Based on type of message, to sincre-c-status:
					 * Write SIFT metadata to KDTree and respond <OR> Query KDTree and send matches.
					 */
					String[] parts = message.getBody().split("\r?\n");
					String s3Key = parts[0];
					String oper = parts[1];
					System.out.println("(OPER, KEY): (" + oper + ", " + s3Key + ")");
					
					// Perform the requested operation
					if (oper.equals(InsertOperation)) {
						/* int numFrames = */
						kdTreeSearcher.insertFrames(s3Key, frameMetadataBucketName);
						
						// To indicate that frames were added successfully:
						//    postData(ResponseEndpoint, "INSERTED: " + numFrames + " frames (metadata)");
					}
					else if (oper.equals(SearchOperation)) {
						List<String> searchResultsList = kdTreeSearcher.searchFrames(s3Key, frameMetadataBucketName);
						
						// First entry should be the query frame key which doubles as a transaction ID, since SQS does not guarantee FIFO order.
						searchResultsList.add(0, s3Key);
						
						// Return the best matched N frames
						postData(ResponseEndpoint, new Gson().toJson(searchResultsList));
					}
					else {
						System.out.println("WARN: Unrecognized message! MSG: " + message.toString() + "\nBODY: " + message.getBody());
					}
					
					// Remove this message from the queue
					awsDeplMgr.deleteMessageFromQueue(inQueueUrl, message);
				}
				
				// Rate limit our AWS SQS requests
				try {
					Thread.sleep(RATELIMIT_WAIT_TIME);
				} catch (InterruptedException e) {
					System.out.println("ERR: InterruptedException while calling sleep() after SQS poll.");
				}
			}
		}
		finally {
			// Disconnect from the sqs queues
			awsDeplMgr.shutdownSqs();
		}
	}
	
	public static void postData(String url, String messageBody) {
	    // Create a new HttpClient and Post Header
	    CloseableHttpClient httpclient = HttpClientBuilder.create().build();
	    HttpPost httppost = new HttpPost(url);

	    try {
	        // Add your data
	    	httppost.setHeader(ResponseKey, messageBody);
	    	
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
            properties.load(new FileInputStream("AwsCredentials.properties"));
            this.credentials = new   BasicAWSCredentials(properties.getProperty("accessKey"),
                                                         properties.getProperty("secretKey"));
            this.sqsClient = new AmazonSQSClient(this.credentials);
            
            /**
             * Find endpoints here: http://docs.aws.amazon.com/general/latest/gr/rande.html
             * Overrides the default endpoint for this client ("sqs.us-east-1.amazonaws.com")
             */
            this.sqsClient.setEndpoint(SqsEndpoint);
            
            /** You can use this in your web app where    AwsCredentials.properties is stored in web-inf/classes
             */
            //AmazonSQS sqs = new AmazonSQSClient(new ClasspathPropertiesFileCredentialsProvider());
        }
        catch(Exception e){
            System.out.println("Exception while creating SQS client: " + e);
        }
	}
    
    public static AWSDeploymentManager getInstance(){
        return awsDeplMgr;
    }
 
    /**
     * shuts down the SQS endpoint client
     * @param
     */
    public void shutdownSqs(){
        this.sqsClient.shutdown();
    }
    
    /**
     * returns the queueurl for for sqs queue if you pass in a name
     * @param queueName
     * @return
     */
    public String getQueueUrl(String queueName){
        GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(queueName);
        return this.sqsClient.getQueueUrl(getQueueUrlRequest).getQueueUrl();
    }
 
    /**
     * lists all your queue.
     * @return
     */
    public ListQueuesResult listQueues(){
       return this.sqsClient.listQueues();
    }
 
    /**
     * send a single message to your sqs queue
     * @param queueUrl
     * @param message
     */
    public void sendMessageToQueue(String queueUrl, String message){
        SendMessageResult messageResult =  this.sqsClient.sendMessage(new SendMessageRequest(queueUrl, message));
        System.out.println(messageResult.toString());
    }
 
    /**
     * gets messages from your queue
     * @param queueUrl
     * @return
     */
    public List<Message> getMessagesFromQueue(String queueUrl){
       ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
       List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
       return messages;
    }
 
    /**
     * deletes a single message from your queue.
     * @param queueUrl
     * @param message
     */
    public void deleteMessageFromQueue(String queueUrl, Message message){
        String messageRecieptHandle = message.getReceiptHandle();
        System.out.println("Message to delete: " + message.getBody() + "." + message.getReceiptHandle());
        sqsClient.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));
    }
}
