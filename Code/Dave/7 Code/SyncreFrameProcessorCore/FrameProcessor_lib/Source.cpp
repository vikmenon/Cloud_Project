#include <ctime>
#include "Header.h"
#include "FileRead.h"
#include <Windows.h>


	using namespace std;
	using namespace cv;

	string featDectAlgorithm = "SURF";
	string featExtrAlgorithm = "FREAK";


	char* framesPath;
	char* datasetsLocalFolder;
	int framesCount;
	char* videoKeyname;
	string* frames_filenames_array;

	Frame_Processor::Frame_Processor()
	{
		//__HrLoadAllImportsForDll
	}

	Frame_Processor::~Frame_Processor()
	{
		cvDestroyAllWindows();
	}

	
	void Frame_Processor::ProcessFrames(char* frames_path, char* dataset_path, int frames_count, char* video_keyname)
	{
		framesPath = frames_path;
		datasetsLocalFolder = dataset_path;
		framesCount = frames_count;
		videoKeyname = video_keyname;

		RunProcessFrames();
	}

	/*-------- Non-Public method---------*/
	void Frame_Processor::RunProcessFrames()
	{
		//initialize non-free opencv module (SIFT and SURF). regquires /nonfree/nonfree.hpp
		initModule_nonfree();


		//get all frames in folder
		frames_filenames_array = new string[framesCount + 2];  //. and  ..
		
		GetFiles(framesPath, framesCount, frames_filenames_array);

		//iterate through each frame and process it
		string frameFileName;
		for (int i = 2; i <= framesCount + 1; i++)
		{
			//string test = framsPath + frames_filenames_array[i];
			//get frames
			Mat frame = imread(framesPath + frames_filenames_array[i], CV_LOAD_IMAGE_GRAYSCALE);



			/* re-size */



			/* Grayscale */



			/* Feature Detection */
			Ptr<FeatureDetector> detector = FastFeatureDetector::create(featDectAlgorithm);
			vector<KeyPoint> keypoints;

			//clock_t startTime_detect = clock();
			detector->detect(frame, keypoints);
			//clock_t endTime_detect = clock();

			//double timeSec_detect = (endTime_detect - startTime_detect) / static_cast<double>(CLOCKS_PER_SEC);
			//cout << "\nDetection time = " << timeSec_detect << "secs";

			//Mat img_display;
			//drawKeypoints(frame, keypoints, img_display);

			//namedWindow("Detected Features", CV_WINDOW_AUTOSIZE);
			//imshow("Detected Features", img_display);



			/* Feature Extraction */
			Ptr<DescriptorExtractor> descriptorsExtractor = DescriptorExtractor::create(featExtrAlgorithm);
			Mat descriptors;

			//clock_t startTime_extract = clock();
			descriptorsExtractor->compute(frame, keypoints, descriptors);
			//clock_t endTime_extract = clock();

			//double timeSec_extract = (endTime_extract - startTime_extract) / static_cast<double>(CLOCKS_PER_SEC);
			//cout << "\nExtraction time = " << timeSec_extract << "secs";

			/* File Storage */
			string fileName = frames_filenames_array[i];


			/*char* output_dir;
			strcpy(output_dir, datasetsLocalFolder);
			strcat(output_dir, videoKeyname);	strcat(output_dir, "\\");	strcat(str, ".xml");*/
			
			//FileStorage fs(std::string(datasetsLocalFolder) + "\\" + videoKeyname + "\\" + fileName + ".xml", FileStorage::WRITE);
			//string newFile = string(datasetsLocalFolder) + "\\" + string(videoKeyname) + "\\" + fileName + ".xml";

			FileStorage fs(string(datasetsLocalFolder) + "\\" + fileName + ".xml", FileStorage::WRITE);
			//FileStorage fs("david.xml", FileStorage::WRITE);  //frames_filenames_array[i]			
			

			fs << "ImageFile" << fileName;
			fs << "Features" << descriptors;
			fs.release();
		}

		//free memory
		delete [] frames_filenames_array;
	}