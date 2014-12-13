import com.ning.http.client.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


class JersyTest {


public void asyncCall() throws IOException, InterruptedException, ExecutionException{
	//System.out.println("asynccall");
	long starttime = System.currentTimeMillis();
	
	String url = "http://ec2-54-88-121-180.compute-1.amazonaws.com:8080/SincreWorker/rest/files/getMatches/1415061165075.jpg17082db0-ecee-4da1-bfab-2afccd3c9101.png";
	AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	Future<Response> f = asyncHttpClient.prepareGet(url).addQueryParameter("name", "1415061165075.jpg17082db0-ecee-4da1-bfab-2afccd3c9101.png").execute(new AsyncCompletionHandler<Response>(){
	int count = 0;
	    @Override
	    public Response onCompleted(Response response) throws Exception{
	        // Do something with the Response
	        // ...
	    	System.out.println("complete1");
	    	count++;
	    	System.out.println(response.getResponseBody());
	        return response;
	    }

	    @Override
	    public void onThrowable(Throwable t){
	    	System.out.println(t.getMessage());
	    	System.out.println("error");
	        // Something wrong happened.
	    }
	});
	System.out.println("made first call");
	/*url = "http://localhost:8080/SincreWorker/rest/files/getMatches/search_image.png4894993f-4d54-461b-a621-c8865d0f6964";
	AsyncHttpClient asyncHttpClient1 = new AsyncHttpClient();
	Future<Response> f1 = asyncHttpClient1.prepareGet(url).addQueryParameter("name", "search_image.png4894993f-4d54-461b-a621-c8865d0f6964").execute(new AsyncCompletionHandler<Response>(){

	    @Override
	    public Response onCompleted(Response response) throws Exception{
	        // Do something with the Response
	        // ...
	    	System.out.println("complete2");
	    	System.out.println(response.getResponseBody());
	        return response;
	    }

	    @Override
	    public void onThrowable(Throwable t){
	    	System.out.println("error");
	        // Something wrong happened.
	    }
	});
	System.out.println("finished second call");
	url = "http://localhost:8080/SincreWorker/rest/files/getMatches/search_image.pnga4d11dd2-5b89-4028-b0e3-455b2534376a";
	AsyncHttpClient asyncHttpClient2 = new AsyncHttpClient();
	Future<Response> f2 = asyncHttpClient2.prepareGet(url).addQueryParameter("name", "search_image.pnga4d11dd2-5b89-4028-b0e3-455b2534376a").execute(new AsyncCompletionHandler<Response>(){

	    @Override
	    public Response onCompleted(Response response) throws Exception{
	        // Do something with the Response
	        // ...
	    	System.out.println("complete2");
	    	System.out.println(response.getResponseBody());
	        return response;
	    }

	    @Override
	    public void onThrowable(Throwable t){
	    	System.out.println("error");
	        // Something wrong happened.
	    }
	});
	System.out.println("finished second call");

	url = "http://localhost:8080/SincreWorker/rest/files/getMatches/search_image.png4ff2ce73-4d40-429f-9b9d-5c44676e3f98";
	AsyncHttpClient asyncHttpClient3 = new AsyncHttpClient();
	Future<Response> f3 = asyncHttpClient3.prepareGet(url).addQueryParameter("name", "search_image.png4ff2ce73-4d40-429f-9b9d-5c44676e3f98").execute(new AsyncCompletionHandler<Response>(){

	    @Override
	    public Response onCompleted(Response response) throws Exception{
	        // Do something with the Response
	        // ...
	    	System.out.println("complete2");
	    	System.out.println(response.getResponseBody());
	        return response;
	    }

	    @Override
	    public void onThrowable(Throwable t){
	    	System.out.println("error");
	        // Something wrong happened.
	    }
	});
	System.out.println("finished second call");

	url = "http://localhost:8080/SincreWorker/rest/files/getMatches/search_image.png618d13e4-3dbd-4b74-aa3a-01ca4895db91";
	AsyncHttpClient asyncHttpClient4 = new AsyncHttpClient();
	Future<Response> f4 = asyncHttpClient4.prepareGet(url).addQueryParameter("name", "search_image.png618d13e4-3dbd-4b74-aa3a-01ca4895db91").execute(new AsyncCompletionHandler<Response>(){

	    @Override
	    public Response onCompleted(Response response) throws Exception{
	        // Do something with the Response
	        // ...
	    	System.out.println("complete2");
	    	System.out.println(response.getResponseBody());
	        return response;
	    }

	    @Override
	    public void onThrowable(Throwable t){
	    	System.out.println("error");
	        // Something wrong happened.
	    }
	});
	System.out.println("finished second call");
*/
	Response responce = f.get();
	//Response responce1 = f1.get();
	//Response responce2 = f2.get();
	//Response responce3 = f3.get();
	//Response responce4 = f4.get();
	
	System.out.println(responce.getResponseBody());
	//System.out.println(responce1.getResponseBody());
	//System.out.println(responce2.getResponseBody());
	//System.out.println(responce4.getResponseBody());
	//System.out.println(responce1.getResponseBody());
	//System.out.println(responce.getResponseBody());
	System.out.println((System.currentTimeMillis() - starttime));
	//Response response = f.get();
	   
	   
	   
}

}