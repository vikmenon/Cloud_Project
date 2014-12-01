import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class Main {
	
			
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		System.out.println("hello world");
		JersyTest jersyupload = new JersyTest();
			//System.out.println(jersyupload.upload(url, new File(uploadFile)));
			jersyupload.asyncCall();
	}
}
