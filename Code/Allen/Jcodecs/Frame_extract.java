
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;


public class Frame_extract {

	  
    public static void main(String[] args) throws IOException, JCodecException {


long time = System.currentTimeMillis();

for (int i = 50; i < 57; i++) { 

BufferedImage frame = FrameGrab.getFrame(new File("/Users/allenstarke/Desktop/test_videos/sample.mp4"), i);

ImageIO.write(frame, "bmp", new File("/Users/allenstarke/Desktop/frames/frame_"+i+".bmp"));

}

System.out.println("Time Used:" + (System.currentTimeMillis() - time)+" Milliseconds");


    }
}
