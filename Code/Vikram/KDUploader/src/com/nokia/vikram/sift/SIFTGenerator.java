package com.nokia.vikram.sift;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImageM;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_features2d.DescriptorExtractor;
import com.googlecode.javacv.cpp.opencv_features2d.FeatureDetector;
import com.googlecode.javacv.cpp.opencv_features2d.KeyPoint;
import com.googlecode.javacv.cpp.opencv_nonfree.SIFT;

public class SIFTGenerator {
	public static final String SiftMetadataDir = "SiftMetadataDir";

	private static final String SiftMetadataExtension = ".xml";
	private static final String testImg = "C:/Users/vikmenon/Desktop/SIFT/test.jpg";

	private static SIFT sift = null;
	private static FeatureDetector featureDetector;
	private static DescriptorExtractor descriptorExtractor;

	static {
		sift = new SIFT();
		featureDetector = sift.getFeatureDetector();
		descriptorExtractor = DescriptorExtractor.create("SIFT");
	}

	public static void generateMetadataForFrames(String reducedframesdir,
			String siftmetadatadirArg) {
		Set<String> foldersCreated = new HashSet<String>();

		// Iterate over movies
		for (File firstLevel : new File(reducedframesdir).listFiles()) {
			System.out.println("Generating metadata for movie: " + firstLevel.getName());
			
			if (firstLevel.isDirectory()) {
				// Iterate over frames
				for (File secondLevel : firstLevel.listFiles()) {
					if (!secondLevel.getName().contains("thumbs.db")) {
						int[][] result128Vectors = getMetadata(secondLevel);

						// Ensure parent directories are created
						File outFile = new File(SiftMetadataDir
								+ '/' + firstLevel.getName()
								+ '/' + secondLevel.getName()
								+ SiftMetadataExtension);
						if (! foldersCreated.contains(firstLevel.getName())) {
							outFile.getParentFile().mkdirs();
							foldersCreated.add(firstLevel.getName());
						}

						// Write to a metadata file, with <data></data> tags
						PrintWriter writer = null;
						try {
							System.out.println("Attempting to write to: " + outFile.getAbsolutePath());
							writer = new PrintWriter(outFile, "US-ASCII");
						} catch (IOException e) {
							System.out.println("ERR: Could not create PrintWriter for frame: " + secondLevel.getName());
						}
						
						// Output one vector per line, between the open and close 'data' tag
						writer.println("<data>");
						writer.println();
						
						for (int[] vector : result128Vectors) {
							String bufferedStr = " ";
							for (int value : vector) {
								bufferedStr += String.valueOf(value) + " ";
							}
							writer.println(bufferedStr);
						}
						
						writer.println(" </data>");
						writer.println();
						writer.close();
					}
				}
			}
			
			System.out.println("Finished generating metadata for movie: " + firstLevel.getName());
		}
	}

	public static void main(String[] args) {
		int[][] result128Vectors = getMetadata(testImg);

		// Print out the SIFT features
		for (int[] vector : result128Vectors) {
			System.out.println(Arrays.toString(vector));
		}
	}

	private static int[][] getMetadata(String imageFilePath) {
		return getMetadata(new File(imageFilePath));
	}

	private static int[][] getMetadata(File imageFile) {
		CvMat image = cvLoadImageM(imageFile.getAbsolutePath(),
				CV_LOAD_IMAGE_GRAYSCALE);

		if (image == null) {
			System.out.println("ERR: Image was null!");
			return null;
		}
		int[][] feature128Vectors = new int[1][128];
		try {
		KeyPoint keypoints = new KeyPoint(null);
		featureDetector.detect(image, keypoints, null);
		CvMat featuresM = new CvMat(null);
		descriptorExtractor.compute(image, keypoints, featuresM);

		// Read out the values
		double[] allFeaturesVector = featuresM.get();
		feature128Vectors = arrayChunker(allFeaturesVector);
		} catch (NullPointerException e) {
			
		}

		return feature128Vectors;
	}
	
	private static int[] intArray(double[] arr) {
		int[] retArr = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			retArr[i] = (int) arr[i];
		}
		return retArr;
	}
	
	private static int[][] arrayChunker(double[] origVals) {
		int chunkSize = 128; // chunk size
		int pieces = origVals.length / chunkSize;
		int[] intOrigVals = intArray(origVals);
		int[][] newArrays = new int[pieces][128];

		int len = origVals.length;
		int counter = 0;

		for (int i = 0; i < len - chunkSize + 1; i += chunkSize)
			newArrays[counter++] = Arrays.copyOfRange(intOrigVals, i, i
					+ chunkSize);

		if (len % chunkSize != 0)
			newArrays[counter] = Arrays.copyOfRange(intOrigVals, len - len
					% chunkSize, len);

		return newArrays;
	}
}