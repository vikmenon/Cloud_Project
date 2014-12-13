import com.ning.http.client.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


class JersyPython {


public void asyncCall(String name,String filename) throws IOException, InterruptedException, ExecutionException{
	//System.out.println("asynccall");
	long starttime = System.currentTimeMillis();
	
	String url = "http://localhost:5556/"+name+"/"+filename;
	AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	Future<Response> f = asyncHttpClient.prepareGet(url).addParameter("name", "MSFT").execute(new AsyncCompletionHandler<Response>(){
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
	Response responce = f.get();
	System.out.println(responce.getResponseBody());
	System.out.println((System.currentTimeMillis() - starttime));
}

}