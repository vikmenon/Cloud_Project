
package vikram.hist;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_COMP_INTERSECT;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_HIST_ARRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCalcHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCompareHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCreateHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetMinMaxHistValue;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvNormalizeHist;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvHistogram;

public class HistogramUtils {
	private static final String imageDBDirectory = "C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/CBVRProject/Images/";
	
	private static final String supportedFormats = "(bmp|pbm|pgm|ppm|sr|ras|jpeg|jpg|jpe|jp2|tiff|tif|png)";
	
	private static final String[] queryImages = {
			imageDBDirectory + "cat.jpg",
			imageDBDirectory + "cat1.jpg",
			imageDBDirectory + "cat2.jpg",
			imageDBDirectory + "cat3.jpg",
			imageDBDirectory + "dave.jpg",
			imageDBDirectory + "flower.jpg",
			imageDBDirectory + "lena.jpg",
			imageDBDirectory + "scene0.jpg"
		};
	
	private static CvHistogram getHueHistogram(IplImage image) {
		if (image == null || image.nChannels() < 1) {
			new Exception("Invalid image, could not get Histogram!");
		}

		// Populate a greyscale image
		IplImage greyImage = cvCreateImage(image.cvSize(), image.depth(), 1);
		cvCvtColor(image, greyImage, CV_RGB2GRAY);

		// Histogram: number of bins and range of values
		int numberOfBins = 256;
		float rangeMinVal = 0f;
		float rangeMaxVal = 255f;

		// Allocate histogram object
		int histogramType = CV_HIST_ARRAY;
		int histogramNoOfDimensions = 1;
		int uniformBins = 1;

		int[] histogramDimensions = new int[] { numberOfBins };
		float[] rangeDefinition = new float[] { rangeMinVal, rangeMaxVal };
		float[][] dimensionalRanges = new float[][] { rangeDefinition };

		// Compute histogram
		CvHistogram hist = cvCreateHist(histogramNoOfDimensions,
				histogramDimensions, histogramType, dimensionalRanges,
				uniformBins);

		IplImage[] auxilary = new IplImage[] { greyImage };
		int accumulate = 0;
		cvCalcHist(auxilary, hist, accumulate, null);

		// Normalize our histogram
		cvNormalizeHist(hist, 1);

		// Read out Min and Max values from the Histogram
		cvGetMinMaxHistValue(hist, rangeDefinition, rangeDefinition,
				histogramDimensions, histogramDimensions);
		return hist;
	}

	private static List<String> listFilesInDirectory(String directoryPath) {
		List<String> returnVal = new ArrayList<String>();
		
		File[] dirLs = (new File(directoryPath)).listFiles();

		for (File currentFile : dirLs) {
			if (currentFile.isFile()
				&& currentFile.getName().matches(".*\\." + HistogramUtils.supportedFormats)) {
				returnVal.add(currentFile.getAbsolutePath());
			}
		}
		
		return returnVal;
	}

	public static void main(String[] args) throws Exception {
		Map<String, Double> percentageMatches = new HashMap<String, Double>();
		
		// Input: Images to compare
		String queryImageFile = queryImages[0];
		System.out.println("\nQuery file is: \n\t" + queryImageFile);
		
		List<String> dbImageFiles = listFilesInDirectory(imageDBDirectory);
		
		for (String dbImageFile : dbImageFiles) {
			IplImage queryImage = cvLoadImage(queryImageFile);
			IplImage dbImage = cvLoadImage(dbImageFile);

			CvHistogram queryImageHistogram = getHueHistogram(queryImage);
			CvHistogram dbImageHistogram = getHueHistogram(dbImage);

			double matchPercentage = Math.floor(100 * cvCompareHist(
					queryImageHistogram, dbImageHistogram, CV_COMP_INTERSECT));
			percentageMatches.put(dbImageFile, matchPercentage);
		}

		printSearchResults(getSortedSearchResults(percentageMatches));
	}

	private static void printSearchResults(
			Set<Map.Entry<String, Double>> searchResults) {
		System.out.println("\n=====================================");
		System.out.println("The image results, in sorted order :-\n");
		for (Map.Entry<String, Double> entry : searchResults) {
			System.out.println(entry.getKey() + "\n\tPercentage: "
					+ entry.getValue());
		}
	}

	private static Set<Map.Entry<String, Double>> getSortedSearchResults(
			Map<String, Double> mapArg) {
		SortedSet<Map.Entry<String, Double>> sortedset = new TreeSet<Map.Entry<String, Double>>(
				new Comparator<Map.Entry<String, Double>>() {
					@Override
					public int compare(Map.Entry<String, Double> e1,
							Map.Entry<String, Double> e2) {
						int compareResult = e2.getValue().compareTo(e1.getValue());
						if (compareResult != 0) {
							return compareResult;
						} else {
							return 1;
						}
					}
				});
		sortedset.addAll(mapArg.entrySet());
		return sortedset;
	}

}
