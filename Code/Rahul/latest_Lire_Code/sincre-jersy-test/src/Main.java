import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class Main {
	
			
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		System.out.println("hello world");
		JersyPython jersyupload = new JersyPython();
			//System.out.println(jersyupload.upload(url, new File(uploadFile)));
			 jersyupload.asyncCall("MSFT","filename");
		//String url = "C:\\Users\\Rahul\\Desktop\\LireBasedCloudProject\\FinalFrames\\4.mp4\\1415061393414.jpg";	
		//System.out.println(url);
		//System.out.println(url.substring(0, url.lastIndexOf('\\')));
		
	}
}
