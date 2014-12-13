import java.awt.Color;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

public class SimilarFrameEliminator {
	private Color[][] signature;
	private static final int baseSize = 300;
	private static final String basePath = "C:\\Users\\Rahul\\Desktop\\LireBasedCloudProject\\AllFrames\\";
	private static final String finalPath = "C:\\Users\\Rahul\\Desktop\\LireBasedCloudProject\\FinalFrames\\";
	public static File dir = new File(basePath);
	public static ArrayList<File> allFiles = new ArrayList<>(Arrays.asList(dir.listFiles(new JPEGImageFileFilter())));

	public SimilarFrameEliminator() throws IOException {
		for (int i=0;i<allFiles.size()-1;i++) {
			File reference  = allFiles.get(i);
			RenderedImage ref = rescale(ImageIO.read(reference));
			signature = calcSignature(ref);
			
			RenderedImage[] rothers = new RenderedImage[allFiles.size()];
			double[] distances = new double[allFiles.size()];
			ArrayList<Integer> removeIndex = new ArrayList<>() ;
			int stop = i+30;
			if(i+30 > allFiles.size()) 
				stop = allFiles.size();
			else
				stop = i+30;
			for (int o = i+1; o < stop; o++) {
					rothers[o] = rescale(ImageIO.read(allFiles.get(o)));
					distances[o] = calcDistance(rothers[o]);
					if(distances[o] < 900L){
						System.out.println(100 - ((distances[o] / 11000)*100) + " percent similarity found");
						removeIndex.add(o);
					}
			}
			System.out.println("Removing " + removeIndex.size() + "similar images");
			for(int remi=0;remi<removeIndex.size();remi++){
				if(allFiles.size() > (int)removeIndex.get(remi))
				allFiles.remove((int)removeIndex.get(remi));
				System.out.println("Removed");
			}
			System.out.println("Size after Removal " + allFiles.size());
		}
	}

	private RenderedImage rescale(RenderedImage i) {
		float scaleW = ((float) baseSize) / i.getWidth();
		float scaleH = ((float) baseSize) / i.getHeight();
		// Scales the original image
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(i);
		pb.add(scaleW);
		pb.add(scaleH);
		pb.add(0.0F);
		pb.add(0.0F);
		pb.add(new InterpolationNearest());
		// Creates a new, scaled image and uses it on the DisplayJAI component
		return JAI.create("scale", pb);
	}

	/*
	 * This method calculates and returns signature vectors for the input image.
	 */
	private Color[][] calcSignature(RenderedImage i) {
		Color[][] sig = new Color[5][5];
		float[] prop = new float[] { 1f / 10f, 3f / 10f, 5f / 10f, 7f / 10f,
				9f / 10f };
		for (int x = 0; x < 5; x++)
			for (int y = 0; y < 5; y++)
				sig[x][y] = averageAround(i, prop[x], prop[y]);
		return sig;
	}

	private Color averageAround(RenderedImage i, double px, double py) {
		RandomIter iterator = RandomIterFactory.create(i, null);
		double[] pixel = new double[3];
		double[] accum = new double[3];
		int sampleSize = 15;
		int numPixels = 0;
		for (double x = px * baseSize - sampleSize; x < px * baseSize
				+ sampleSize; x++) {
			for (double y = py * baseSize - sampleSize; y < py * baseSize
					+ sampleSize; y++) {
				iterator.getPixel((int) x, (int) y, pixel);
				accum[0] += pixel[0];
				accum[1] += pixel[1];
				accum[2] += pixel[2];
				numPixels++;
			}
		}
		accum[0] /= numPixels;
		accum[1] /= numPixels;
		accum[2] /= numPixels;
		return new Color((int) accum[0], (int) accum[1], (int) accum[2]);
	}

	private double calcDistance(RenderedImage other) {
		Color[][] sigOther = calcSignature(other);
		double dist = 0;
		for (int x = 0; x < 5; x++)
			for (int y = 0; y < 5; y++) {
				int r1 = signature[x][y].getRed();
				int g1 = signature[x][y].getGreen();
				int b1 = signature[x][y].getBlue();
				int r2 = sigOther[x][y].getRed();
				int g2 = sigOther[x][y].getGreen();
				int b2 = sigOther[x][y].getBlue();
				double tempDist = Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2)
						* (g1 - g2) + (b1 - b2) * (b1 - b2));
				dist += tempDist;
			}
		return dist;
	}
	
	private static boolean createImageDirectory(String outputFrameLocation , String currentVideoName) {
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
	
	public static void main(String[] args) throws IOException {
		File allFrameFolder = new File(basePath);
		String videoName = "";
		for (final File fileEntry : allFrameFolder.listFiles()) {
    		if(fileEntry.getName().equals("Thumbs.db")){
    			continue;
    		}
    		videoName = fileEntry.getName();
    		dir = new File(fileEntry.getAbsolutePath());
    		allFiles = new ArrayList<>(Arrays.asList(dir.listFiles(new JPEGImageFileFilter())));
    		
    		System.out.println("Total Initial frame count "+allFiles.size());
    		new SimilarFrameEliminator();
    		System.out.println("Final frame count "+allFiles.size());
    		createImageDirectory(finalPath,videoName);
    		for(int i=0;i<allFiles.size();i++){
    			//allFiles.get(i).renameTo(new File(finalPath + videoName + "\\" + allFiles.get(i).getName()));
    			Files.copy(allFiles.get(i).toPath(), new File(finalPath + videoName + "\\" + allFiles.get(i).getName()).toPath());
    		}
		}
	}
}