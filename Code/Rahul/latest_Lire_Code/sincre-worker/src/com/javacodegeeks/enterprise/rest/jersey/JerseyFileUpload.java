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
	
	@GET
	@Path("/getMatches/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getResults(@PathParam("name") String name) throws JsonGenerationException, JsonMappingException, IOException{
		try{
			
			List<OutputObject> objects = new ArrayList<OutputObject>();
			ObjectMapper mapper = new ObjectMapper();
			try {

				S3Downloader downloader = new S3Downloader();
				String path = downloader.downloadImage(name);
			
				//String path = SERVER_UPLOAD_LOCATION_FOLDER + name;
			objects = Searcher.searchImage(path);
			//System.out.println("time to search :: " + (System.currentTimeMillis() - middletime));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapper.writeValueAsString(getUniqueValues(objects));
	} catch(Exception e){
		e.printStackTrace();
		return "no data";
	}
	}
	
	private List<OutputObject> getUniqueValues(List<OutputObject> objects){
		//System.out.println("came here");
		List<OutputObject> results = new ArrayList<OutputObject>();
		results.add(objects.get(0));
		
		String previousUrl = objects.get(0).getSummary().substring(0, objects.get(0).getSummary().lastIndexOf('\\'));
		for(int i=1;i<objects.size();i++){
			String url = objects.get(i).getSummary().substring(0, objects.get(i).getSummary().lastIndexOf('\\'));
			if(!url.equals(previousUrl)){
				results.add(objects.get(i));
			}
			previousUrl = url;
		}
		return results;
	}
		
		
		
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
	
	@POST
	@Path("/start")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTrackInJSON(String name) throws JsonGenerationException, JsonMappingException, IOException {
		
		try{
		List<OutputObject> objects = new ArrayList<OutputObject>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			//System.out.println("before search method");
			S3Downloader downloader = new S3Downloader();
			String path = downloader.downloadImage(name);
			objects = Searcher.searchImage(path);
			//System.out.println("after search method");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.status(201).entity(mapper.writeValueAsString(objects)).build();
	} catch(Exception e){
		return Response.status(500).build();
	}
	}
}