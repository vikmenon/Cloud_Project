#pragma once
#include "C:\Users\Administrator\Documents\Visual Studio 2013\Projects\Syncre\FrameProcessorCore\FrameProcessor_lib\Header.h"

using namespace System;
using namespace System::Runtime::InteropServices;


namespace Syncre_LayerB_
{
	
	//wrapper class
	public ref class Frame_Processor_Wrapped
	{
	public:
		//constructor
		Frame_Processor_Wrapped(System::String^ algorithm);
		
		//method
		//void ProcessFrames(System::String^ framePath, System::String^ datasetPath, int frameCount, System::String^ videoKeyname);
		void ProcessFrames(String^ framePath, String^ datasetPath, int frameCount, String^ videoKeyname);

	private:
		Frame_Processor *frameProcessor;
	};


	//C# to c++ type conversion
	public ref class StringConversion
	{
	public:
		StringConversion();
		char* myStringToChar(System::String^ str);
	};
}