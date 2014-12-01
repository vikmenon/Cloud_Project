import com.ning.http.client.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


class JersyTest {


public void asyncCall() throws IOException, InterruptedException, ExecutionException{
	//System.out.println("asynccall");
	String url = "http://localhost:8080/SincreWorker/rest/files/getMatches/search_image.png4894993f-4d54-461b-a621-c8865d0f6964";
	AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	Future<Response> f = asyncHttpClient.prepareGet(url).addQueryParameter("name", "search_image.png4894993f-4d54-461b-a621-c8865d0f6964").execute(new AsyncCompletionHandler<Response>(){

	    @Override
	    public Response onCompleted(Response response) throws Exception{
	        // Do something with the Response
	        // ...
	    	System.out.println("complete");
	    	System.out.println(response.getResponseBody());
	        return response;
	    }

	    @Override
	    public void onThrowable(Throwable t){
	    	System.out.println("error");
	        // Something wrong happened.
	    }
	});
	Response response = f.get();
	   
	   
	   
}

}