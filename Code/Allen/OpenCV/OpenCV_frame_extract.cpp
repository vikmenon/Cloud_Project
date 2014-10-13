//
//  OpenCV_frame_extract.cpp
//  
//
//  Created by Allen starke on 10/02/14.
//
//
#include<opencv\cv.h>
#include<opencv\highgui.h>
#include<opencv\ml.h>
#include<opencv\cxcore.h>


IplImage* skipNFrames(CvCapture* capture, int n)
{
    for(int i = 0; i < n; ++i)
    {
        if(cvQueryFrame(capture) == NULL)
        {
            return NULL;
        }
    }
	
    return cvQueryFrame(capture);
}


int main(int argc, char* argv[])
{
	//pull input video from specified directory
    CvCapture* capture = cvCaptureFromFile("YOURINPUT_DIRECTORY/file.avi");
	
	char s [];
	
    IplImage* frame = NULL;
    do
    {
        frame = skipNFrames(capture, 4);
        cvNamedWindow("frame", CV_WINDOW_AUTOSIZE);
        cvShowImage("frame", frame);
		
		//include processing of frames algorithms here	
		//
		//
		//
		//end
		
		//output any data to a output folder- this case just extracted image
		sprintf(s,"YOUROUPUT_DIRECTORY/frame%d.jpg",num);
		cvSaveImage(s,frame);
		
        cvWaitKey(100);
    } while( frame != NULL );
	
    cvReleaseCapture(&capture);
    cvDestroyWindow("frame");
    cvReleaseImage(&frame);
	
    return 0;
}