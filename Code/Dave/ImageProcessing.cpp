//#include "opencv/highgui.h"
#include "opencv2/opencv.hpp"
#include "opencv2/highgui/highgui.hpp"
#include <ctime>



using namespace cv;
using namespace std;

int main__(int argc, char** argv)
{
	Mat img1 = imread("images/cat0.jpg", CV_LOAD_IMAGE_UNCHANGED);
	Mat img2 = imread("images/scene0.jpg", CV_LOAD_IMAGE_UNCHANGED);
	
	//-- image pre-process --//

	//convert image to gray
	cvtColor(img1, img1, CV_BGR2GRAY);
	cvtColor(img2, img2, CV_BGR2GRAY);
	
	imshow("Input Image", img1);
	imshow("Refernce Image", img2);

	//for colored image, split image into R, G, B planes and compute histogram seperately
	//vector<Mat> img_planes;
	//split(img, img_planes);


	Mat img1_hist;
	Mat img2_hist;

	int histSize = 256;
	float range [] = {0, 256};
	const float* histRange = { range };

	//calculate histogram
	clock_t startTime_hist = clock();
		calcHist(&img1, 1, 0, Mat(), img1_hist, 1, &histSize, &histRange, true, false);
	clock_t endTime_hist = clock();
	double timeSec_hist = (endTime_hist - startTime_hist) / static_cast<double>(CLOCKS_PER_SEC);

	calcHist(&img2, 1, 0, Mat(), img2_hist, 1, &histSize, &histRange, true, false);  //ref

	// Draw the histogram
	int hist_w = 512; int hist_h = 400;
	int bin_w = cvRound((double) hist_w / histSize);

	Mat img1_hist_display(hist_h, hist_w, CV_8UC3, Scalar(0, 0, 0));
	Mat img2_hist_display(hist_h, hist_w, CV_8UC3, Scalar(0, 0, 0));

	/// Normalize the result to [ 0, img_hist.rows ]
	clock_t startTime_norm = clock();
		normalize(img1_hist, img1_hist, 0, img1_hist_display.rows, NORM_MINMAX, -1, Mat());
	clock_t endTime_norm = clock();
	double timeSec_norm = (endTime_norm - startTime_norm) / static_cast<double>(CLOCKS_PER_SEC);

	normalize(img2_hist, img2_hist, 0, img2_hist_display.rows, NORM_MINMAX, -1, Mat());

	/// Draw for channel
	for (int i = 1; i < histSize; i++)
	{
		line(img1_hist_display, Point(bin_w*(i - 1), hist_h - cvRound(img1_hist.at<float>(i - 1))),
			Point(bin_w*(i), hist_h - cvRound(img1_hist.at<float>(i))),
			Scalar(255, 0, 0), 2, 8, 0);
		line(img2_hist_display, Point(bin_w*(i - 1), hist_h - cvRound(img2_hist.at<float>(i - 1))),
			Point(bin_w*(i), hist_h - cvRound(img2_hist.at<float>(i))),
			Scalar(255, 0, 0), 2, 8, 0);
	}

	/// Display
	imshow("Image1 Histogram", img1_hist_display);
	imshow("Image2 Histogram", img2_hist_display);

	//match histograms
	
	/*CV_COMP_CORREL Correlation
	CV_COMP_CHISQR Chi - Square
	CV_COMP_INTERSECT Intersection
	CV_COMP_BHATTACHARYYA Bhattacharyya distance
	CV_COMP_HELLINGER Synonym for CV_COMP_BHATTACHARYYA*/
	
	clock_t startTime_match = clock();
		double result = compareHist(img1_hist, img2_hist, CV_COMP_CORREL);
	clock_t endTime_match = clock();
	double timeSec_match = (endTime_match - startTime_match) / static_cast<double>(CLOCKS_PER_SEC);

	cout << "Histogram: " << timeSec_hist << "ms  " << "Normalization: " << timeSec_norm << "ms  " << "Match:" << timeSec_match << "ms";
	printf("\nMatch Result: %.3f", result);

	cvWaitKey(0);
	return 0;
}
