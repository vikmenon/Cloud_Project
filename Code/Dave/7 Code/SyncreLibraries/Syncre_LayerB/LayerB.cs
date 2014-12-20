using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Configuration;

namespace Syncre_LayerB_library
{
    using Syncre_LayerA;
    using Syncre_LayerB_;       //change namespace to frame processor



    public class LayerB
    {
        /* app.config settings */
        string videoLocalFolder = "syncre-videos";
        string datasetsLocalFolder = "syncre-datasets";
        string framesLocalFolder = "syncre-keyframes";
        /*---*/

        string searchServerAddress;

        Layer_A layerA;                         //Frame Extractor and Cloud Services (S3 and DynamoDB)

        string videoFilename;
        string videoKeyname;
        bool search;

        int numbFrames;
        string statusMsg;

        //constructor
        public LayerB(string videoFileName, bool search, string searchServerAddress)
        {
            videoFilename = videoFileName;
            videoKeyname = videoFileName.Substring(0, videoFileName.Length - 4);
            this.search = search;

            this.searchServerAddress = searchServerAddress;
        }
    
        public string Process(bool onDemand = true, bool keepFrames = false)
        {
            //process video and upload data to cloud-store
            DownloadAndFormatData(keepFrames);    //Layer-A 
            ProcessFrames();            //Frame Processor 
            UploadProcessedData();      //Layer-A

            //send transaction
            string statusMsg = "";
            if (onDemand)
                SendMessageToLayerC(ref statusMsg);
            else
                SendMessageToQueue(ref statusMsg);
            
            //confirm send 
            if (onDemand && statusMsg != "")         //critical
            {
                //FAULT-TOLERANCE: if search server is NOT alive, send transaction to queue for later processing. Notify web front
                statusMsg = "";
                SendMessageToQueue(ref statusMsg);

                CleanUp();  //makeshift code. NOTE!
                return "QUEUED-UP FOR SEARCH";
            }

            //clean-up
            CleanUp();
            return statusMsg;       //pipeline completed successfully
        }


        void DownloadAndFormatData(bool keepFrames)
        {
            //Console.WriteLine("<LAYER-A>:");
            statusMsg = "";

            layerA = new Syncre_LayerA.Layer_A(videoFilename);
            numbFrames = layerA.FrameExtraction(ref statusMsg, keepFrames);

            if (numbFrames == 0)
            {
                //critical error
                //Console.WriteLine(statusMsg);
                //Console.WriteLine("Press any key to end.");
                //Console.Read();
                return;
            }
        }

        void ProcessFrames()
        {
            //Console.WriteLine("<LAYER-B>:");
            //Console.WriteLine(" - Frame Processor: Pre-processing, Feature Detection and Feature Extraction...");
            //Console.Write("                    Number of frames: "); Console.WriteLine(numbFrames);

            string filesPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, framesLocalFolder, videoKeyname) + "\\";      //input dir 
            string outputFilesPath = Directory.CreateDirectory(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, datasetsLocalFolder, videoKeyname)).FullName + "\\";    //create output dir

            Frame_Processor_Wrapped frameProcessor = new Frame_Processor_Wrapped("");

            //DateTime t1 = DateTime.Now; Console.Write("                    Start time: "); Console.WriteLine(t1);
                frameProcessor.ProcessFrames(filesPath, outputFilesPath, numbFrames, videoKeyname);
            //DateTime t2 = DateTime.Now; Console.Write("                    Finish time: "); Console.WriteLine(t2);
            //TimeSpan t = t2 - t1; Console.Write("                    Processing time: "); Console.Write(t.Minutes); Console.WriteLine(" minutes");
        }

        //**NOTE use AWS batch mode
        void UploadProcessedData()
        {
            //Console.WriteLine(" - Uploading features dataset through <LAYER-A>...");
            statusMsg = "";
            layerA.FeaturesDataUpload(/*featuresDataFilenames,*/ ref statusMsg);
            //if (statusMsg != "")
            //    Console.WriteLine("  " + statusMsg);       //features data not uploaded. could also be critical
        }

        void SendMessageToQueue(ref string statusMsg)
        {
            //Console.WriteLine(" - Sending transaction to <LAYER-C>...");           
            string tranxKey = videoKeyname;
            string srch = "0";
            if (search == true)
                srch = "1";

            StringBuilder message = new StringBuilder();
            message.AppendLine(tranxKey);
            message.AppendLine(srch);
           
            CloudServices.CloudServices.PutTransaction(message.ToString(), ref statusMsg);
        }

        //ayschronously sends key to search layer to begin processing, and returns. Search layer returns serach results (through http) to webfront layer 
        void SendMessageToLayerC(ref string statusMsg)
        {
            //create instance of http client
            Syncre_LayerB.Utilities.HttpSendClient searchClient = new Syncre_LayerB.Utilities.HttpSendClient(searchServerAddress);

            //send transaction to server, returns. Search server sends results directly to web front
            searchClient.SendPlainString(videoKeyname, ref statusMsg);         
        }

        void CleanUp()
        {
            //Console.WriteLine(" - Cleaning-up...");
            CleanUpLocalStore();

            //Console.WriteLine("\n*End of pipeline*\n");
            //Finish(); 
        }

        void Finish()
        {
            //Console.WriteLine("Press any key to exit...");
            //Console.ReadKey();
            //System.Environment.Exit(1);
        }

        void CheckError(string errorMsg)
        {
            if (errorMsg != "")
            {
                Console.WriteLine(errorMsg);
                Finish();
            }
        }

        #region Clean-up Local Store

        //Clean up local store
        void CleanUpLocalStore()
        {
            //Delete uploaded features data from local store
            DeleteFeaturesDataFromLocal();

            //Delete the processed frames from local store
            DeleteFramesFromLocal();

            //Delete the processed video from local store
            DeleteVideofromLocalStore();

        }

        //delete features data from local store
        private void DeleteFeaturesDataFromLocal()
        {
            System.IO.DirectoryInfo dirInfo = new DirectoryInfo(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, datasetsLocalFolder, videoKeyname));
            if (dirInfo.Exists)
                dirInfo.Delete(true);
        }

        //delete keyframes from local store
        private void DeleteFramesFromLocal()
        {
            System.IO.DirectoryInfo dirInfo = new DirectoryInfo(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, framesLocalFolder, videoKeyname));
            if (dirInfo.Exists)
                dirInfo.Delete(true);
        }

        //delete video from local store
        private void DeleteVideofromLocalStore()
        {
            string file = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, videoLocalFolder, videoFilename);
            if (File.Exists(file))
                File.Delete(file);
        }

        #endregion
    }    
}
