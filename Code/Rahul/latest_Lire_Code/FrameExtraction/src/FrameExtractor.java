import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

public class FrameExtractor {
    
	static int FRAME_INTERVAL = 5; //in seconds
    static String inputVideoURL = "C:\\Users\\Rahul\\Desktop\\LireBasedCloudProject\\Videos";
    static String outputFrameLocation = "C:\\Users\\Rahul\\Desktop\\LireBasedCloudProject\\AllFrames\\";
    static int VideoStreamStartIndex = 4;
    static String currentVideoName = "";
    static long mLastPtsWrite = Global.NO_PTS;
    static int counter=0;

    public static final long MICRO_SECOND_INTERVAL = Global.DEFAULT_PTS_PER_SECOND * FRAME_INTERVAL;

    public void extract(String[] args) {
    	File videoFolder = new File(inputVideoURL);
		for (final File fileEntry : videoFolder.listFiles()) {
    		if(fileEntry.getName().equals("Thumbs.db")){
    			continue;
    		}
    		System.out.println("reading file" + fileEntry.getAbsolutePath());
    		currentVideoName = fileEntry.getName();
    		if(!createImageDirectory()){
    			continue;
    		}
    		IMediaReader mediaReader = ToolFactory.makeReader(inputVideoURL + "\\" + fileEntry.getName());
    		System.out.println(mediaReader.isOpen());
    		System.out.println(mediaReader.getUrl());
    		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
    		mediaReader.addListener(new ImageSnapListener());
    		while (mediaReader.readPacket() == null) ;
    		break;
        }
    }
    public class ImageSnapListener extends MediaListenerAdapter {
        public void onVideoPicture(IVideoPictureEvent event) {
            if (event.getStreamIndex() != VideoStreamStartIndex) {
                if (VideoStreamStartIndex == 5)
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
                String outputFilename = outputFrameLocation + currentVideoName +"\\" + System.currentTimeMillis() + ".jpg";
                ImageIO.write(image, "jpg", new File(outputFilename));
                return outputFilename;
            } 
            catch (Exception e) {
            	System.out.println("Error !");
            	return "Found Exception";
            }
        }

    }

   
	/**
	 * @return
	 */
	private static boolean createImageDirectory() {
		File theDir = new File(outputFrameLocation + currentVideoName);
		  if (!theDir.exists()) {
		    System.out.println("creating directory: " + currentVideoName);
		    boolean result = false;
		    try{
		        theDir.mkdir();
		        result = true;
		     } catch(SecurityException se){
		     }        
		     if(result) {    
		       System.out.println("DIR created");
		       return true;
		     }
		  }
		  return false;
	}
}
