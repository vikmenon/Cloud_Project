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

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvHistogram;

public class HistogramUtils {
	private static final String[] pictures = {
			"C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/CBVRProject/Images/cat.jpg",
			"C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/CBVRProject/Images/cat1.jpg",
			"C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/CBVRProject/Images/cat2.jpg",
			"C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/CBVRProject/Images/cat3.jpg",
			"C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/CBVRProject/Images/dave.jpg",
			"C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/CBVRProject/Images/flower.jpg",
			"C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/CBVRProject/Images/lena.jpg",
			"C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/CBVRProject/Images/scene0.jpg"
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

	public static void main(String[] args) throws Exception {
		// Input: Images to compare
		String queryImageFile = pictures[0];
		String[] dbImageFiles = pictures;

		for (String dbImageFile : dbImageFiles) {
			IplImage queryImage = cvLoadImage(queryImageFile);
			IplImage dbImage = cvLoadImage(dbImageFile);

			CvHistogram queryImageHistogram = getHueHistogram(queryImage);
			CvHistogram dbImageHistogram = getHueHistogram(dbImage);

			double matchPercentage = Math.floor(100 * cvCompareHist(
					queryImageHistogram, dbImageHistogram, CV_COMP_INTERSECT));
			System.out.println("\nPercentage match between -\n\t"
					+ queryImageFile + "\n- and -\n\t" + dbImageFile
					+ "\n- is " + matchPercentage + "%.");
		}

// TODO-Vikram		printSortedImageMatches();
	}
	
/* TODO-Vikram
	private static void printSortedImageMatches() {
		SortedSet<Map.Entry<String, Double>> sortedset = new TreeSet<Map.Entry<String, Double>>(
				new Comparator<Map.Entry<String, Double>>() {
					@Override
					public int compare(Map.Entry<String, Double> e1,
							Map.Entry<String, Double> e2) {
						return e1.getValue().compareTo(e2.getValue());
					}
				});
		sortedset.addAll(myMap.entrySet());
	}*/
}
