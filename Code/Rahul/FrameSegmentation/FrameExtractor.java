import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

public class FrameExtractor {
    
	static int FRAME_INTERVAL = 1; //in seconds
    static String inputVideoURL = "c:/sample.mp4";
    static String outputFrameLocation = "c:/snapshots2/";
    static int VideoStreamStartIndex = -1;
    //system param don't understand
    static long mLastPtsWrite = Global.NO_PTS;
    static int counter=0;

    public static final long MICRO_SECOND_INTERVAL = Global.DEFAULT_PTS_PER_SECOND * FRAME_INTERVAL;

    public static void main(String[] args) {
        IMediaReader mediaReader = ToolFactory.makeReader(inputVideoURL);
        mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        mediaReader.addListener(new ImageSnapListener());
        while (mediaReader.readPacket() == null) ;
    }

    private static class ImageSnapListener extends MediaListenerAdapter {
        public void onVideoPicture(IVideoPictureEvent event) {
            if (event.getStreamIndex() != VideoStreamStartIndex) {
                if (VideoStreamStartIndex == -1)
                    VideoStreamStartIndex = event.getStreamIndex();
                else
                    return;
            }
            if (mLastPtsWrite == Global.NO_PTS)
                mLastPtsWrite = event.getTimeStamp() - MICRO_SECOND_INTERVAL;
            if (event.getTimeStamp() - mLastPtsWrite >= 
                    MICRO_SECOND_INTERVAL) {
                String outputFilename = dumpImageToFile(event.getImage());
                System.out.println("Index::" + (counter++) + "  FileName:"+outputFilename );
                mLastPtsWrite += MICRO_SECOND_INTERVAL;
            }
        }
        
        private String dumpImageToFile(BufferedImage image) {
            try {
                String outputFilename = outputFrameLocation + System.currentTimeMillis() + ".jpg";
                ImageIO.write(image, "jpg", new File(outputFilename));
                return outputFilename;
            } 
            catch (Exception e) {
            	System.out.println("Error !");
            	return "Found Exception";
            }
        }

    }

}