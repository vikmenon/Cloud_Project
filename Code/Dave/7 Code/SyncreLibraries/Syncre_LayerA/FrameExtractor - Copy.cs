using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

using Emgu.CV;
using Emgu.CV.CvEnum;
using Emgu.CV.Structure;
using Emgu.CV.Util;


namespace Syncre_LayerA
{
    class FrameExtractor
    {
        static string frameLocalStore          = "syncre-keyframes";

        static string videofilename;
        static List<Mat> keyFrames; 
        static List<string> keyFramesFileNames = null;


        //extracts keyframs from video and retunrs a list of the file names (stored on local disk)
        public static List<string> ExtractKeyFrames(string videoFileNameAndPath, ref string errMsg)
         {
            videofilename = Path.GetFileName(videoFileNameAndPath);
            keyFrames = new List<Mat>();
            keyFramesFileNames = new List<string>();

            Capture cap = new Capture(videoFileNameAndPath);

            Mat frame1 = new Mat();
            Mat frame2 = new Mat();
            Mat frame2_raw = new Mat();
            
            double frameCount = cap.GetCaptureProperty(CapProp.FrameCount) - 3;     //	***WARNING: cant read last two frames!! ***/

            frame1 = cap.QueryFrame();                       //Console.WriteLine("Read Frame%i\t", 1);	//read initial frame
            for (int i = 2; i <= frameCount; i++)			//read subsequent frames
            {
                frame2 = cap.QueryFrame();                  //Console.WriteLine("\nRead Frame%i\t", i);
                frame2.CopyTo(frame2_raw);      
                
                if (!HistogramMatch(frame1, frame2))
                {
                    Mat temp = new Mat();
                    frame1.CopyTo(temp);
                    keyFrames.Add(temp);					//is a keyframe
                }

                frame2_raw.CopyTo(frame1);

                #if DEBUG
                    CvInvoke.cvShowImage("Input Video", frame2_raw);
                    if (CvInvoke.cvWaitKey(1) >= 0) break;
                #endif
            }

            //check if keyframes extracted
            if (keyFrames.Count == 0)
            {
                errMsg = "No keyframes were extracted from video";
                return null;
            }

            //else, save keyframes to file
            if (KeyFrames_save())
                errMsg = "";
            else
                errMsg = "Error saving keyframes to local store";
            
            return keyFramesFileNames;
        }

        static bool KeyFrames_save()
        {
            string folderName = videofilename.Substring(0, videofilename.Length - 4);
            string currDir = Directory.GetCurrentDirectory();
            System.IO.Directory.CreateDirectory(Path.Combine(currDir, frameLocalStore, folderName));

            for (int i = 0; i < keyFrames.Count; i++)
            {
                //save file to disk
                string fileNameAndPath = Path.Combine(currDir, frameLocalStore, folderName) + "\\" + (i + 1).ToString() + ".jpg";

                CvInvoke.Imwrite(fileNameAndPath, keyFrames[i]);

                //record filename
                keyFramesFileNames.Add(fileNameAndPath);
            }

            //check if all frames succesfully saved
            if (keyFramesFileNames.Count == keyFrames.Count)
                return true;
            return false;
        }


        //histogram match
        static bool HistogramMatch(Mat frame1, Mat frame2)
        {
	        //convert frames to HSV color space
            Mat frame1_hist = new Mat();
            Mat frame2_hist = new Mat();

            
	        CvInvoke.CvtColor(frame1, frame1_hist, ColorConversion.Bgr2Gray);
	        CvInvoke.CvtColor(frame2, frame2_hist, ColorConversion.Bgr2Gray);

	        //set histogram parameters
	        int h_bins = 50; int s_bins = 60;
            int[] histSize = { h_bins, s_bins };       //int histSize[] = { h_bins, s_bins };

	        float[] h_ranges = { 0, 180 };
	        float[] s_ranges = { 0, 256 };

            
	        //const float[] ranges = { h_ranges, s_ranges };
            float[][] ranges = { h_ranges };

           

            //GrayHist = new float[256];
     //       Image<Gray, Byte> img_gray = new Image<Gray, byte>(frame1_hist.Rows, frame1_hist.Cols);
     //       frame1_hist.CopyTo(img_gray, null);
     //       DenseHistogram hist = new DenseHistogram(256, new RangeF(0, 256));
     //       hist.Calculate(new Image<Gray, Byte>[] { img_gray }, true, null);
            //hist.Calculate(new Image<Gray, Byte>[] { frame1_hist.ToImage<Gray, Byte>() }, true, null);




            //try each...
            //frame1_hist.ToImage<Gray, Byte>().MIplImage()
            //frame1_hist.ToImage<Gray, Byte>
            //frame1_hist.ToImage<Gray, Byte>().To
            //imencode: ``imdecode`` and ``imencode`` to read and write image from/to memory rather than a file.


            Image<Gray, float> img_temp = frame1_hist.ToImage<Gray, float>();
            Matrix<float> img = new Matrix<float>(img_temp.Width, img_temp.Height);

            //img_temp.CopyTo(img);
            CvInvoke.cvCopy(img_temp, img, new IntPtr());

            DenseHistogram hist = new DenseHistogram(256, new RangeF(0, 256));
            hist.Calculate(new Matrix<float>[] { img }, true, null);


            // Create a grayscale image
            //Image<Gray, Byte> img = new Image<Gray, byte>(400, 400);
            
            // Fill image with random values
            //img.SetRandUniform(new MCvScalar(), new MCvScalar(255));
            
            // Create and initialize histogram
            //DenseHistogram hist = new DenseHistogram(256, new RangeF(0.0f, 255.0f));
            
            // Histogram Computing
            //hist.Calculate<Byte>(new Image<Gray, byte>[] { img }, true, null);
            


            //CvArray<byte> aaa = null;
            //frame1_hist.CopyTo(aaa, null);


            //Image<Gray, Byte> temp_img = new Image<Gray, byte>(frame1_hist.Rows, frame1_hist.Cols, 1, frame1_hist);
            //frame1_hist.CopyTo(temp_img, null);

            //hist.Calculate<Byte>(new Image<Gray, byte>[] { temp_img }, true, null);


            
           

            //int[] channels = { 0, 1 };								//Use the o-th and 1-st channels

            ////calculate and normalize histograms
            ////MatND frame1_norm;
            ////MatND frame2_norm;

            //MatND<double> frame1_norm;
            //MatND<double> frame2_norm;


            //CvInvoke.CalcHist(frame1_hist, channels, null, frame1_norm, histSize, ranges, false);
            //CvInvoke.Normalize(frame1_norm, frame1_norm, 0, 1, NormType.MinMax, DepthType.Default, null);

            //CvInvoke.CalcHist(frame2_hist, channels, null, frame2_norm, histSize, ranges, false);
            //CvInvoke.Normalize(frame2_norm, frame2_norm, 0, 1, NormType.MinMax, DepthType.Default, null);

            //double result = CvInvoke.CompareHist(frame1_norm, frame2_norm, 0);		//Correlation comparion 

            //float thresh_min = 0.2f;		//get unique keyframes
            //float thresh_max = 0.997f;	    //good for fast moving objects in video
            //if (result < thresh_min)
            //    return false;			    //not a match: likely different scenes

	        return true;
        }
    }
}
