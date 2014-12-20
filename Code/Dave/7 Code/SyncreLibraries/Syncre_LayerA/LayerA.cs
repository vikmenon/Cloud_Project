using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

using CloudServices;

namespace Syncre_LayerA
{
    
    public class Layer_A
    {
        string videofilename;                                    //filename of video on cloud store
        static string videofilename_local;                      //filename of downaloaded video from cloud store

        string videoKeyname;
        //static List<string> keyFramesFileNames;
        //static List<string> featuresDataFilenames;              //list of file names of feature vectors data


        public Layer_A(string videofilename)
        {
            this.videofilename = videofilename;
            this.videoKeyname = this.videofilename.Substring(0, videofilename.Count() - 4);        

            //featuresDataFilenames = new List<string>();
            //keyFramesFileNames = new List<string>();

            //generate unique key filename for the video
            //videoKeyname = Utilities.KeyGenerator.GetUniqueKey();  - used by work submitter
        }


        static bool CheckError(string errorMsg)
        {
            if (errorMsg != "")
            {
                //LogManager.Write("Error", errorMsg);
                return true;
            }
            return false;
        }

        public int /*List<string>*/ FrameExtraction(ref string statusMsg, bool uploadKeyframes = false)
        {
            //download video from cloud storage
            string errMsg = "";
            //Console.WriteLine(" - Downloading Video...");
            videofilename_local = CloudServices.CloudServices.DownloadVideo(videofilename, ref errMsg);
            if (CheckError(errMsg))
            {
                statusMsg = errMsg;
                return 0;            //crtical error occured. return and stop.
            }
            //videofilename_local = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "syncre-videos" + "\\" + videofilename); //TESTING

            //extract keyframes
            int numbFrames = 0;
            errMsg = "";
                //Console.WriteLine(" - Extracting keyframes from video...");

            //DateTime tt1 = DateTime.Now; Console.Write("                    Start time: "); Console.WriteLine(tt1);
                numbFrames = FrameExtractor.ExtractKeyFrames(videofilename_local, ref errMsg);
            //DateTime tt2 = DateTime.Now; Console.Write("                    Finish time: "); Console.WriteLine(tt2);
            //TimeSpan tt = tt2 - tt1; Console.Write("                    Processing time: "); Console.Write(tt.Minutes); Console.WriteLine(" minutes");

            CheckError(errMsg);
                     
            
            //OPTIONALLY upload raw frames to cloud storage
            if (uploadKeyframes)
            {
                errMsg = "";
                    //Console.WriteLine(" - Optionally uploading keyframes to cloud store...");
                    //CloudServices.UploadFrames(/*keyFramesFileNames,*/ videoKeyname, ref errMsg);
                CheckError(errMsg); 
            }

            return numbFrames;
        }

        //Uploads features data to cloud store
        public void FeaturesDataUpload(/*List<string> featuresDataFilenames,*/ ref string statusMsg)
        {
            //featuresDataFilenames.Add(AppDomain.CurrentDomain.BaseDirectory + "\\" + datasetsLocalFolder + "\\" + videoKeyname + "\\dave.xml");

            //note use AWS batch upload method here


           


            //iterative single file upload 
            string errMsg = "";
            CloudServices.CloudServices.UploadFeaturesData(/*featuresDataFilenames,*/ videoKeyname, ref errMsg);
            if (CheckError(errMsg))
                statusMsg = errMsg;

        }
    }
}
