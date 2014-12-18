using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Drawing;

using Emgu.CV;
using Emgu.CV.CvEnum;
using Emgu.CV.Structure;
using Emgu.CV.Util;


namespace Syncre_LayerA
{
    class FrameExtractor
    {
        static string frameLocalStore = "syncre-keyframes";

        static string videofilename;
        static List<Bitmap> keyFrames;
        static List<long> keyFrames_frameIdicies;
        static int numbFrames = 0;

        static int f_width, f_height;
        //extracts keyframs from video and retunrs a list of the file names (stored on local disk)
        public static int /*List<string>*/ ExtractKeyFrames(string videoFileNameAndPath, ref string errMsg)
        {
            videofilename = Path.GetFileName(videoFileNameAndPath);
            keyFrames = new List<Bitmap>();
            keyFrames_frameIdicies = new List<long>();

            Capture cap = new Capture(videoFileNameAndPath);

            f_width = int.Parse(cap.GetCaptureProperty(CAP_PROP.CV_CAP_PROP_FRAME_WIDTH).ToString());
            f_height = int.Parse(cap.GetCaptureProperty(CAP_PROP.CV_CAP_PROP_FRAME_HEIGHT).ToString());          

            Bitmap frame1 = new Bitmap(f_height, f_width);
            Bitmap frame2 = new Bitmap(f_height, f_width);
            

            double frameCount = cap.GetCaptureProperty(CAP_PROP.CV_CAP_PROP_FRAME_COUNT) - 3;     //	***WARNING: cant read last two frames!! ***/

            frame1 = cap.QueryFrame().ToBitmap();           
            for (long i = 1; i <= frameCount; i++)			            //use while(cap.grab()) instead to prevent warning error
            {
                long frame_idx = (long)cap.GetCaptureProperty(CAP_PROP.CV_CAP_PROP_POS_FRAMES);
                frame2 = cap.QueryFrame().ToBitmap();
                
                if (!HistogramMatch(frame1, frame2))
                {
                    keyFrames.Add(frame1);                   //frame
                    keyFrames_frameIdicies.Add(frame_idx);   //frame#   

                    numbFrames++;
                }
                frame1 = (Bitmap) frame2.Clone();

#if DEBUG
                //Image<Bgr, byte> imgToshow = new Image<Bgr, byte>(frame2);
                //CvInvoke.cvShowImage("Input Video", imgToshow);
                //if (CvInvoke.cvWaitKey(1) >= 0) break;
#endif
            }
#if DEBUG
            //CvInvoke.cvDestroyWindow("Input Video");
#endif
            cap.Dispose();      //close video handler

            //check if keyframes extracted
            if (numbFrames == 0)
            {
                errMsg = "No keyframes were extracted from video";
                return numbFrames;
            }
         
            //save keyframes to file
            if (KeyFrames_save())
                errMsg = "";
            else
                errMsg = "Error saving keyframes to local store";

            return numbFrames;
        }

        static bool KeyFrames_save()
        {
            string folderName = videofilename.Substring(0, videofilename.Length - 4);
            string currDir = AppDomain.CurrentDomain.BaseDirectory;
            System.IO.Directory.CreateDirectory(Path.Combine(currDir, frameLocalStore, folderName));

            for (int i = 0; i < keyFrames.Count; i++)
            {
                //save file to disk
                string fileNameAndPath = Path.Combine(currDir, frameLocalStore, folderName) + "\\" + (i + 1).ToString() + ".jpg";
                //string fileNameAndPath = Path.Combine(currDir, frameLocalStore, folderName) + "\\" + keyFrames_frameIdicies[i] + ".jpg";

                try
                {
                    CvInvoke.cvSaveImage(fileNameAndPath, new Image<Bgr, byte>(keyFrames[i]), new IntPtr());
                }
                catch(Exception excep)
                {
                    return false;
                }

                //record filename
                //keyFramesFileNames.Add(fileNameAndPath);
            }

            //check if all frames succesfully saved
            //if (keyFramesFileNames.Count == keyFrames.Count)
            //    return true;
            
            return true;
        }

        //histogram match
        static bool HistogramMatch(Bitmap frame1, Bitmap frame2)
        {
            //convert bitmap to  temp "Image" for color processing by older version of emgucv
            Image<Bgr, byte> frame1_Image = new Image<Bgr, byte>(frame1);
            Image<Bgr, byte> frame2_Image = new Image<Bgr, byte>(frame2);

            //convert frames to HSV color space
            Image<Gray, byte> frame1_hist = new Image<Gray, byte>(f_width, f_height);
            Image<Gray, byte> frame2_hist = new Image<Gray, byte>(f_width, f_height);

            CvInvoke.cvCvtColor(frame1_Image, frame1_hist, COLOR_CONVERSION.BGR2GRAY);
            CvInvoke.cvCvtColor(frame2_Image, frame2_hist, COLOR_CONVERSION.BGR2GRAY);

            //dispose temp Image after color conversion
            frame1_Image.Dispose();
            frame2_Image.Dispose();


            #region Multi-dimentional hitogram (HSV channels)
            ////set histogram parameters
            /*int h_bins = 50; int s_bins = 60;
            int[] histSize = { h_bins, s_bins };       //int histSize[] = { h_bins, s_bins };

            float[] h_ranges = { 0, 180 };
            float[] s_ranges = { 0, 256 };

            //const float[] ranges = { h_ranges, s_ranges };
            float[][] ranges = { h_ranges };*/


            //set histogram parameters
            /*int[] bins = {256, 256, 256};
            RangeF r = new RangeF(0, 256);
            RangeF[] ranges = {r, r, r};*/

            //DenseHistogram denseHist_frame1 = new DenseHistogram(bins, ranges);
            //DenseHistogram denseHist_frame2 = new DenseHistogram(bins, ranges);*
            #endregion

            #region Single-dimentional (Grayscale) histogram
            DenseHistogram denseHist_frame1 = new DenseHistogram(256, new RangeF(0.0f, 256.0f));
            DenseHistogram denseHist_frame2 = new DenseHistogram(256, new RangeF(0.0f, 256.0f));

            denseHist_frame1.Calculate(new Image<Gray, byte>[] { frame1_hist }, true, null);
            denseHist_frame2.Calculate(new Image<Gray, byte>[] { frame2_hist }, true, null);

            denseHist_frame1.Normalize(1);
            denseHist_frame2.Normalize(1);
            #endregion

            double histCompareRst = CvInvoke.cvCompareHist(denseHist_frame1, denseHist_frame2, HISTOGRAM_COMP_METHOD.CV_COMP_CORREL);

            float thresh_min = 0.2f;		//get unique keyframes
            float thresh_max = 0.997f;	    //good for fast moving objects in video
            if (histCompareRst < 0.8f)
                return false;			    //not a match: likely different scenes

            return true;
        }
    }
}
