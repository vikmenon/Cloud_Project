package com.javacodegeeks.enterprise.rest.jersey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

@Path("/files")
public class JerseyFileUpload {

	/**
	 * Upload a File
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 *//*

	@POST
	@Path("/upload")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String uploadFile(
			@FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws JsonGenerationException, JsonMappingException, IOException {
		String filePath = SERVER_UPLOAD_LOCATION_FOLDER
				+ contentDispositionHeader.getFileName();
		System.out.println(filePath);
		uploadedFilePath = filePath;
		ObjectMapper mapper = new ObjectMapper();
		// save the file to the server
		saveFile(fileInputStream, filePath);
		return mapper.writeValueAsString(filePath);
	}*/
	
	@POST
	@Path("/start")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTrackInJSON(String name) throws JsonGenerationException, JsonMappingException, IOException {
		/*String result = path;
		File file = new File("C:\\Users\\Rahul\\Desktop\\temp");
		try {
			OutputStream outpuStream = new FileOutputStream(file);
			outpuStream.write(result.getBytes());
			outpuStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Response.status(201).entity(result).build();*/
		System.out.println(name);
		List<OutputObject> objects = new ArrayList<OutputObject>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			System.out.println("before search method");
			S3Downloader downloader = new S3Downloader();
			String path = downloader.downloadImage(name);
			objects = Searcher.searchImage(path);
			System.out.println("after search method");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.status(201).entity(mapper.writeValueAsString(objects)).build();
	}

	
	@GET
	@Path("/getMatches/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getResults(@PathParam("name") String name) throws JsonGenerationException, JsonMappingException, IOException{
		System.out.println(name);
		List<OutputObject> objects = new ArrayList<OutputObject>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			long starttime = System.currentTimeMillis();
			S3Downloader downloader = new S3Downloader();
			String path = downloader.downloadImage(name);
			long middletime = System.currentTimeMillis();
			System.out.println("time to download and save from s3 :: " + (middletime-starttime));
			objects = Searcher.searchImage(path);
			System.out.println("time to search :: " + (System.currentTimeMillis() - middletime));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapper.writeValueAsString(objects);
	}

	
	// save uploaded file to a defined location on the server
	public void saveFile(InputStream uploadedInputStream, String serverLocation) {
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