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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/files")
public class JerseyFileUpload {

    //private static final String SERVER_UPLOAD_LOCATION_FOLDER = "/home/ec2-user/download/";
    private static final String SERVER_UPLOAD_LOCATION_FOLDER = "C:\\Users\\Rahul\\Desktop\\download\\";
    private String videoName = "";
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
        
        try{
        String fileName = contentDispositionHeader.getFileName();
        String filePath = SERVER_UPLOAD_LOCATION_FOLDER + fileName;
        videoName = fileName;

        //uploadedFilePath = filePath;
        ObjectMapper mapper = new ObjectMapper();
        // save the file to the server
        saveFile(fileInputStream, filePath);
        
        ThumbsGenerator thgen = new ThumbsGenerator();
        String name = thgen.generateFrames(SERVER_UPLOAD_LOCATION_FOLDER + fileName, fileName);
        
        return mapper.writeValueAsString(videoName+"-"+name);
        
        } catch(Exception e){
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
    public String getResults(@PathParam("name") String name) throws JsonGenerationException, JsonMappingException, IOException{
        try{
            System.out.println("Name of the file to upload::" + name);
            List<OutputObject> objects = new ArrayList<OutputObject>();
            ObjectMapper mapper = new ObjectMapper();
            try {
                System.out.println("before search method");
                System.out.println(SERVER_UPLOAD_LOCATION_FOLDER + "frames\\" + name);
                objects = Searcher.searchImage(SERVER_UPLOAD_LOCATION_FOLDER + "frames\\" + name.split("-")[0] + "\\" +name.split("-")[1]);
                //objects = getMatchesFromWorkers(name);
                File directory = new File(SERVER_UPLOAD_LOCATION_FOLDER + "frames\\" + name.split("-")[0]);
                delete(directory);
                
                System.out.println("after search method");
                
            } catch (Exception e) {
            }
            
            Gson gson = new Gson();
            return mapper.writeValueAsString(gson.toJson(objects));
        } catch(Exception e){
            return "no data";
        }
    }

    
    public static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            // directory is empty, then delete it
            if (file.list().length == 0) {
                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());
            } else {
                // list all the directory contents
                String files[] = file.list();
                for (String temp : files) {
                    // construct the file structure
                    File fileDelete = new File(file, temp);
                    // recursive delete
                    delete(fileDelete);
                }
                // check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }
        } else {
            // if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath());
        }
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