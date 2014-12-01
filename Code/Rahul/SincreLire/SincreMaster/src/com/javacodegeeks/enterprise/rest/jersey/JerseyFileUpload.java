package com.javacodegeeks.enterprise.rest.jersey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@SuppressWarnings("deprecation")
@Path("/files")
public class JerseyFileUpload {

	private static final String SERVER_UPLOAD_LOCATION_FOLDER = "C:\\Users\\Rahul\\Desktop\\temp\\";
	//private static final String SERVER_UPLOAD_LOCATION_FOLDER = "";
	/**
	 * Upload a File
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */

	@POST
	@Path("/upload")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String uploadFile(
			@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws JsonGenerationException, JsonMappingException, IOException {
		String fileName = contentDispositionHeader.getFileName() +  UUID.randomUUID();
		String filePath = SERVER_UPLOAD_LOCATION_FOLDER + fileName;
		
		/*String filePath = SERVER_UPLOAD_LOCATION_FOLDER
				+ contentDispositionHeader.getFileName();*/
		System.out.println("Filename uploaded to server" + fileName);
		
		//uploadedFilePath = filePath;
		ObjectMapper mapper = new ObjectMapper();
		// save the file to the server
		saveFile(fileInputStream, filePath);
		
		S3Uploader uploader = new S3Uploader();
		uploader.uploadImageToS3(fileName, filePath);
		
		return mapper.writeValueAsString(fileName);
	}
	
	/**
	 * @param name
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("/getMatches/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getResults(@PathParam("name") String name) throws JsonGenerationException, JsonMappingException, IOException{
		
		System.out.println("Name of the file to upload::" + name);
		List<OutputObject> objects = new ArrayList<OutputObject>();
		//String result = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			System.out.println("before search method");
			//objects = Searcher.searchImage(SERVER_UPLOAD_LOCATION_FOLDER + name);
			objects = getMatchesFromWorkers1(name);
			System.out.println("after search method");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return mapper.writeValueAsString(result);
		Gson gson = new Gson();
		return mapper.writeValueAsString(gson.toJson(objects));
	}

	/**
	 * @param name
	 * @return
	 */
	public List<OutputObject> getMatchesFromWorkers1(String name){
		
		
		
		
		
		
		
		
		
		
		String url = "http://localhost:8080/SincreWorker/rest/files/start";
		Client client = Client.create();
		WebResource webResource = client
		   .resource(url);
 
		ClientResponse response = webResource.type("application/json")
		   .post(ClientResponse.class, name);
 
		if (response.getStatus() != 201) {
			throw new RuntimeException("Failed : HTTP error code : "
			     + response.getStatus());
		}
 
		System.out.println("Output from Server .... \n");
		String output = response.getEntity(String.class);
		System.out.println(output);
		
		Gson gson = new Gson();

		Type listType = new TypeToken<List<OutputObject>>(){}.getType();
		List<OutputObject> myList = gson.fromJson(output,listType);
		
		System.out.println(myList);
		return myList;
		
	}
	
	/**
	 * @param name
	 * @return
	 */
	public String getMatchesFromWorkers(String name) {
		try {
			String url = "http://localhost:8080/SincreWorker/rest/files/start/getMatches/"+name;
			Client client = Client.create();
			WebResource webResource = client.resource(url);
			ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
	 
			if (response.getStatus() != 201) {
				throw new RuntimeException("Failed : HTTP error code : "
				     + response.getStatus());
			}
	 
			System.out.println("Output from Server .... \n");
			String output = response.getEntity(String.class);
			System.out.println(output);
			return output;
	 
		  } catch (Exception e) {
			e.printStackTrace();
		  }
			return null;
	}
	
	// save uploaded file to a defined location on the server
	/**
	 * @param uploadedInputStream
	 * @param serverLocation
	 */
	private void saveFile(InputStream uploadedInputStream, String serverLocation) {
		try {
			OutputStream outpuStream = new FileOutputStream(new File(
					serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			outpuStream = new FileOutputStream(new File(serverLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outpuStream.write(bytes, 0, read);
			}
			outpuStream.flush();
			outpuStream.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}