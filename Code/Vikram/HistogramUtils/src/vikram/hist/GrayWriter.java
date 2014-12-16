package vikram.hist;

/**
 * @Author Kushal Paudyal
 * www.sanjaal.com/java
 * Last Modified On: 2009-09-28
 *
 * Utility to convert a colored image into gray color.
 */
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

public class GrayWriter {
	private static final String basePath = "C:/Users/vikmenon/Desktop/SIFT/";

	public static void main(String args[]) {
		GrayWriter imageGrayer = new GrayWriter();
		String inputImageFilePath = basePath + "test_2.jpg";
		String outputImageFilePath = basePath + "test-gray-output.jpg";

		System.out.println("Reading input image...");
		BufferedImage inputImage = imageGrayer.readImage(inputImageFilePath);
		System.out.println("Successfully Read Image: " + inputImageFilePath);

		System.out.println("\nConverting the image to Gray colors.");
		BufferedImage grayedOut = imageGrayer.grayOut(inputImage);
		System.out.println("Successful...");

		System.out.println("\nConverting the image to PGM format.");
		writePGM(
				getArrayFromImage(grayedOut)
			);
		System.out.println("Successful...");
		
		System.out.println("\nWriting gray image to filesystems.");
		imageGrayer.writeImage(grayedOut, outputImageFilePath, "jpg");
		System.out.println("Successfully Wrote Image To: "
				+ outputImageFilePath);
	}
	
	private static int[][] getArrayFromImage(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		Raster raster = image.getData();
		int[][] arr = new int[width][height];
		
		for (int j = 0; j < width; j++) {
		    for (int k = 0; k < height; k++) {
		        arr[j][k] = raster.getSample(j, k, 0);
		    }
		}
		
		return arr;
	}
	
	private static void writePGM(int[][] arr) {
		 try {
			 int width = arr.length;
			 int height = arr[0].length;
			 
		     //specify the name of the output..
		     FileWriter fstream = new FileWriter(basePath + "output.pgm");
		     //we create a new BufferedWriter
		     BufferedWriter out = new BufferedWriter(fstream);
		     //we add the header, 128 128 is the width-height and 63 is the max value-1 of ur data
		     out.write("P2\n# CREATOR: XV Version 3.10a  Rev: 12/29/94\n" + width + " " + height + "\n63\n");
		     //2 loops to read the 2d array
	    	 System.out.println("ARR WIDTH WAS " + width);
	    	 System.out.println("ARR HEIGHT WAS " + height);
		     for(int i = 0 ; i < width;i++) {
		        for(int j = 0 ; j < height;j++) {
		            //we write in the output the value in the position ij of the array
		            out.write(arr[i][j]+" ");
		        }
		        out.write('\n');
		     }
		     //we close the bufferedwritter
		     out.close();
		     }
		catch (Exception e){
		     System.err.println("Error : " + e.getMessage());
		}
	}
	
	/**
	 * This method converts any input BufferedImage object to the gray color and
	 * returns it.
	 */
	public BufferedImage grayOut(BufferedImage colorFrame) {
		BufferedImage grayFrame = new BufferedImage(colorFrame.getWidth(), colorFrame.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		ColorConvertOp colorConvert = new ColorConvertOp(
				colorFrame.getColorModel().getColorSpace(),
				grayFrame.getColorModel().getColorSpace(), null);
		colorConvert.filter(colorFrame, grayFrame);

		return grayFrame;
	}

	/**
	 * This method reads an image from the file
	 * 
	 * @param fileLocation
	 *            -- > eg. "C:/testImage.jpg"
	 * @return BufferedImage of the file read
	 */
	public BufferedImage readImage(String fileLocation) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(fileLocation));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	/**
	 * This method writes a buffered image to a file
	 * 
	 * @param img
	 *            -- > BufferedImage
	 * @param fileLocation
	 *            --> e.g. "C:/testImage.jpg"
	 * @param extension
	 *            --> e.g. "jpg","gif","png"
	 */
	public void writeImage(BufferedImage img, String fileLocation,
			String extension) {
		try {
			BufferedImage bi = img;
			File outputfile = new File(fileLocation);
			ImageIO.write(bi, extension, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}