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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

/**
 * @author Rahul
 * 
 */
@Path("/files")
public class JerseyFileUpload {

	private static final String SERVER_UPLOAD_LOCATION_FOLDER = "/home/ec2-user/download/";

	/**
	 * Upload a File
	 * 
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
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader)
			throws JsonGenerationException, JsonMappingException, IOException {

		try {
			String fileName = UUID.randomUUID() + ".png";
			String filePath = SERVER_UPLOAD_LOCATION_FOLDER + fileName;

			// uploadedFilePath = filePath;
			ObjectMapper mapper = new ObjectMapper();
			// save the file to the server
			saveFile(fileInputStream, filePath);

			// upload it to s3 for final download by all workers
			S3Uploader uploader = new S3Uploader();
			uploader.uploadImageToS3(fileName, filePath);

			// return the filename to the UI
			return mapper.writeValueAsString(fileName);

		} catch (Exception e) {
			return "no data";
		}
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
	public String getResults(@PathParam("name") String name)
			throws JsonGenerationException, JsonMappingException, IOException {
		try {
			System.out.println("Name of the file to upload::" + name);
			List<OutputObject> objects = new ArrayList<OutputObject>();

			ObjectMapper mapper = new ObjectMapper();

			try {
				System.out.println("before search method");
				objects = getMatchesFromWorkers(name);
				System.out.println("after search method");
			} catch (Exception e) {
				System.out
						.println("something went wrong while calling workers");
			}

			// return mapper.writeValueAsString(result);
			Gson gson = new Gson();
			return mapper.writeValueAsString(gson.toJson(objects));

		} catch (Exception e) {
			return "no data";
		}
	}

	/**
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public List<OutputObject> getMatchesFromWorkers(String name)
			throws IOException, InterruptedException, ExecutionException {

		// first worker
		String url = "http://ec2-54-85-12-32.compute-1.amazonaws.com:8080/SincreWorker/rest/files/getMatches/"
				+ name;
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		String output = "";
		Future<Response> f = asyncHttpClient.prepareGet(url)
				.addQueryParameter("name", name)
				.execute(new AsyncCompletionHandler<Response>() {
					@Override
					public Response onCompleted(Response response)
							throws Exception {
						// Do something with the Response
						// ...
						System.out.println(response.getResponseBody());
						return response;
					}

					@Override
					public void onThrowable(Throwable t) {
						System.out.println("error");
						// Something wrong happened.
					}
				});
		System.out.println("made first call");

		// second worker
		url = "http://ec2-54-174-219-45.compute-1.amazonaws.com:8080/SincreWorker/rest/files/getMatches/"
				+ name;
		AsyncHttpClient asyncHttpClient1 = new AsyncHttpClient();
		// String output = "";
		Future<Response> f1 = asyncHttpClient1.prepareGet(url)
				.addQueryParameter("name", name)
				.execute(new AsyncCompletionHandler<Response>() {
					@Override
					public Response onCompleted(Response response)
							throws Exception {
						// Do something with the Response
						// ...
						System.out.println(response.getResponseBody());
						return response;
					}

					@Override
					public void onThrowable(Throwable t) {
						System.out.println("error");
						// Something wrong happened.
					}
				});
		System.out.println("made second call");

		Response response = f.get();
		output = response.getResponseBody();

		Response responce1 = f1.get();
		System.out.println(responce1);

		Gson gson = new Gson();
		Type listType = new TypeToken<List<OutputObject>>() {
		}.getType();
		List<OutputObject> myList = gson.fromJson(output, listType);

		return myList;
	}

	// not used in real just for local test before deployment
	/**
	 * @param name
	 * @return
	 */
	public List<OutputObject> getMatchesFromWorkers1(String name) {

		String url = "http://localhost:8080/SincreWorker/rest/files/start";
		Client client = Client.create();
		WebResource webResource = client.resource(url);

		ClientResponse response = webResource.type("application/json").post(
				ClientResponse.class, name);

		if (response.getStatus() != 201) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatus());
		}

		System.out.println("Output from Server .... \n");
		String output = response.getEntity(String.class);
		System.out.println(output);

		Gson gson = new Gson();

		Type listType = new TypeToken<List<OutputObject>>() {
		}.getType();
		List<OutputObject> myList = gson.fromJson(output, listType);

		System.out.println(myList);
		return myList;

	}

	/**
	 * genarates unique values from the results returned by the workers
	 * 
	 * @param objects
	 * @return
	 */
	public List<OutputObject> getUniqueValues(List<OutputObject> objects) {
		// System.out.println("came here");
		List<OutputObject> results = new ArrayList<OutputObject>();
		results.add(objects.get(0));

		String previousUrl = objects.get(0).getSummary()
				.substring(0, objects.get(0).getSummary().lastIndexOf('\\'));
		for (int i = 1; i < objects.size(); i++) {
			String url = objects
					.get(i)
					.getSummary()
					.substring(0, objects.get(i).getSummary().lastIndexOf('\\'));
			if (!url.equals(previousUrl)) {
				results.add(objects.get(i));
			}
			previousUrl = url;
		}
		return results;
	}

	/**
	 * call this method is the worker is running locally
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public List<OutputObject> getMatchesFromWorkers2(String name)
			throws IOException, InterruptedException, ExecutionException {

		String url = "http://localhost:8080/SincreWorker/rest/files/start/getMatches/"
				+ name;
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		String output = "";

		Future<Response> f = asyncHttpClient.prepareGet(url)
				.addQueryParameter("name", name)
				.execute(new AsyncCompletionHandler<Response>() {
					@Override
					public Response onCompleted(Response response)
							throws Exception {
						// Do something with the Response
						// ...
						System.out.println(response.getResponseBody());
						return response;
					}

					@Override
					public void onThrowable(Throwable t) {
						System.out.println("error");
						// Something wrong happened.
					}
				});

		System.out.println("made first call");

		Response responce = f.get();
		output = responce.getResponseBody();

		Gson gson = new Gson();

		Type listType = new TypeToken<List<OutputObject>>() {
		}.getType();
		List<OutputObject> myList = gson.fromJson(output, listType);

		System.out.println(myList);
		return myList;
		/*
		 * try { String url =
		 * "http://localhost:8080/SincreWorker/rest/files/start/getMatches/"
		 * +name; Client client = Client.create(); WebResource webResource =
		 * client.resource(url); ClientResponse response =
		 * webResource.type("application/json").get(ClientResponse.class);
		 * 
		 * if (response.getStatus() != 201) { throw new
		 * RuntimeException("Failed : HTTP error code : " +
		 * response.getStatus()); }
		 * 
		 * System.out.println("Output from Server .... \n"); String output =
		 * response.getEntity(String.class); System.out.println(output); return
		 * output;
		 * 
		 * } catch (Exception e) { e.printStackTrace(); } return null;
		 */
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