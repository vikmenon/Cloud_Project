#pragma once

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/nonfree/features2d.hpp>
#include <opencv2/nonfree/nonfree.hpp>
#include <opencv2/legacy/legacy.hpp>

using namespace std;

class Frame_Processor
{
public:
	// Constructor
	Frame_Processor();
	~Frame_Processor();

	// methods
	void ProcessFrames(char* frames_path, char* dataset_path, int frames_count, char* videoKeyname);

private:
	void RunProcessFrames();
};