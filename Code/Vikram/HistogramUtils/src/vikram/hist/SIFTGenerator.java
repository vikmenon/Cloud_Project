package vikram.hist;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImageM;

import java.io.File;
import java.util.Arrays;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_features2d.DescriptorExtractor;
import com.googlecode.javacv.cpp.opencv_features2d.FeatureDetector;
import com.googlecode.javacv.cpp.opencv_features2d.KeyPoint;
import com.googlecode.javacv.cpp.opencv_nonfree.SIFT;

public class SIFTGenerator {
	public static final String imageBasePath = "C:/Users/vikmenon/Desktop/SIFT/";
	public static final String imageFilePath = imageBasePath + "test.jpg";

	static FeatureDetector featureDetector;
	static DescriptorExtractor descriptorExtractor;

	public static void main(String[] args) {
		SIFT sift = new SIFT(); //128, ?, ?, ?, ?
		FeatureDetector featureDetector = sift.getFeatureDetector();
		descriptorExtractor = DescriptorExtractor.create("SIFT");
		
		File file = new File(imageFilePath);
		CvMat image = cvLoadImageM(file.getAbsolutePath(),
				CV_LOAD_IMAGE_GRAYSCALE);
		
		if (image == null)
			System.out.println("ERR: Image was null!");
		
		KeyPoint keypoints = new KeyPoint(null);
		featureDetector.detect(image, keypoints, null);
		CvMat featuresM = new CvMat(null);
		descriptorExtractor.compute(image, keypoints, featuresM);
		
		// Read out the values
		double[] feature128UniVector = featuresM.get();
		double[][] feature128Vectors = arrayChunker(feature128UniVector);
		
		// Print out the SIFT features
		// System.out.println(featuresM);
		for (double[] vector : feature128Vectors) {
			System.out.println(Arrays.toString(vector));
		}
	}
	
	private static double[][] arrayChunker(double[] origVals) {
		int chunkSize = 128;  // chunk size
		int pieces = origVals.length / chunkSize;
		double[][] newArrays = new double[pieces][128];
		
		int len = origVals.length;
		int counter = 0;
		
		for (int i = 0; i < len - chunkSize + 1; i += chunkSize)
		    newArrays[counter++] = Arrays.copyOfRange(origVals, i, i + chunkSize);

		if (len % chunkSize != 0)
		    newArrays[counter] = Arrays.copyOfRange(origVals, len - len % chunkSize, len);
		
		return newArrays;
	}
}