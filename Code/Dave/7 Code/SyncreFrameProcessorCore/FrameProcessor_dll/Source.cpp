#include "stdafx.h"
#include "Header.h"

namespace Syncre_LayerB_ 
{

	Frame_Processor_Wrapped::Frame_Processor_Wrapped(System::String^ algorithm)
	{
		//convert System.String to std::string (or char*)
		//StringConversion ^convert = gcnew StringConversion();
		//char* algorithm_char = convert->myStringToChar(algorithm);
		
		frameProcessor = new Frame_Processor();
	}

	
	//void Frame_Processor_Wrapped::ProcessFrames(System::String^ framePath, System::String^ datasetPath, int frameCount, System::String^ videoKeyname)
	void Frame_Processor_Wrapped::ProcessFrames(String^ framePath, String^ datasetPath, int frameCount, String^ videoKeyname)
	{
		//convert System.String to std::string (or char*)
		/*StringConversion ^convert = gcnew StringConversion();
		
		char* framePath_char = convert->myStringToChar(framePath);
		char* videoKeyname_char = convert->myStringToChar(videoKeyname);
		char* datasetPath_char = convert->myStringToChar(datasetPath);
												
		frameProcessor->ProcessFrames(framePath_char, datasetPath_char, frameCount, videoKeyname_char);	*/	


		//convert String to char*
		String^ s_framePath = gcnew String(framePath);
		IntPtr ip_framePath = Marshal::StringToHGlobalAnsi(s_framePath);
		char* str_framePath = static_cast<char*>(ip_framePath.ToPointer());

		//convert System.String to char*
		String^ s_datasetPath = gcnew String(datasetPath);
		IntPtr ip_datasetPath = Marshal::StringToHGlobalAnsi(s_datasetPath);
		char* str_datasetPath = static_cast<char*>(ip_datasetPath.ToPointer());

		//convert System.String to char*
		String^ s_videoKeyname = gcnew String(videoKeyname);
		IntPtr ip_videoKeyname = Marshal::StringToHGlobalAnsi(s_videoKeyname);
		char* str_videoKeyname = static_cast<char*>(ip_videoKeyname.ToPointer());

		//call unmanged
		frameProcessor->ProcessFrames(str_framePath, str_datasetPath, frameCount, str_videoKeyname);
		
		//free type conversin memory
		Marshal::FreeHGlobal(ip_framePath);
		Marshal::FreeHGlobal(ip_datasetPath);
		Marshal::FreeHGlobal(ip_videoKeyname);

		
	}


	/* Conversion Class*/
	StringConversion::StringConversion() {}

	char* StringConversion::myStringToChar(System::String^ str)
	{
		System::IntPtr ptr = Marshal::StringToHGlobalAnsi(str);
		return static_cast<char*>(ptr.ToPointer());
	}	
}