#include <iostream>
#include "FileRead.h"
#include "Header.h"

using namespace std;
using std::cout;
using std::endl;

int main()
{
	//test
			char* filesPath = "C:\\Users\\Administrator\\Documents\\Visual Studio 2013\\Projects\\Syncre\\SyncreLibraries\\Syncre_LayerB\\bin\\Debug\\syncre-keyframes\\RaQksAdQku1m5GqaGXdmV61E7vRkkdKk";
			char* datasetPath = "C:\\Users\\Administrator\\Documents\\Visual Studio 2013\\Projects\\Syncre\\SyncreLibraries\\Syncre_LayerB\\bin\\Debug\\syncre-datasets\\RaQksAdQku1m5GqaGXdmV61E7vRkkdKk";
			int filesCount = 7;

			Frame_Processor frameProc = Frame_Processor();

			frameProc.ProcessFrames(filesPath, datasetPath, filesCount, "RaQksAdQku1m5GqaGXdmV61E7vRkkdKk");


	////get files in path
	//string* filenames_array = new string[filesCount + 2];
	//GetFiles(filesPath, filesCount, filenames_array);

	////use it!
	//for (int i = 2; i <= filesCount + 1; i++)
	//{
	//	string hh = filesPath + filenames_array[i];
	//}

	////free files memory
	//delete [] filenames_array;


	return 0;
}